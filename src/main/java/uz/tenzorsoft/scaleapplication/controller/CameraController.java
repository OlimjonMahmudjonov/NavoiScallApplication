package uz.tenzorsoft.scaleapplication.controller;

import jakarta.servlet.http.HttpServletRequest;
import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.sentDataNavoi.ApiResponse;
import uz.tenzorsoft.scaleapplication.service.AttachService;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.ui.BaseController;
import uz.tenzorsoft.scaleapplication.ui.ButtonController;
import uz.tenzorsoft.scaleapplication.ui.TableController;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.*;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.truckExitPosition;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.truckPosition;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CameraController implements BaseController {

    private final AttachService attachService;
    private final TruckService truckService;
    private final LogService logService;
    private final RestTemplate restTemplate;

    @Value("${spring.url}/navoiyazot-transfers/drivers-with-transfers")
    private String apiUrl;
    @Value("${spring.token}")
    private String token;

    @Autowired @Lazy
    private TableController tableController;
    @Autowired @Lazy
    private ButtonController buttonController;

    @PostMapping(value = "/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(HttpServletRequest request, @PathVariable("id") Integer cameraId) {
        if (!(request instanceof MultipartHttpServletRequest multipartRequest)) {
            showAlert(Alert.AlertType.ERROR, "Xato", "Noto‘g‘ri so‘rov formati");
            return ResponseEntity.ok("INVALID_REQUEST");
        }

        log.info("=== KAMERA SO‘ROVI KELDI | ID: {} ===", cameraId);
        List<AttachResponse> attachResponses = new ArrayList<>();
        String plateNumber = null;

        for (Map.Entry<String, MultipartFile> entry : multipartRequest.getFileMap().entrySet()) {
            String fileName = entry.getKey();
            MultipartFile file = entry.getValue();

            try {
                // ANPR xml
                if (fileName.equalsIgnoreCase("anpr.xml")) {
                    plateNumber = extractNumberFromXmlFile(file);
                    if (plateNumber == null || plateNumber.trim().isEmpty() || plateNumber.equalsIgnoreCase("unknown")) {
                        log.warn("Raqam aniqlanmadi");
                        return ResponseEntity.ok("NOT_MATCH");
                    }

                    if (!isAuthorizedTruck(plateNumber)) {
                        log.warn("Ruxsat yo‘q: {}", plateNumber);
                        showAlert(Alert.AlertType.WARNING, "Ogohlantirish", "Ruxsat yo‘q: " + plateNumber);
                        return ResponseEntity.ok("NOT_AUTHORIZED");
                    }

                    if (cameraId == 1) {
                        currentTruck.setTruckNumber(plateNumber);
                        isWaiting = true;
                    } else {
                        currentExitTruck.setTruckNumber(plateNumber);
                        isExitWaiting = true;
                    }
                }

                // Detection pictures
                if (fileName.contains("detectionPicture")) {
                    byte[] bytes = file.getBytes();
                    AttachResponse attach = attachService.saveToSystem(bytes); // uses @Transactional(REQUIRES_NEW)

                    if (attach != null && attach.getId() != null) {
                        attachResponses.add(attach);
                        log.info("Rasm saqlandi → AttachID={}", attach.getId());
                    } else {
                        logService.save(new LogEntity(5L, plateNumber, "Attach saqlanmadi yoki ID yo‘q"));
                    }
                }
            } catch (Exception e) {
                log.error("Faylni qayta ishlashda xato: {}", e.getMessage(), e);
                logService.save(new LogEntity(5L, plateNumber, "00006: (CameraController) " + e.getMessage()));
                return ResponseEntity.ok("ERROR");
            }
        }

        if (plateNumber == null) {
            log.warn("Raqam topilmadi (cameraId={})", cameraId);
            return ResponseEntity.ok("NOT_MATCH");
        }

        try {
            if (cameraId == 1) {
                handleEntrance(plateNumber, attachResponses);
            } else if (cameraId == 2) {
                handleExit(plateNumber, attachResponses);
            }
        } catch (Exception e) {
            log.error("Darvoza jarayonida xato: {}", e.getMessage(), e);
            logService.save(new LogEntity(5L, plateNumber, "00047: (CameraController) " + e.getMessage()));
            return ResponseEntity.ok("ERROR");
        }

        return ResponseEntity.ok("OK");
    }

    /** --- Kirish jarayoni --- */
    private void handleEntrance(String truckNumber, List<AttachResponse> attaches) {
        if (buttonController.openGate1(0)) {
            currentTruck.setEnteredStatus(TruckAction.ENTRANCE);
            firstGateEntranceTime = System.currentTimeMillis();
            truckService.saveTruck(currentTruck, 1, attaches);
            tableController.addLastRecord();
            log.info("DARVOZA OCHILDI (KIRISH) | {} | Rasmlar: {}", truckNumber, attaches.size());
        } else {
            showAlert(Alert.AlertType.ERROR, "Xato", "Darvoza ochilmadi!");
            currentTruck = new TruckResponse();
            isWaiting = false;
        }
    }

    /** --- Chiqish jarayoni --- */
    private void handleExit(String truckNumber, List<AttachResponse> attaches) {
        if (buttonController.openExitGate1(0)) {
            currentExitTruck.setEnteredStatus(TruckAction.EXIT);
            firstExitGateEntranceTime = System.currentTimeMillis();
            truckService.saveTruck(currentExitTruck, 2, attaches);
            tableController.addLastRecord();
            log.info("DARVOZA OCHILDI (CHIQISH) | {} | Rasmlar: {}", truckNumber, attaches.size());
        } else {
            showAlert(Alert.AlertType.ERROR, "Xato", "Chiqish darvozasi ochilmadi!");
            currentExitTruck = new TruckResponse();
            isExitWaiting = false;
        }
    }

    /** --- XML raqamni o‘qish --- */
    private String extractNumberFromXmlFile(MultipartFile file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(file.getBytes());
            Document doc = builder.parse(input);
            NodeList nodes = doc.getElementsByTagName("originalLicensePlate");
            if (nodes.getLength() > 0) {
                return nodes.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            log.error("XML o‘qishda xato: {}", e.getMessage(), e);
            logService.save(new LogEntity(5L, truckNumber, "00007: (CameraController) " + e.getMessage()));
        }
        return null;
    }

    /** --- API orqali ruxsatni tekshirish --- */
    private boolean isAuthorizedTruck(String truckNumber) {
        String clean = truckNumber.replaceAll("\\s+", "").toUpperCase();
        String url = apiUrl + "?status=ENTERED&size=2000";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        try {
            ResponseEntity<ApiResponse> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), ApiResponse.class);
            if (resp.getBody() == null || resp.getBody().getContent() == null) return false;

            return resp.getBody().getContent().stream()
                    .anyMatch(item -> item.getDriver().getTransportNumber()
                            .replaceAll("\\s+", "").equalsIgnoreCase(clean));
        } catch (Exception e) {
            log.error("API bilan aloqa xatosi: {}", e.getMessage(), e);
            return false;
        }
    }
}
