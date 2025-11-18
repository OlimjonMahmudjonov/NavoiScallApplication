package uz.tenzorsoft.scaleapplication.ui;

import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.service.AttachService;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.TruckPhotoService;

import java.io.IOException;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.CAMERA_1;

@Component
@RequiredArgsConstructor
public class CameraViewController implements BaseController {

    private final AttachService attachService;
    private final LogService logService;
    private final TruckPhotoService truckPhotoService;

    public AttachResponse takePicture(String cameraIpAddress) {
        System.out.println("Saving truck number: " + truckNumber + " from Camera 1");

        String username = "admin";
        String password = "Joe@252544";

        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(cameraIpAddress, 80),
                new UsernamePasswordCredentials(username, password.toCharArray())
        );

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {

            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Connection", "close");

            int maxRetries = 3;
            int retryDelay = 1000;

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                System.out.println("Urinish: " + attempt);
                try {
                    if (isTesting) {
                        if (cameraIpAddress.equals(CAMERA_1)) {
                            return attachService.getTestingImages();
                        }
                        logService.save(new LogEntity(5L, truckNumber, "TEST REJIMI: Kamera so'rovi o'tkazib yuborildi"));
                        return attachService.getCameraImgTesting();
                    }

                    String url = "http://" + cameraIpAddress + "/ISAPI/Streaming/channels/1/picture";
                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        byte[] fileBytes = response.getBody();
                        if (fileBytes == null || fileBytes.length == 0) {
                            logService.save(new LogEntity(5L, truckNumber, "00028: Kamera bo'sh rasm yubordi"));
                            continue;
                        }

                        if (currentTruck == null || currentTruck.getTruckNumber() == null || currentTruck.getTruckNumber().trim().isEmpty()) {
                            logService.save(new LogEntity(5L, truckNumber, "00028: Truck raqami yo'q yoki bo'sh"));
                            continue;
                        }

                        AttachResponse saved = attachService.saveToSystem(fileBytes);
                        if (saved != null && saved.getPath() != null && saved.getId() != null) {
                            logService.save(new LogEntity(5L, currentTruck.getTruckNumber(),
                                    "Rasm S3 ga yuklandi: " + saved.getPath() + ", AttachID: " + saved.getId()));
                            truckPhotoService.addEntrancePhoto(currentTruck.getId(), saved.getId());
                            System.out.println("Rasm S3 ga yuklandi: " + saved.getPath());
                            return saved;
                        } else {
                            logService.save(new LogEntity(5L, truckNumber, "00028: Attach saqlanmadi yoki ID yo'q: " + (saved == null ? "null" : "ID=null")));
                        }
                    } else {
                        logService.save(new LogEntity(5L, truckNumber, "00028: Kamera javobi: " + response.getStatusCode()));
                    }

                } catch (HttpClientErrorException.Unauthorized ex) {
                    logService.save(new LogEntity(5L, truckNumber, "00028: 401 Unauthorized - Kamera login/parol xato"));
                    if (attempt == maxRetries) {
                        showAlert(Alert.AlertType.ERROR, "Autentifikatsiya xatosi", "Kamera login/parol noto'g'ri: " + cameraIpAddress);
                    }
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, truckNumber, "00028: Urinish " + attempt + " xato: " + e.getMessage()));
                    if (attempt == maxRetries) {
                        showAlert(Alert.AlertType.ERROR, "Kamera xatosi", "Rasm olishda xatolik: " + e.getMessage());
                        return null;
                    }
                }

                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (IOException e) {
            logService.save(new LogEntity(5L, truckNumber, "00028: HttpClient yopishda xato: " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Kamera xatosi", "Ulanish yopildi: " + e.getMessage());
        }

        logService.save(new LogEntity(5L, truckNumber, "00028: Barcha urinishlar muvaffaqiyatsiz"));
        showAlert(Alert.AlertType.WARNING, "Kamera ogohlantirish", "Rasm olinmadi: " + cameraIpAddress);
        return null;
    }
}