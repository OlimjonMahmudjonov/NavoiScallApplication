package uz.tenzorsoft.scaleapplication.controller;

import jakarta.servlet.http.HttpServletRequest;
import javafx.application.Platform;
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
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;
import uz.tenzorsoft.scaleapplication.sentDataNavoi.ApiResponse;
import uz.tenzorsoft.scaleapplication.sentDataNavoi.RefreshToken;
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
import java.util.concurrent.ConcurrentHashMap;

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
    private final RefreshToken refreshToken;
    private static final Set<String> alertShown = ConcurrentHashMap.newKeySet();
    @Autowired
    private TruckRepository truckRepository;

    @Value("${spring.url}/navoiyazot-transfers/drivers-with-transfers-current-status")
    private String apiUrl;

    @Autowired
    @Lazy
    private TableController tableController;
    @Autowired
    @Lazy
    private ButtonController buttonController;

    @PostMapping(value = "/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(HttpServletRequest request, @PathVariable("id") Integer cameraId) {
        if (!(request instanceof MultipartHttpServletRequest multipartRequest)) {
            if (alertShown.add("INVALID_REQUEST")) {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Xato", "Noto‘g‘ri so‘rov formati")
                );
            }
            return ResponseEntity.ok("INVALID_REQUEST");
        }

        log.info("=== KAMERA SO‘ROVI KELDI | Kamera ID: {} ===", cameraId);

        List<AttachResponse> attachResponses = new ArrayList<>();
        String plateNumber = null;

        // 1. Barcha fayllarni o‘qish (ANPR + detectionPicture)
        for (Map.Entry<String, MultipartFile> entry : multipartRequest.getFileMap().entrySet()) {
            String fileName = entry.getKey();
            MultipartFile file = entry.getValue();

            try {
                // ANPR XML → raqam olish
                if (fileName.equalsIgnoreCase("anpr.xml")) {
                    plateNumber = extractNumberFromXmlFile(file);
                    if (plateNumber == null || plateNumber.trim().isEmpty() || plateNumber.equalsIgnoreCase("unknown")) {
                        log.warn("Raqam aniqlanmadi yoki 'unknown'");
                        return ResponseEntity.ok("NOT_MATCH");
                    }
                    plateNumber = cleanTruckNumber(plateNumber);
                    log.info("Aniqlangan raqam: {}", plateNumber);
                }

                // Detection rasmlar
                if (fileName.contains("detectionPicture")) {
                    byte[] bytes = file.getBytes();
                    AttachResponse attach = attachService.saveToSystem(bytes);
                    if (attach != null && attach.getId() != null) {
                        attachResponses.add(attach);
                        log.info("Rasm saqlandi → AttachID={}", attach.getId());
                    }
                }
            } catch (Exception e) {
                log.error("Faylni o‘qishda xato: {}", e.getMessage(), e);
                return ResponseEntity.ok("ERROR");
            }
        }

        if (plateNumber == null) {
            log.warn("Raqam topilmadi (ANPR yo‘q)");
            return ResponseEntity.ok("NOT_MATCH");
        }

        // 2. KIRISH (1) → API tekshiruvi
        if (cameraId == 1) {
            if (!isAuthorizedTruck(plateNumber)) {
                log.warn("KIRISH RAD ETILDI → API da ENTERED holatida emas: {}", plateNumber);

                if (alertShown.add("KIRISH_" + plateNumber)) {  // faqat 1 marta
                    showAlert(Alert.AlertType.WARNING, "Ruxsat yo‘q", "Mashina hali kiritilmagan: " + plateNumber);
                }
                return ResponseEntity.ok("NOT_AUTHORIZED");
            }

            currentTruck.setTruckNumber(plateNumber);
            isWaiting = true;
            log.info("KIRISHGA RUXSAT BERILDI → {}", plateNumber);

        }
        // 3. CHIQISH (2) → Ichki baza tekshiruvi (COMPLETE bo‘lishi shart!)
        else if (cameraId == 2) {
            Optional<TruckEntity> readyTrucks = truckRepository.findTopCompleteTruck(plateNumber);

            if (readyTrucks.isEmpty()) {
                log.warn("CHIQISH RAD ETILDI → Mashina hali taroziga kirmagan yoki jarayon yakunlanmagan: {}", plateNumber);

                if (alertShown.add("CHIQISH_" + plateNumber)) {  // faqat 1 marta
                    showAlert(Alert.AlertType.WARNING, "Chiqish rad etildi",
                            "Mashina hali taroziga kirmagan yoki jarayon yakunlanmagan: " + plateNumber);
                }
                return ResponseEntity.ok("NOT_READY_FOR_EXIT");
            }

            currentExitTruck.setTruckNumber(plateNumber);
            isExitWaiting = true;
            log.info("CHIQISHGA RUXSAT BERILDI → TruckID={}, Raqam={}",
                    readyTrucks.get().getId(), plateNumber);
        }

        // 4. Jarayonlarni ishga tushirish
        try {
            if (cameraId == 1) {
                handleEntrance(plateNumber, attachResponses);
            } else if (cameraId == 2) {
                handleExit(plateNumber, attachResponses);
            }
        } catch (Exception e) {
            log.error("Jarayon ishga tushmadi: {}", e.getMessage(), e);
            logService.save(new LogEntity(5L, plateNumber, "00047: (CameraController) " + e.getMessage()));
            return ResponseEntity.ok("ERROR");
        }

        return ResponseEntity.ok("OK");
    }

    /**
     * --- Kirish jarayoni ---
     */
    private void handleEntrance(String truckNumber, List<AttachResponse> attaches) {
        if (buttonController.openGate1(0)) {
            currentTruck.setEnteredStatus(TruckAction.ENTRANCE);
            firstGateEntranceTime = System.currentTimeMillis();
            truckService.saveTruck(currentTruck, 1, attaches);
            tableController.addLastRecord();

            alertShown.remove("KIRISH_" + truckNumber);
            alertShown.remove("CHIQISH_" + truckNumber);

            log.info("DARVOZA OCHILDI (KIRISH) | {} | Rasmlar: {}", truckNumber, attaches.size());
        } else {
            showAlert(Alert.AlertType.ERROR, "Xato", "Darvoza ochilmadi!");
            currentTruck = new TruckResponse();
            isWaiting = false;
        }
    }


    /**
     * --- Chiqish jarayoni ---
     */
    private void handleExit(String truckNumber, List<AttachResponse> attaches) {
        if (buttonController.openExitGate1(0)) {
            // TO‘G‘RI: Chiqish uchun exitedStatus ishlatiladi!
            currentExitTruck.setExitedStatus(TruckAction.EXIT);  // ← BU TO‘G‘RI!
            // currentExitTruck.setEnteredStatus(...) — BU XATO edi!

            firstExitGateEntranceTime = System.currentTimeMillis();
            truckService.saveTruck(currentExitTruck, 2, attaches);
            tableController.addLastRecord();

            alertShown.remove("KIRISH_" + truckNumber);
            alertShown.remove("CHIQISH_" + truckNumber);

            log.info("DARVOZA OCHILDI (CHIQISH) | {} | Rasmlar: {}", truckNumber, attaches.size());
        } else {
            showAlert(Alert.AlertType.ERROR, "Xato", "Chiqish darvozasi ochilmadi!");
            currentExitTruck = new TruckResponse();
            isExitWaiting = false;
        }
    }

    /**
     * --- XML raqamni o‘qish ---
     */
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

    private String cleanTruckNumber(String number) {
        if (number == null) return "";
        return number.replaceAll("\\s+", "").toUpperCase();
    }

    /**
     * --- API orqali ruxsatni tekshirish ---
     */
    private boolean isAuthorizedTruck(String truckNumber) {
        if (truckNumber == null || truckNumber.trim().isEmpty()) {
            return false;
        }

        String cleaned = cleanTruckNumber(truckNumber);

        String token;
        try {
            token = refreshToken.getNewToken();
        } catch (Exception e) {
            log.error("Token olishda xato (isAuthorizedTruck): {}", e.getMessage(), e);
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        int page = 0;
        final int size = 2000;

        try {
            while (true) {
                String url = apiUrl + "?status=ENTERED&page=" + page + "&size=" + size;

                ResponseEntity<ApiResponse> response = restTemplate.exchange(
                        url, HttpMethod.GET, new HttpEntity<>(headers), ApiResponse.class
                );

                ApiResponse body = response.getBody();
                if (body == null || body.getContent() == null || body.getContent().isEmpty()) {
                    if (body == null || body.isLast()) break;
                    page++;
                    continue;
                }

                // Har bir itemda transfers ichidagi statusChanges ichida ENTERED borligini tekshiramiz
                for (var item : body.getContent()) {

                    if (item.getDriver() == null || item.getDriver().getTransportNumber() == null)
                        continue;

                    String apiNumber = cleanTruckNumber(item.getDriver().getTransportNumber());

                    // mashina raqami mos kelmasa davom etamiz
                    if (!cleaned.equals(apiNumber)) continue;

                    //  faqat transfers ichidagi statusChanges ni tekshiramiz
                    boolean enteredFound = item.getTransfers().stream()
                            .filter(t -> t.getStatusChanges() != null)
                            .anyMatch(t -> t.getStatusChanges().stream()
                                    .anyMatch(s -> s.equalsIgnoreCase("ENTERED"))
                            );

                    if (enteredFound) {
                        log.info("RUXSAT BERILDI: {} (ENTERED topildi)", truckNumber);
                        return true;
                    }
                }

                if (body.isLast() || (body.getTotalPages() > 0 && page >= body.getTotalPages() - 1))
                    break;

                page++;
            }

            log.info("RUXSAT BERILMADI: {} (ENTERED holatida topilmadi)", truckNumber);
            return false;

        } catch (Exception e) {
            log.error("API xatosi (isAuthorizedTruck, mashina: {}): {}", truckNumber, e.getMessage(), e);
            return false;
        }
    }


}
