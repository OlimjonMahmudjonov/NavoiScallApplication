package uz.tenzorsoft.scaleapplication.ui;

import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.service.AttachService;
import uz.tenzorsoft.scaleapplication.service.LogService;

import java.util.Arrays;
import java.util.Base64;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.CAMERA_2;

@Component
@RequiredArgsConstructor
public class CameraViewController implements BaseController {
    private final AttachService attachService;
    private final LogService logService;


//    @FXML
//    private ImageView camera1;
//
//    @FXML
//    private ImageView camera2;
//
//    @FXML
//    private ImageView camera3;
//

    private void loadCameraMenu() {

    }

//    public AttachResponse takePicture(String cameraIpAddress) {
//        String username = "admin";
//        String password = "Joe@252544";
//        String auth = username + ":" + password;
//        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Basic " + encodedAuth);
//
//        try {
//            if (isTesting) {
//                if (cameraIpAddress.equals(CAMERA_2)) return attachService.getTestingImages();
//                return attachService.getCameraImgTesting();
//            }
//            String url = "http://" + cameraIpAddress + "/ISAPI/Streaming/channels/1/picture";
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
//            if (response.getStatusCode() == HttpStatus.OK) {
//                byte[] fileBytes = response.getBody();
//                if (fileBytes != null && currentTruck.getTruckNumber() != null) {
//                    return attachService.saveToSystem(fileBytes);
//                }
//            } else {
//                System.out.println("Failed to get snapshot, status: " + response.getStatusCode());
//            }
//        } catch (Exception e) {
//            showAlert(Alert.AlertType.ERROR, "Error while taking picture", e.getMessage());
//            logService.save(new LogEntity(5L, truckNumber, "00029: (" + getClass().getName() + ") " +e.getMessage()));
//        }
//        return null;
//    }

    public AttachResponse takePicture(String cameraIpAddress) {
        System.out.println("Kamera IP: " + cameraIpAddress);
        String username = "admin";
        String password = "Joe@252544";
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Connection", "close");
        System.out.println("Authorization " + encodedAuth);


        int maxRetries = 3;
        int retryDelay = 1000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            System.out.println("Retrying " + attempt);
            try {

                if (isTesting) {
                    if (cameraIpAddress.equals(CAMERA_2)) return attachService.getTestingImages();
                    return attachService.getCameraImgTesting();
                }

                String url = "http://" + cameraIpAddress + "/ISAPI/Streaming/channels/2/picture";
              //  String url = "http://" + cameraIpAddress + "/ISAPI/Traffic/channels/1/picture";
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

                System.out.println("Kamera Response: " + response);
                System.out.println("Kamera Status: " + response.getStatusCode());

                if (response.getStatusCode() == HttpStatus.OK) {
                    byte[] fileBytes = response.getBody();
                    System.out.println("Current Truck: " + currentTruck);
//                    System.out.println("FileBytes: " + Arrays.toString(fileBytes));
                    System.out.println("Truck number: " + currentTruck.getTruckNumber());
                    if (fileBytes != null && currentTruck.getTruckNumber() != null) {
                        return attachService.saveToSystem(fileBytes);
                    }
                    System.out.println("File Bytes Null bo'lgani uchun muammo: FileBytes: " + (fileBytes == null ? null : "malumot bor:") + " Truck Number: " + currentTruck.getTruckNumber());
                } else {
                    System.out.println("Rasmga ololmadi ...");
                    logService.save(new LogEntity(5L, truckNumber, "Failed to get snapshot, status: " + response.getStatusCode()));
                }
            } catch (Exception e) {
                logService.save(new LogEntity(5L, truckNumber,
                        "Attempt " + attempt + " failed: " + e.getMessage()));


//                if (attempt == maxRetries) {
//                    showAlert(Alert.AlertType.ERROR, "Error while taking picture",
//                            "Failed after " + maxRetries + " attempts: " + e.getMessage());
//                    throw new RuntimeException("Failed to take picture after " + maxRetries + " attempts", e);
//                }close


                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }

        System.out.println("Null return ishlayapti: ");
        return null;
    }
}
