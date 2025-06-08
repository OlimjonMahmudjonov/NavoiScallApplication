package uz.tenzorsoft.scaleapplication.controller;

import jakarta.servlet.http.HttpServletRequest;
import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.service.AttachService;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.ui.BaseController;
import uz.tenzorsoft.scaleapplication.ui.ButtonController;
import uz.tenzorsoft.scaleapplication.ui.TableController;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

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

    @Autowired
    @Lazy
    private TableController tableController;

    @Autowired
    @Lazy
    private ButtonController buttonController;

    @PostMapping(value = "/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(HttpServletRequest request, @PathVariable("id") Integer cameraId) {
        log.info("=== REQUEST KELDI, CAMERA ID: {} ===", cameraId);
        if (request instanceof MultipartHttpServletRequest multipartRequest && cameraId == 1) {
            System.out.println("Request is processing " + cameraId);
            System.out.println("Camera id: " + cameraId);
            System.out.println("multipartRequest.getFileMap().size() = " + multipartRequest.getFileMap().size());
            System.out.println("truckPosition " + truckPosition + " currentUser.getId(): " + currentUser.getId() + " isWaiting: " + isWaiting);
//            System.out.println("truckExitPosition " + truckExitPosition + " currentUser.getId(): " + currentUser.getId() + " isWaiting: " + isExitWaiting);

            if (multipartRequest.getFileMap().size() < 3 && truckPosition != -1 && isWaiting) {
                System.out.println("NOT_MATCH");
                return ResponseEntity.ok("NOT_MATCH");
            }

            AttachResponse attachResponse = new AttachResponse();

            for (Map.Entry<String, MultipartFile> entry : multipartRequest.getFileMap().entrySet()) {
                String fileName = entry.getKey();
                MultipartFile file = entry.getValue();
                System.out.println("fileName = " + fileName);
                System.out.println("File processing");

                if (file.isEmpty()) {
                    log.warn("File is empty: {}", fileName);
                    continue;
                }

                try {
                    if (fileName.equals("anpr.xml")) {
                        truckNumber = extractNumberFromXmlFile(file);
                        System.out.println("truckNumber = " + truckNumber);
//                        truckExitNumber = extractNumberFromXmlFile(file);
//                        System.out.println("truckNumber = " + truckNumber);
//                        System.out.println("truckExitNumber = " + truckExitNumber);

                        if (truckNumber == null || truckNumber.trim().isEmpty() || truckNumber.equalsIgnoreCase("unknown")) {
                            showAlert(Alert.AlertType.ERROR, "Xatolik", "Moshina raqami aniqlanmadi");
                            return ResponseEntity.ok("-NOT_MATCH"); // isWaiting hali true qilinmagan
                        }

                        // API dan kelgan ma'lumotlar bilan taqqoslash
                        if (!isAuthorizedTruck(truckNumber)) {
                            log.warn("Truck not found in API data: {}", truckNumber);
                            logService.save(new LogEntity(5L, truckNumber, "API da topilmadi: " + truckNumber));
                            showAlert(Alert.AlertType.WARNING, "Ogohlantirish", "Bu mashina rasmiylashtirilmagan : " + truckNumber);
                            return ResponseEntity.ok("NOT_AUTHORIZED"); // isWaiting hali true qilinmagan
                        }

                        if (!truckNumber.isEmpty()) {
                            isWaiting = true; // ← FAQAT SHU YERDA TRUE QILAMIZ (number topilganda)
                            System.out.println("Tarozi waiting for " + truckNumber);

                            if (!truckService.isValidTruckNumber(truckNumber)) {
                                log.warn("Truck number does not match: {}", truckNumber);
                                truckNumber = "";
                                isWaiting = false; // ← XATOLIK: false qilamiz
                                return ResponseEntity.ok("NOT_MATCH");
                            }

                            if (cameraId == 1) {
                                if (!truckService.isEntranceAvailableForCamera1(truckNumber)) {
                                    logService.save(new LogEntity(5L, truckNumber, "00001: (CameraController) Chiqishi topilmadi" + truckNumber));
                                    System.err.println("Chiqishi topilmadi" + truckNumber);
                                    isWaiting = false; // ← XATOLIK: false qilamiz
                                    return ResponseEntity.ok("Entrance exception");
                                }
                            }
//                            else {
//                                if (!truckService.isEntranceAvailableForCamera2(truckNumber)) {
//                                    logService.save(new LogEntity(5L, truckNumber, "00002: (CameraController) Kirishi topilmadi" + truckNumber));
//                                    System.err.println("Kirishi topilmadi" + truckNumber);
//                                    isWaiting = false; // ← XATOLIK: false qilamiz
//                                    return ResponseEntity.ok("Entrance exception");
//                                }
//                            }
                        }
                    }

                    if (fileName.contains("detectionPicture")) {
                        attachResponse = attachService.saveToSystem(file);
                        if (attachResponse == null) {
                            logService.save(new LogEntity(5L, truckNumber, "00004: (CameraController) Unable to save file"));
                            log.warn("See logs for error cause. Unable to save file: {}", fileName);
                            showAlert(Alert.AlertType.ERROR, "Xatolik", "Rasmni saqlashda xatolik");
                            isWaiting = false; // ← XATOLIK: false qilamiz
                            return ResponseEntity.ok("Unable to save file");
                        }

                        if (cameraId == 1) {
                            System.out.println("saving image " + cameraId);
                            currentTruck.getAttaches().add(new AttachIdWithStatus(attachResponse.getId(), AttachStatus.ENTRANCE_PHOTO));
                        }
//                        else if (cameraId == 2) {
//                            System.out.println("saving image 2");
//                            currentTruck.getAttaches().add(new AttachIdWithStatus(attachResponse.getId(), AttachStatus.EXIT_PHOTO));
//                        }
                    }
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, truckNumber, "00006: (CameraController) " + e.getMessage()));
                    log.warn("File processing failed: {}", fileName);
                    log.error(e.getMessage(), e);
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                    System.out.println(e.getMessage());
                    isWaiting = false; // ← XATOLIK: false qilamiz
                    return ResponseEntity.status(200).body("Failed to save file: " + file.getOriginalFilename());
                }
            }

            System.out.println("Camera id: " + cameraId + " truckNumber=" + truckNumber);
            currentTruck.setTruckNumber(truckNumber);
//            currentExitTruck.setTruckNumber(truckExitNumber);

            if (truckNumber == null || truckNumber.trim().isEmpty() || truckNumber.equalsIgnoreCase("unknown")) {
                showAlert(Alert.AlertType.ERROR, "Xatolik", "Moshina raqami aniqlanmadi");
                isWaiting = false; // ← XATOLIK: false qilamiz
                return ResponseEntity.ok("_NOT_MATCH");
            }

            try {
                if (cameraId == 1) {
                    if (buttonController.openGate1(0)) {
                        currentTruck.setEnteredStatus(TruckAction.ENTRANCE);
                        firstGateEntranceTime = System.currentTimeMillis();
//                        updateTruckWithApiData(currentTruck, truckNumber);
                        truckService.saveTruck(currentTruck, cameraId, attachResponse);
                        tableController.addLastRecord();
                        System.out.println("Opening gate 1 for truck: " + truckNumber);
//                        isWaiting = false; // ← MUVAFFAQIYATLI YAKUNLANDI: false qilamiz
                    } else {
                        System.err.println("Unable to open gate 1");
                        showAlert(Alert.AlertType.ERROR, "Error", "Unable to open gate 1");
                        currentTruck = new TruckResponse();
                        isWaiting = false; // ← XATOLIK: false qilamiz
                    }
                }
//                if (cameraId == 2) {
//                    if (buttonController.openExitGate1(0)) {
//                        currentExitTruck.setEnteredStatus(TruckAction.EXIT);
//                        firstExitGateEntranceTime = System.currentTimeMillis();
//                        updateTruckWithApiData(currentExitTruck, truckExitNumber);
//                        truckService.saveTruck(currentExitTruck, cameraId, attachResponse);
//                        tableController.addLastRecord();
//                        System.out.println("Opening Exit gate 1 for authorized truck: " + truckExitNumber);
////                        isWaiting = false; // ← MUVAFFAQIYATLI YAKUNLANDI: false qilamiz
//                    } else {
//                        System.err.println("Unable to open gate 1");
//                        showAlert(Alert.AlertType.ERROR, "Error", "Unable to open gate 1");
//                        currentExitTruck = new TruckResponse();
//                        isWaiting = false; // ← XATOLIK: false qilamiz
//                    }
//                }
            } catch (Exception e) {
                logService.save(new LogEntity(5L, truckNumber, "00047: (" + getClass().getName() + ") " + e.getMessage()));
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                e.printStackTrace();
                isWaiting = false; // ← XATOLIK: false qilamiz
                return ResponseEntity.status(200).body("Error occurred: " + e.getMessage());
            }

            return ResponseEntity.ok("Files uploaded and saved successfully.");
        }

        // Camera 2 uchun ham xuddi shunday mantiq
        if (request instanceof MultipartHttpServletRequest multipartRequest && cameraId == 2) {
            System.out.println("Request is processing " + cameraId);
            System.out.println("Camera id: " + cameraId);
            System.out.println("multipartRequest.getFileMap().size() = " + multipartRequest.getFileMap().size());
            System.out.println("truckExitPosition " + truckExitPosition + " currentUser.getId(): " + currentUser.getId() + " isExitWaiting: " + isExitWaiting);

            if (multipartRequest.getFileMap().size() < 3 && truckExitPosition != -1 && isExitWaiting) {
                System.out.println("NOT_MATCH");
                return ResponseEntity.ok("NOT_MATCH");
            }

            AttachResponse attachResponse = new AttachResponse();

            for (Map.Entry<String, MultipartFile> entry : multipartRequest.getFileMap().entrySet()) {
                String fileName = entry.getKey();
                MultipartFile file = entry.getValue();
                System.out.println("fileName = " + fileName);
                System.out.println("File processing");

                if (file.isEmpty()) {
                    log.warn("File is empty: {}", fileName);
                    continue;
                }

                try {
                    if (fileName.equals("anpr.xml")) {
                        truckExitNumber = extractNumberFromXmlFile(file);
                        System.out.println("truckNumber = " + truckExitNumber);

                        if (truckExitNumber == null || truckExitNumber.trim().isEmpty() || truckExitNumber.equalsIgnoreCase("unknown")) {
                            showAlert(Alert.AlertType.ERROR, "Xatolik", "Moshina raqami aniqlanmadi");
                            return ResponseEntity.ok("-NOT_MATCH"); // isExitWaiting hali true qilinmagan
                        }

                        if (!truckExitNumber.isEmpty()) {
                            isExitWaiting = true; // ← FAQAT SHU YERDA TRUE QILAMIZ (number topilganda)
                            System.out.println("waiting for " + truckExitNumber);

                            if (!truckService.isValidTruckNumber(truckExitNumber)) {
                                log.warn("Truck number does not match: {}", truckExitNumber);
                                truckExitNumber = "";
                                isExitWaiting = false; // ← XATOLIK: false qilamiz
                                return ResponseEntity.ok("NOT_MATCH");
                            }

//                            if (!truckService.isEntranceAvailableForCamera2(truckExitNumber)) {
//                                logService.save(new LogEntity(5L, truckExitNumber, "00002: (CameraController) Kirishi topilmadi" + truckExitNumber));
//                                System.err.println("Kirishi topilmadi" + truckExitNumber);
//                                isExitWaiting = false; // ← XATOLIK: false qilamiz
//                                return ResponseEntity.ok("Entrance exception");
//                            }
                        }
                    }

                    if (fileName.contains("detectionPicture")) {
                        attachResponse = attachService.saveToSystem(file);
                        if (attachResponse == null) {
                            logService.save(new LogEntity(5L, truckExitNumber, "00004: (CameraController) Unable to save file"));
                            log.warn("See logs for error cause. Unable to save file: {}", fileName);
                            showAlert(Alert.AlertType.ERROR, "Xatolik", "Rasmni saqlashda xatolik");
                            isExitWaiting = false; // ← XATOLIK: false qilamiz
                            return ResponseEntity.ok("Unable to save file");
                        }

                        System.out.println("saving image 2");
                        currentExitTruck.getAttaches().add(new AttachIdWithStatus(attachResponse.getId(), AttachStatus.EXIT_PHOTO));
                    }
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, truckExitNumber, "00006: (CameraController) " + e.getMessage()));
                    log.warn("File processing failed: {}", fileName);
                    log.error(e.getMessage(), e);
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                    System.out.println(e.getMessage());
                    isExitWaiting = false; // ← XATOLIK: false qilamiz
                    return ResponseEntity.status(200).body("Failed to save file: " + file.getOriginalFilename());
                }
            }

            System.out.println("Camera id: " + cameraId + " truckNumber=" + truckExitNumber);
            currentExitTruck.setTruckNumber(truckExitNumber);

            if (truckExitNumber == null || truckExitNumber.trim().isEmpty() || truckExitNumber.equalsIgnoreCase("unknown")) {
                showAlert(Alert.AlertType.ERROR, "Xatolik", "Moshina raqami aniqlanmadi");
                isExitWaiting = false; // ← XATOLIK: false qilamiz
                return ResponseEntity.ok("_NOT_MATCH");
            }

            try {
                if (cameraId == 2) {
                    if (buttonController.openExitGate1(0)) {
                        currentExitTruck.setEnteredStatus(TruckAction.EXIT);
                        firstExitGateEntranceTime = System.currentTimeMillis();
                        updateTruckWithApiData(currentExitTruck, truckExitNumber);
                        truckService.saveTruck(currentExitTruck, cameraId, attachResponse);
                        tableController.addLastRecord();
                        System.out.println("Opening Exit gate 1 for authorized truck: " + truckExitNumber);
//                        isExitWaiting = false; // ← MUVAFFAQIYATLI YAKUNLANDI: false qilamiz
                    } else {
                        System.err.println("Unable to open gate 1");
                        showAlert(Alert.AlertType.ERROR, "Error", "Unable to open gate 1");
                        currentExitTruck = new TruckResponse();
                        isExitWaiting = false; // ← XATOLIK: false qilamiz
                    }
                }
            } catch (Exception e) {
                logService.save(new LogEntity(5L, truckExitNumber, "00047: (" + getClass().getName() + ") " + e.getMessage()));
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                e.printStackTrace();
                isExitWaiting = false; // ← XATOLIK: false qilamiz
                return ResponseEntity.status(200).body("Error occurred: " + e.getMessage());
            }

            return ResponseEntity.ok("Files uploaded and saved successfully.");
        }

        log.warn("Request is not a multipart request");
        showAlert(Alert.AlertType.ERROR, "Error", "Request is not a multipart request");
        return ResponseEntity.ok().body("Request is not a multipart request");
    }
    /**
     * API dan kelgan ma'lumotlar bilan truck raqamini tekshirish
     */
    private boolean isAuthorizedTruck(String truckNumber) {
        try {
            TruckEntity truck = truckService.findByTruckNumber(truckNumber);
            return truck != null;
        } catch (Exception e) {
            log.error("Error checking truck authorization: {}", e.getMessage());
            return false;
        }
    }


    /**
     * API dan kelgan ma'lumotlar bilan truck ma'lumotlarini yangilash
     */
    private void updateTruckWithApiData(TruckResponse truckResponse, String truckNumber) {
        try {
            TruckEntity existingTruck = truckService.findByTruckNumber(truckNumber);
            if (existingTruck != null) {
                // API dan kelgan qo'shimcha ma'lumotlarni TruckResponse ga qo'shish
                // Bu yerda siz TruckResponse da yangi fieldlar qo'shishingiz kerak bo'ladi
                log.info("Truck ma'lumotlari topildi: {} - Driver: {}, Product: {}",
                        truckNumber, existingTruck.getDriverName(), existingTruck.getProducts().getName());
            }
        } catch (Exception e) {
            log.error("Error updating truck with API data: {}", e.getMessage());
        }
    }

    private String extractNumberFromXmlFile(MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("originalLicensePlate");
            if (nodeList.getLength() > 0) {
                Element element = (Element) nodeList.item(0);
                return element.getTextContent();
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logService.save(new LogEntity(5L, truckNumber, "00007: (CameraController) " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            log.error(e.getMessage(), e);
        }
        return null;
    }

}