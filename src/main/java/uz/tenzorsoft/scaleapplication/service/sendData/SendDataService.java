package uz.tenzorsoft.scaleapplication.service.sendData;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uz.tenzorsoft.scaleapplication.domain.CustomMultipartFile;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.*;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.*;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.*;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.mycoal.*;
import uz.tenzorsoft.scaleapplication.service.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.MYCOAL_SCALE_ID;
import static uz.tenzorsoft.scaleapplication.domain.Settings.SCALE_WEB_ID;

@Service
@RequiredArgsConstructor
public class SendDataService {

    private final TruckService truckService;
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final CargoService cargoService;
    private final AttachService attachService;
    private final TruckActionService truckActionService;
    private final LogService logService;
    private final ProductService productService;

    public void sendNotSentData() {
        List<WebViewDto> notSentTruckData = truckService.getNotSentDataNew();

        if (notSentTruckData.isEmpty()) {
            return;
        }

        List<AttachmentDto> notSentAttachData = attachService.getNotSentDataNew();

        if (notSentAttachData.isEmpty()) {
            return;
        }

        System.out.println("Tarozi webga jo'natilmoqda url: https://api-scale.mycoal.uz/getAllController/getAllLocalAndServerIds");
        GetAllLocalServerIds body = restTemplate.postForObject(
                "https://api-scale.mycoal.uz/getAllController/getAllLocalAndServerIds",
                AllSendResponse.builder()
//                        .attachmentDto(notSentAttachData)
                        .webViewDto(notSentTruckData)
                        .build(), GetAllLocalServerIds.class
        );
        if (body == null) {
            return;
        }
        System.out.println("Malumotlar jo'natildi");
        System.out.println("Not Send data = " + body);

        truckService.dataSentNew(notSentTruckData, body.getWebView());
        System.out.println(body);


        attachService.dataSentNew(notSentAttachData, body.getViewAttachment(), false);

        notSentAttachData = attachService.getNotSentDataNew();
        body = restTemplate.postForObject(
                "https://api-scale.mycoal.uz/getAllController/getAllLocalAndServerIds",
                AllSendResponse.builder()
                        .attachmentDto(notSentAttachData)
                        .build(), GetAllLocalServerIds.class, LocalAndServerIds.class
        );

        if (body == null) {
            System.out.println("body = " + body);
            return;
        }
        attachService.dataSentNew(notSentAttachData, body.getViewAttachment(), true);
    }


    public void sendStatuses() {
        try {
            StatusResponse statusResponse = new StatusResponse(
                    isConnected, gate1Connection, gate2Connection, kppgate1Connection, kppgate2Connection, camera1Connection, camera2Connection,
                    camera3Connection, sensor1Connection, sensor2Connection, sensor3Connection, currentUser.getInternalScaleId()
            );

            RestTemplate restTemplate = new RestTemplate();
            HttpStatusCode statusCode = restTemplate.postForEntity(
                    SERVER_URL + "shlagbaun/check",
                    statusResponse, Void.class
            ).getStatusCode();
            boolean error = statusCode.isError();
            if (error) {
                System.err.println("Error occurred with status code: " + statusCode);
            }

        } catch (Exception e) {
            logService.save(new LogEntity(5L, truckNumber, "00016: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
            System.err.println("Unable to send data");
        }
    }

    public void sendDataToMyCoal() {
        if (MYCOAL_SCALE_ID == 0) {
            System.out.println("Settingdan mycoal tarozi id ni kiriting .....");
            return;
        }
        System.out.println("Ma'lumotlarni mycoalga jo'natadigan metod ishlayapti ...");
        System.out.println("My coal scale id: " + MYCOAL_SCALE_ID);

        List<MyCoalData> request = new ArrayList<>();

        List<TruckEntity> trucks = truckService.findNotSentDataToMyCoal();

        if (trucks.isEmpty()) {
            System.out.println("Hamma malumotlar mycoalga jo'natilgan");
            return;
        }

        for (TruckEntity truckEntity : trucks) {
            if (!isSendAvailable(truckEntity)) continue;
            CargoEntity cargo = cargoService.findByTruckId(truckEntity.getId());
            MyCoalData myCoalData = new MyCoalData();
            TruckActionEntity exitedWeigh = new TruckActionEntity();
            TruckActionEntity noneAction = new TruckActionEntity();
            TruckActionEntity enteredWeigh = new TruckActionEntity();

            for (TruckActionEntity action : truckEntity.getTruckActions()) {

                if (action.getAction() == null) {
                    noneAction = action;
                    continue;
                }

                if (action.getAction().equals(TruckAction.EXIT) || action.getAction().equals(TruckAction.MANUAL_EXIT)) {
                    exitedWeigh = action;
                    continue;
                }

                if (action.getAction().equals(TruckAction.ENTRANCE) || action.getAction().equals(TruckAction.MANUAL_ENTRANCE)) {
                    enteredWeigh = action;
                    continue;
                }
            }


            myCoalData.setLocalId(truckEntity.getId());
//            myCoalData.setNp(0L);
            myCoalData.setScaleId(MYCOAL_SCALE_ID);
//            myCoalData.setRfid("");
//            myCoalData.setAvto_number(truckEntity.getTruckNumber());
//            myCoalData.setFul_name("");
//            myCoalData.setTex_pass_number("");
//            myCoalData.se("");
//            myCoalData.setOrg_name_seller(cargo == null ? "" : cargo.getScaleName());
//            myCoalData.setProduct(new ProductResponse());
//            myCoalData.setCheck(new CheckResponse(
//                    getLocalDateTime(enteredWeigh), getLocalDateTime(exitedWeigh)
//            ));
//            myCoalData.setAccord(new AccordResponse(null, LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
//            myCoalData.setDoverennost(new Doverennost(null, LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            if (noneAction.getId() != null) {
                if (enteredWeigh.getAction() == null) {
                    enteredWeigh = noneAction;
                    enteredWeigh.setAction(TruckAction.ENTRANCE);
                }

                if (exitedWeigh.getAction() == null) {
                    exitedWeigh = noneAction;
                    exitedWeigh.setAction(TruckAction.EXIT);
                }
            }

            double brutto = Math.max(exitedWeigh.getWeight(), enteredWeigh.getWeight());
            double tara = Math.min(exitedWeigh.getWeight(), enteredWeigh.getWeight());
            myCoalData.setBrutto(brutto);
            myCoalData.setTara(tara);
            myCoalData.setNetto(cargo != null ? cargo.getNetWeight() : null);
//            myCoalData.setHeft(new Heft(
//                    brutto, tara, brutto,
//                    cargo != null ? cargo.getNetWeight() : null
//            ));

            request.add(myCoalData);
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<MyCoalData>> entity = new HttpEntity<>(request, headers);

        System.out.println("My coal url: https://api.mycoal.uz/be/api/v1/scales/save-list");
        System.out.println("My coalga jo'natilmoqda");
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                SERVER_URL + "weight/create",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }
        );

        List<Map<String, Object>> body = response.getBody();

        if (body == null) {
//            System.out.println("Bu malumot my coalda mavjud");
            return;
        }

        List<ScalesSaveResponseDTO> dtoList = body.stream().map(data ->
                new ScalesSaveResponseDTO(
                        Long.valueOf(data.get("id").toString()),
                        UUID.fromString(data.get("scaleReadingId").toString()),
                        Boolean.parseBoolean(data.get("status").toString())
                )
        ).toList();

        if (body.toString().length() < 5) return;
        System.out.println("Ma'lumotlar mycoalga jonaitldi !");

        for (TruckEntity truck : trucks) {
            for (TruckActionEntity actionEntity : truck.getTruckActions()) {
                actionEntity.setIsSentToMyCoal(true);
                truckActionService.save(actionEntity);
            }
            truck.setIsSentToMyCoal(true);
            truckService.save(truck);
        }

        System.out.println("Rasmlar jonatish uchun tayyyorlanmoqda ..");

        for (ScalesSaveResponseDTO dto : dtoList) {
            TruckEntity truck = truckService.findById(dto.getId());
            if (truck == null) {
                return;
            }

            List<MultipartFile> multipartFiles = convertToMultipartFile(truck.getTruckPhotos());

            String attachBody = uploadMultipartFiles(multipartFiles, dto.getScaleReadingId());

            System.out.println("My Coal Data attach body = " + attachBody);
            System.out.println("Truck Id: " + truck.getId());

        }

        System.out.println("My Coal Data body = " + body);
    }

    public String uploadMultipartFiles(List<MultipartFile> files, UUID externalId) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (MultipartFile file : files) {
            File tempFile = null;
            try {
                tempFile = File.createTempFile("upload_", file.getOriginalFilename());
                file.transferTo(tempFile);
                body.add("files", new FileSystemResource(tempFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String auth = "coalAPPF:uP8M(F8^($4up=";

        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Basic " + encodedAuth);

        System.out.println("My coal rasmlarni jo'natish uchun url: https://api.mycoal.uz/be/api/v1/scales/fileUpload?externalId=");
        System.out.println("Rasmlar jonatilmoqda ..");
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(SERVER_URL + "weight/addImages" + externalId, HttpMethod.POST, requestEntity, String.class);

        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());
        return response.getBody();
    }

    public List<MultipartFile> convertToMultipartFile(List<TruckPhotosEntity> truckPhotos) {
        List<MultipartFile> multipartFiles = new ArrayList<>();

        for (TruckPhotosEntity attach : truckPhotos) {
            if (attach.getTruckPhoto() == null) continue;
            Path filePath = Paths.get(attach.getTruckPhoto().getPath());

            try {
                byte[] content = Files.readAllBytes(filePath);
                multipartFiles.add(new CustomMultipartFile(
                        attach.getTruckPhoto().getOriginalName(),
                        attach.getTruckPhoto().getFileName(),
                        attach.getTruckPhoto().getContentType(),
                        content
                ));
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + filePath, e);
            }
        }
        return multipartFiles;
    }


    private boolean isSendAvailable(TruckEntity truckEntity) {
        boolean isEntered = false;
        boolean isExited = false;
        for (TruckActionEntity action : truckEntity.getTruckActions()) {
            switch (action.getAction()) {
                case MANUAL_ENTRANCE, ENTRANCE -> isEntered = true;
                case MANUAL_EXIT, EXIT -> isExited = true;
            }
        }
        return isEntered && isExited;
    }

    public void sendLogsToServer() {
        LogResponse request = new LogResponse();
        List<LogEntity> notSentLogs = logService.getNotSentLogs();
        if (notSentLogs.isEmpty()) return;
        request.setDtoList(notSentLogs);
        ResponseEntity<LocalAndServerIds> response = restTemplate.postForEntity(
                "https://api-scale.mycoal.uz/logs/create",
                request, LocalAndServerIds.class
        );
        System.out.println("Log response: " + response);

        Map<Long, Long> data = Objects.requireNonNull(response.getBody()).getData();
        logService.dataSent(notSentLogs, data);

    }

    public void sendProductsToServer() {
        // Jo'natilmagan mahsulotlarni olish
        List<ProductsEntity> notSentProducts = productService.getNotSentProducts();
        if (notSentProducts.isEmpty()) {
            System.out.println("No products to send.");
            return;
        }

        // Birinchi mahsulotni olish va javobni tayyorlash
        ProductsEntity products = notSentProducts.get(0);
        ProductResponses response = new ProductResponses(
                products.getId(),
                products.getName(),
                currentUser.getInternalScaleId()
        );
        System.out.println("Prepared response: " + response);

        // REST so'rovini yuborish
        try {
            // Assuming 'response' is your request body object
            HttpEntity<ProductResponses> requestEntity = new HttpEntity<>(response);

            ResponseEntity<Map<String, Long>> request = restTemplate.exchange(
                    SERVER_URL + "/product/save",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Long>>() {
                    }
            );

            // Javobni tekshirish
            if (request.getStatusCode() == HttpStatus.OK) {
                Map<String, Long> responseBody = request.getBody();
                if (responseBody != null) {
                    System.out.println("Response body: " + responseBody);
                    // Ma'lumotlarni belgilash: muvaffaqiyatli jo'natildi
                    productService.dataSent(notSentProducts, responseBody);
                } else {
                    System.out.println("Response body is null.");
                }
            } else {
                System.out.println("Request failed with status: " + request.getStatusCode());
            }
        } catch (RestClientException e) {
            // Xatolikni log qilish
            System.err.println("Error while sending product: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static String getLocalDateTime(TruckActionEntity enteredWeigh) {
        LocalDateTime createdAt = enteredWeigh.getCreatedAt();
        if (createdAt == null) createdAt = LocalDateTime.now();
        return createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }


}
