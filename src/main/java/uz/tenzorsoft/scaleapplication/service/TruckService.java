package uz.tenzorsoft.scaleapplication.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.*;
import uz.tenzorsoft.scaleapplication.domain.enumerators.ActionStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.request.TruckRequest;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.ActionResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.WebViewDto;
import uz.tenzorsoft.scaleapplication.repository.*;
import uz.tenzorsoft.scaleapplication.sentDataNavoi.*;
import uz.tenzorsoft.scaleapplication.sentDataNavoi.S3service.S3Service;
import uz.tenzorsoft.scaleapplication.ui.MainController;
import uz.tenzorsoft.scaleapplication.ui.TableController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uz.tenzorsoft.scaleapplication.domain.Settings.EXIT_TIMEOUT;
import static uz.tenzorsoft.scaleapplication.domain.Settings.SCALE_WEB_ID;

@Service
@RequiredArgsConstructor
public class TruckService implements BaseService<TruckEntity, TruckResponse, TruckRequest> {
    private static final Logger log = LoggerFactory.getLogger(TruckService.class);
    private final TruckRepository truckRepository;
    private final TruckActionRepository truckActionRepository;
    private final TruckPhotoRepository truckPhotoRepository;
    private final UserService userService;
    private final CargoService cargoService;
    private final LogService logService;
    private final ProductRepository productRepository;
    private final RefreshToken refreshToken;
    @Setter
    @Getter
    private TruckEntity currentTruckEntity = new TruckEntity();

    @Setter
    @Getter
    private TruckEntity currentExitTruckEntity = new TruckEntity();
    @Lazy
    @Autowired
    private AttachService attachService;

    @Value("${number.pattern.regexp.number}")
    private String regexPattern;
    @Value("${number.pattern.regexp.standard}")
    private String regexStandard;
    @Autowired
    private ProductService productService;

    @Autowired
    @Lazy
    private MainController mainController;
    @Autowired
    @Lazy
    private TableController tableController;

    @Autowired
    private S3Service s3Service;

    @Value("${spring.url}/navoiyazot-transfers/drivers-with-transfers")
    private String url;

    @Value("${spring.url}/basic/weight/save-in-weight")
    private String url_in;

    @Value("${spring.url}/basic/weight/save-out-weight")
    private String url_out;

    @Value("${spring.url}/basic/weight/save-cam-image")
    private String image_url;


    @Value("${spring.basic-auth.login}")
    private String login;

    @Value("${spring.basic-auth.password}")
    private String password;

    @Value("${spring.token}")
    private String token;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CargoRepository cargoRepository;
    @Autowired
    private AttachRepository attachRepository;

    // kamayish bo`yicha  truckni  ma`lumotlani olib chiqish
    public List<TableViewData> getTruckData() {
        List<TruckEntity> all = truckRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<TableViewData> data = new ArrayList<>();
        for (TruckEntity truck : all) {
            data.add(entityToTableViewData(truck));
        }
        return data;
    }

    // kirish  chiqish  truck larni  statusga  qarab   saqlash
    @Transactional
    public TruckEntity save(TruckResponse truck) {
        TruckEntity truckEntity = truckRepository.findTruckWithEntranceNoExit(truck.getTruckNumber()).orElse(new TruckEntity());

        if (truckEntity.getId() != null) {
            TruckActionEntity truckAction = new TruckActionEntity(
                    truck.getExitedWeight(), TruckAction.EXIT, ActionStatus.NEW, userService.findByPhoneNumber(truck.getExitConfirmedBy()), false
            );
            truckEntity.getTruckActions().add(truckAction);
            return truckRepository.save(truckEntity);
        }

        List<TruckActionEntity> truckActions = new ArrayList<>();
        truckActions.add(new TruckActionEntity(truck.getEnteredWeight(),
                TruckAction.ENTRANCE, ActionStatus.NEW, userService.findByPhoneNumber(truck.getEntranceConfirmedBy()), false)
        );
        truckActions.add(new TruckActionEntity(truck.getExitedWeight(),
                TruckAction.EXIT, ActionStatus.NEW, userService.findByPhoneNumber(truck.getEntranceConfirmedBy()), false)
        );

        truckActionRepository.saveAll(truckActions);

        List<TruckPhotosEntity> truckPhotos = new ArrayList<>();

        truck.getAttaches().forEach(attach -> {
            truckPhotos.add(new TruckPhotosEntity(
                    attachService.findById(attach.getId()), attach.getStatus()
            ));
        });

        Set<Long> attachIds = new HashSet<>();
        System.out.println("truck.getAttaches().size() = " + truck.getAttaches().size());
        truck.getAttaches().forEach(attach -> {
            if (!attachIds.contains(attach.getId())) { // Check for uniqueness
                System.out.println("attach.getId() = " + attach.getId());
                truckPhotos.add(truckPhotoRepository.save(new TruckPhotosEntity(
                        attachService.findById(attach.getId()), attach.getStatus()
                )));
                attachIds.add(attach.getId()); // Add to the set to track uniqueness
            }
        });
        System.out.println("saved truck photo");


        truckEntity.setTruckNumber(truck.getTruckNumber());
        truckEntity.setTruckPhotos(truckPhotos);
        truckEntity.setTruckActions(truckActions);
        return truckRepository.save(truckEntity);
    }

    // hali  serverga  yuborilmagan ma`lumotlani  yuborish uchun
    public List<ActionResponse> getNotSentData() {
        List<ActionResponse> result = new ArrayList<>();
        List<TruckEntity> notSentData = truckRepository.findTop10ByIsSentToCloud(false);
//        if (!notSentData.isEmpty()) {
//            notSentData = List.of(notSentData.get(0));
//        }
        for (TruckEntity truck : notSentData) {
            ActionResponse actionResponse = new ActionResponse();
            List<Long> attachIds = truck.getTruckPhotos().stream().map(
                    act -> act.getTruckPhoto().getId()
            ).toList();
            actionResponse.setId(truck.getId());
            actionResponse.setAttachIds(attachIds);
            actionResponse.setTruckNumber(truck.getTruckNumber() == null ? "" : truck.getTruckNumber());
            for (TruckActionEntity action : truck.getTruckActions()) {
                switch (action.getAction()) {
                    case ENTRANCE, MANUAL_ENTRANCE -> {
                        actionResponse.setEnteredStatus(action.getAction());
                        actionResponse.setEnteredAt(action.getCreatedAt());
                        actionResponse.setEnteredWeight(action.getWeight());
                        actionResponse.setEntranceConfirmedBy(action.getOnDuty().getPhoneNumber());
                    }
                    case EXIT, MANUAL_EXIT -> {
                        actionResponse.setExitedStatus(action.getAction());
                        actionResponse.setExitedAt(action.getCreatedAt());
                        actionResponse.setExitedWeight(action.getWeight());
                        actionResponse.setExitConfirmedBy(action.getOnDuty().getPhoneNumber());
                    }
                }
            }
            actionResponse.setIdOnServer(truck.getIdOnServer());
            result.add(actionResponse);
        }
        return result;
    }

    /// bunda  web ga ma`lumot  jo`natish uchun
    public List<WebViewDto> getNotSentDataNew() {
        List<WebViewDto> result = new ArrayList<>();
        List<TruckEntity> notSentData = truckRepository.findTop10ByIsSentToCloud(false);
        for (TruckEntity truck : notSentData) {
            WebViewDto webViewDto = new WebViewDto();
            webViewDto.setLocalId(truck.getId());
            webViewDto.setScaleId(SCALE_WEB_ID);
            webViewDto.setCarNumber(truck.getTruckNumber());
            ActionStatus enteredStatus = null;
            ActionStatus exitedStatus = null;
            for (TruckActionEntity action : truck.getTruckActions()) {
                switch (action.getAction()) {
                    case ENTRANCE, MANUAL_ENTRANCE -> {
                        enteredStatus = action.getActionStatus();
                        webViewDto.setEnteredAt(action.getCreatedAt());
                        webViewDto.setEnterWeight(action.getWeight());
                        webViewDto.setResponsiblePerson(action.getOnDuty().getPhoneNumber());
                    }
                    case EXIT, MANUAL_EXIT -> {
                        exitedStatus = action.getActionStatus();
                        webViewDto.setExitedAt(action.getCreatedAt());
                        webViewDto.setExitWeight(action.getWeight());
                        webViewDto.setExitResponsiblePerson(action.getOnDuty().getPhoneNumber());
                    }
                }
            }

            // Ichkarida bo'lsa (faqat kirish completed bo'lsa, lekin chiqish yo'q yoki hali bo'lmagan)
            if (enteredStatus != null && enteredStatus.equals(ActionStatus.COMPLETE) && exitedStatus == null) {
                result.add(webViewDto);
                continue;
            }

            // Agar ham kirish, ham chiqish complete bo'lsa
            if (enteredStatus != null && enteredStatus.equals(ActionStatus.COMPLETE) &&
                    exitedStatus != null && exitedStatus.equals(ActionStatus.COMPLETE)) {
                result.add(webViewDto);
            }
        }
        return result;
    }


    public void dataSent(List<ActionResponse> notSentData, Map<Long, Long> truckMap) {
        if (truckMap == null || truckMap.isEmpty()) {
            return;
        }
        notSentData.forEach(truck -> {
            TruckEntity entity = truckRepository.findById(truck.getId()).orElseThrow(() -> new RuntimeException(truck.getId() + " is not found from database"));
            entity.setIsSentToCloud(true);
            entity.setIdOnServer(truckMap.get(entity.getId()));
            truckRepository.save(entity);
        });
    }


    public void dataSentNew(List<WebViewDto> notSentData, Map<Long, Long> truckMap) {
        if (truckMap == null || truckMap.isEmpty()) {
            return;
        }
        notSentData.forEach(truck -> {
            Long key = truckMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null && entry.getValue().equals(truck.getLocalId()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            TruckEntity entity = truckRepository.findById(truck.getLocalId()).orElseThrow(() -> new RuntimeException(truck.getLocalId() + " is not found from database"));
            entity.setIsSentToCloud(true);
            entity.setIdOnServer(key);
            truckRepository.save(entity);
        });
    }

    public TableViewData findLastRecord() {
        TruckEntity truck = truckRepository.findTopByOrderByIdDesc().orElse(null);
        if (truck == null) {
            System.err.println("Unable to find last record");
            return null;
        }
        return entityToTableViewData(truck);
    }

    public List<TruckEntity> findAll() {
        return truckRepository.findAllByIsDeleted(false, Sort.by(Sort.Direction.DESC, "id"));
    }

    public TableViewData entityToTableViewData(TruckEntity entity) {
        TableViewData data = new TableViewData();
        data.setId(entity.getId());
        double enteredWeight = 0.0;
        double exitedWeight = 0.0;
        TruckAction entranceAction = TruckAction.NO_ACTION;
        TruckAction exitAction = TruckAction.NO_ACTION;

        for (TruckActionEntity action : entity.getTruckActions()) {
            switch (action.getAction()) {
                case ENTRANCE, MANUAL_ENTRANCE -> {
                    data.setEnteredTruckNumber(entity.getTruckNumber());
                    data.setEnteredDate(getDate(action.getCreatedAt()));
                    data.setEnteredTime(getTime(action.getCreatedAt()));
                    data.setEnteredWeight(action.getWeight());
                    data.setEnteredActionStatus(String.valueOf(action.getActionStatus()));
                    enteredWeight = action.getWeight() == null ? 0.0 : action.getWeight();
                    entranceAction = action.getAction();

                    if (action.getActionStatus() != null) {
                        data.setEnteredActionStatus(action.getActionStatus().name());
                    }
                    if (action.getOnDuty() != null) {
                        data.setEnteredOnDuty(action.getOnDuty().getPhoneNumber());
                    } else {
                        data.setEnteredOnDuty("Unknown"); // Or handle this case differently as needed
                    }
                }
                case MANUAL_EXIT, EXIT -> {
                    data.setExitedTruckNumber(entity.getTruckNumber());
                    data.setExitedDate(getDate(action.getCreatedAt()));
                    data.setExitedTime(getTime(action.getCreatedAt()));
                    data.setExitedWeight(action.getWeight());
                    data.setExitedActionStatus(String.valueOf(action.getActionStatus()));
                    exitedWeight = action.getWeight();
                    exitAction = action.getAction();
                    if (action.getActionStatus() != null) {
                        data.setExitedActionStatus(action.getActionStatus().name());
                    }
                    if (action.getOnDuty() != null) {
                        data.setExitedOnDuty(action.getOnDuty().getPhoneNumber());
                    } else {
                        data.setExitedOnDuty("Unknown"); // Or handle this case differently as needed
                    }
                }
            }
        }
        data.setProductType(entity.getProducts().getName());
        CargoEntity cargo = cargoService.findByTruckId(entity.getId());
        if (cargo == null) return data;
        switch (cargo.getCargoStatus()) {
            case PICKUP -> data.setPickupWeight(String.valueOf(cargo.getNetWeight()));
            case DROP -> data.setDropWeight(String.valueOf(cargo.getNetWeight()));
        }
        data.setEnteredActionStatus(getTruckActionStatus(entity, entranceAction));
        data.setExitedActionStatus(getTruckActionStatus(entity, exitAction));

        data.setMinWeight(String.valueOf(Math.min(enteredWeight, exitedWeight)));
        data.setMaxWeight(String.valueOf(Math.max(enteredWeight, exitedWeight)));
        return data;
    }

    private String getTruckActionStatus(TruckEntity truckEntity, TruckAction truckAction) {

        String truckActionStatus = "";
        for (TruckActionEntity action : truckEntity.getTruckActions()) {
            if (action.getActionStatus() != null &&
                    (action.getAction().equals(truckAction))) {
                truckActionStatus = action.getActionStatus().name();
            }
        }
        return truckActionStatus.toString();
    }

    public String getTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.getHour() + ":" + dateTime.getMinute();
    }

    public String getDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.getDayOfMonth() + "." + dateTime.getMonth().getValue() + "." + dateTime.getYear();
    }

    public TruckEntity save(TruckEntity truck) {
        return truckRepository.save(truck);
    }

    public TruckEntity findById(Long id) {
        return truckRepository.findById(id).orElse(null);
    }

    public TruckEntity findByTruckPhoto(TruckPhotosEntity truckPhotos) {
        return truckRepository.findByTruckPhotosContains(truckPhotos).orElse(null);
    }

    public void deleteTruckById(Long id) {
        try {
            truckRepository.deleteById(id);
            tableController.addLastRecord();
        } catch (Exception e) {
            System.out.println("To'liq bo'lmagan truck datani o'chirishda xatolik.");
        }
    }

    @Transactional
    public void sendNetoWeight(Long truckId) {
        TruckEntity truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new IllegalStateException("Truck Id not found: " + truckId));

        // 1. KIRISH (ENTRANCE) COMPLETE bo‘lishi MUTLAQ SHART!
        boolean hasEntranceComplete = truck.getTruckActions().stream()
                .anyMatch(action ->
                        (action.getAction() == TruckAction.ENTRANCE || action.getAction() == TruckAction.MANUAL_ENTRANCE)
                                && action.getActionStatus() == ActionStatus.COMPLETE
                );

        if (!hasEntranceComplete) {
            String errorMsg = "XATO: Mashina tarozidan o‘tmagan! Kirish COMPLETE emas. Truck: " + truck.getTruckNumber() + " (ID: " + truckId + ")";
            log.error(errorMsg);
            mainController.showAlert(Alert.AlertType.ERROR, "Xatolik", "Bu mashina tarozidan o‘tmagan! Chiqish rad etildi.");
            throw new IllegalStateException(errorMsg);
        }

        // 2. Chiqish (EXIT) COMPLETE bo‘lganini tekshir
        boolean hasExitComplete = truck.getTruckActions().stream()
                .anyMatch(action ->
                        (action.getAction() == TruckAction.EXIT || action.getAction() == TruckAction.MANUAL_EXIT)
                                && action.getActionStatus() == ActionStatus.COMPLETE
                );

        if (!hasExitComplete) {
            log.info("Truck {} chiqish holati COMPLETE emas, brutto yuborilmaydi", truckId);
            return;
        }

        // 3. Cargo va neto vazn
        CargoEntity cargo = cargoRepository.findByTruck(truck).orElse(null);
        if (cargo == null || cargo.getNetWeight() == null || cargo.getNetWeight() <= 0) {
            log.warn("Truck {} uchun neto vazn topilmadi yoki noto'g'ri", truckId);
            return;
        }

        // 4. Tara vazn
        Double tara = truck.getTruckActions().stream()
                .filter(a -> a.getAction() == TruckAction.ENTRANCE || a.getAction() == TruckAction.MANUAL_ENTRANCE)
                .map(TruckActionEntity::getWeight)
                .findFirst()
                .orElse(null);

        if (tara == null || tara <= 0) {
            log.warn("Truck {} uchun tara vazni topilmadi", truckId);
            return;
        }

        Double brutto = tara + cargo.getNetWeight();

        // 5. API dan transfer ID olish (hozir bu yerda ENTERED kerak emas emas, lekin baribir olamiz)
        DriverWithTransfersImport apiData = importInformation(truck.getTruckNumber());
        if (apiData == null || apiData.getTransfers().isEmpty()) {
            log.error("Truck {} uchun transfer ma'lumotlari topilmadi", truck.getTruckNumber());
            return;
        }

        Long navoiyAzotTransferId = apiData.getTransfers().stream()
                .filter(t -> "ENTERED".equalsIgnoreCase(t.getCurrentStatus()))
                .max(Comparator.comparing(TransferImport::getCreatedAt))
                .map(TransferImport::getId)
                .orElse(null);

        if (navoiyAzotTransferId == null) {
            log.warn("Truck {} uchun ENTERED transfer topilmadi – lekin kirish COMPLETE bo‘lgani uchun davom etamiz", truck.getTruckNumber());
            // Bu yerda xatolik tashlamaymiz, chunki kirish allaqachon COMPLETE
            // Agar kerak bo‘lsa, bu yerni qattiq qilish mumkin
        }

        // 6. Brutto yuborish
        Weight_OutDto weightOutDto = new Weight_OutDto();
        weightOutDto.setNavoiyAzotTransferId(navoiyAzotTransferId); // null bo‘lsa ham yuboramiz, backend qabul qiladi deb umid qilamiz
        weightOutDto.setBrutto(brutto);
        weightOutDto.setBruttoTime(LocalDateTime.now());
        weightOutDto.setLocalId(truck.getId());
        weightOutDto.setScaleId(SCALE_WEB_ID);

        sendToNavoiyAzotOut(weightOutDto);

        log.info("Brutto muvaffaqiyatli yuborildi: truckId={}, tara={}, neto={}, brutto={}, transferId={}",
                truckId, tara, cargo.getNetWeight(), brutto, navoiyAzotTransferId);
    }

    private void sendToNavoiyAzotOut(Weight_OutDto weight_outDto) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth( login, password);

        try {
            HttpEntity<Weight_OutDto> entity = new HttpEntity<>(weight_outDto, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url_out,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Neto sent successfully  neto = {} , transferId ={}", weight_outDto.getBrutto(), weight_outDto.getNavoiyAzotTransferId());
            } else {
                log.error("Neto sent something went  wrong ");
            }
        } catch (Exception e) {
            log.error("Neto sent something went  wrong {}", e.getMessage());
        }

    }

    @Transactional
    public void sendTaraWeight(Long truckId) {
        TruckEntity truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new IllegalStateException("Truck Id not found: " + truckId));

        // 1. Truck allaqachon chiqib ketgan bo‘lsa → hech narsa qilmaymiz
        if (truck.getIsFinished() != null && truck.getIsFinished()) {
            log.warn("Truck allaqachon chiqib ketgan (isFinished=true): {}", truckId);
            return;
        }

        // 2. Kirish harakati (ENTRANCE) COMPLETE bo‘lishi shart
        TruckActionEntity enterAction = truck.getTruckActions().stream()
                .filter(a -> (a.getAction() == TruckAction.ENTRANCE || a.getAction() == TruckAction.MANUAL_ENTRANCE)
                        && a.getActionStatus() == ActionStatus.COMPLETE)
                .findFirst()
                .orElseThrow(() -> {
                    String msg = "KRITIK XATO: Kirish COMPLETE emas! Tara yuborib bo‘lmaydi. Truck: " + truck.getTruckNumber();
                    log.error(msg);
                    mainController.showAlert(Alert.AlertType.ERROR, "Xatolik", "Kirish hali yakunlanmagan!");
                    return new IllegalStateException(msg);
                });

        // 3. API dan transfer ma'lumotlarini olish
        DriverWithTransfersImport apiData = importInformation(truck.getTruckNumber());
        if (apiData == null || apiData.getTransfers() == null || apiData.getTransfers().isEmpty()) {
            String msg = "KRITIK XATO: Tashqi API da transfer ma'lumotlari topilmadi! Truck: " + truck.getTruckNumber();
            log.error(msg);
            mainController.showAlert(Alert.AlertType.ERROR, "API Xatosi", "Transfer ma'lumotlari yo‘q!");
            throw new IllegalStateException(msg);
        }

        // 4. "ENTERED" statusli transfer ID ni topish
        Long navoiyAzotTransferId = apiData.getTransfers().stream()
                .filter(t -> "ENTERED".equalsIgnoreCase(t.getCurrentStatus()))
                .max(Comparator.comparing(TransferImport::getCreatedAt))
                .map(TransferImport::getId)
                .orElse(null);

        if (navoiyAzotTransferId == null) {
            String msg = "KRITIK XATO: ENTERED transfer topilmadi! Tara yuborib bo‘lmaydi. Truck: " + truck.getTruckNumber();
            log.error(msg);
            mainController.showAlert(Alert.AlertType.ERROR, "Transfer Xatosi",
                    "Bu mashina uchun ENTERED transfer mavjud emas!\nRaqam: " + truck.getTruckNumber());
            throw new IllegalStateException(msg); // Tranzaksiya rollback bo‘ladi!
        }

        // 5. Tara vaznini yuborish
        Weight_InDto weight = new Weight_InDto();
        weight.setNavoiyAzotTransferId(navoiyAzotTransferId);
        weight.setTara(enterAction.getWeight());
        weight.setTaraTime(LocalDateTime.now());
        weight.setLocalId(truck.getId());
        weight.setScaleId(SCALE_WEB_ID);

        try {
            senToNavoiyAzot(weight);
            log.info("TARA muvaffaqiyatli yuborildi: TruckID={}, Raqam={}, Tara={}, TransferID={}",
                    truckId, truck.getTruckNumber(), enterAction.getWeight(), navoiyAzotTransferId);
        } catch (Exception e) {
            String msg = "Tara yuborishda xatolik: " + e.getMessage();
            log.error(msg, e);
            mainController.showAlert(Alert.AlertType.ERROR, "Yuborish Xatosi", "Tara ma'lumotlari yuborilmadi!");
            throw new RuntimeException(msg, e); // rollback
        }
    }
    private void senToNavoiyAzot(Weight_InDto weight) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(login, password);
        try {
            HttpEntity<Weight_InDto> entity = new HttpEntity<>(weight, headers);
            ResponseEntity<String> response = restTemplate.exchange(url_in, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Send  information localId={} , transferId={}", weight.getLocalId(), weight.getNavoiyAzotTransferId());
            } else {
                log.error("send  information , something went  wrong : {} - {}", response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            log.error("Tara send  data something went wrong : {}", e.getMessage());
        }
    }

    private String cleanTruckNumber(String number) {
        if (number == null) return "";
        return number.trim()
                .toUpperCase()
                .replaceAll("\\s+", ""); // 80 6443 AA → 806443AA
    }

    public DriverWithTransfersImport importInformation(String carNumber) {
        if (carNumber == null || carNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Mashina raqami bo'sh bo'lishi mumkin emas");
        }

        String cleanedInput = cleanTruckNumber(carNumber);
        log.info("Qidirilayotgan mashina (tozalangan): {}", cleanedInput);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        int page = 0;
        int size = 2000; // katta qilib oldik, tezroq bo‘ladi
        DriverWithTransfersImport latestMatch = null; // eng yangi topilganini saqlaymiz
        String latestEnterDate = null; // enterDate bo‘yicha solishtirish uchun

        // Eng yangi kirganlar birinchi chiqishi uchun sort qo‘shdik!
        String baseUrl = url + "?status=ENTERED&page={page}&size={size}&sort=enterDate,desc";

        while (true) {
            try {
                String currentUrl = baseUrl.replace("{page}", String.valueOf(page));
                log.info("API so‘rov: {} (Sahifa: {})", currentUrl, page);

                ResponseEntity<ApiResponse> response = restTemplate.exchange(
                        currentUrl, HttpMethod.GET, entity, ApiResponse.class, page, size
                );

                if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                    throw new IllegalArgumentException("API ishlamayapti: " + response.getStatusCode());
                }

                ApiResponse apiResponse = response.getBody();
                List<DriverWithTransfersImport> drivers = apiResponse.getContent();

                if (drivers == null || drivers.isEmpty()) {
                    if (apiResponse.isLast()) break;
                    page++;
                    continue;
                }

                log.info("Sahifa {}: {} ta mashina topildi", page, drivers.size());

                for (DriverWithTransfersImport di : drivers) {
                    String apiNumber = cleanTruckNumber(di.getDriver().getTransportNumber());
                    if (!cleanedInput.equals(apiNumber)) {
                        continue; // raqam mos emas
                    }

                    // ENTERED holatini tekshiramiz
                    boolean hasEntered = di.getTransfers() != null && di.getTransfers().stream()
                            .anyMatch(t -> {
                                if ("ENTERED".equalsIgnoreCase(t.getCurrentStatus())) return true;
                                if (t.getStatusChanges() != null) {
                                    return t.getStatusChanges().stream()
                                            .anyMatch(s -> "ENTERED".equalsIgnoreCase(s));
                                }
                                return false;
                            });

                    if (!hasEntered) {
                        log.warn("Raqam mos, lekin ENTERED yo‘q: {}", carNumber);
                        continue;
                    }

                    // enterDate ni olish
                    String enterDate = di.getDriver().getEnterDate();
                    if (enterDate == null) enterDate = "0000-00-00T00:00:00";

                    // Agar bu eng yangi bo‘lsa — saqlaymiz
                    if (latestMatch == null || enterDate.compareTo(latestEnterDate) > 0) {
                        latestMatch = di;
                        latestEnterDate = enterDate;
                        log.info("YANGI ENG YANGI TOPILDI: {} (enterDate: {})", carNumber, enterDate);
                    }
                }

                // Oxirgi sahifa bo‘lsa to‘xta
                if (apiResponse.isLast()) {
                    log.info("Oxirgi sahifa yetib keldi (jami sahifalar: {})", apiResponse.getTotalPages());
                    break;
                }

                page++;

            } catch (Exception e) {
                log.error("API xatosi (sahifa {}): {}", page, e.getMessage());
                throw new IllegalArgumentException("API bilan aloqa xatosi: " + e.getMessage());
            }
        }

        if (latestMatch != null) {
            log.info("MUVOFAQQIYAT: {} uchun eng yangi ENTERED ma’lumot topildi!", carNumber);
            return latestMatch;
        }

        throw new IllegalArgumentException("Mashina topilmadi yoki ENTERED holatida emas: " + carNumber);
    }


    @Transactional
    public void sendCameraImage(Long truckId, boolean isEntrance) {
        TruckEntity truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck id " + truckId + " not found"));

        // 1. Transfer ID olish
        DriverWithTransfersImport apiData = importInformation(truck.getTruckNumber());
        if (apiData == null || apiData.getTransfers().isEmpty()) {
            log.warn("Transfer ma'lumoti yo'q: {}", truck.getTruckNumber());
            return;
        }

        Long transferId = apiData.getTransfers().stream()
                .filter(t -> "ENTERED".equalsIgnoreCase(t.getCurrentStatus()))
                .max(Comparator.comparing(t -> LocalDateTime.parse(t.getCreatedAt())))
                .map(TransferImport::getId)
                .orElse(null);

        if (transferId == null) {
            log.warn("ENTERED transfer topilmadi: {}", truck.getTruckNumber());
            return;
        }

        // 2. Rasmlarni olish
        AttachStatus status = isEntrance ? AttachStatus.ENTRANCE_PHOTO : AttachStatus.EXIT_PHOTO;
        List<TruckPhotosEntity> photos = truckPhotoRepository.findByAttachStatusAndTruckId(status, truckId);
        if (photos.isEmpty()) {
            log.warn("Rasm topilmadi: TruckID = {}, Status = {}", truckId, status);
            return;
        }

        // 3. Kamera nomlari
        String[] cameraNames = isEntrance
                ? new String[]{"CAMERA1", "CAMERA2"}
                : new String[]{"CAMERA3", "CAMERA4"};

        int sentCount = 0;

        // 4. 2 tagacha rasm yuborish
        for (int i = 0; i < Math.min(photos.size(), 2); i++) {
            TruckPhotosEntity photoEntity = photos.get(i);
            AttachEntity attach = photoEntity.getTruckPhoto();
            if (attach == null) continue;

            // YAXSHILANGAN: null-safe tekshirish
            if (attach.getIsSentToCloud() != null && attach.getIsSentToCloud()) {
                log.info("Rasm allaqachon yuborilgan: {}", cameraNames[i]);
                continue;
            }

            // 5. S3 URL mavjudmi?
            String s3Url = attach.getPath();
            if (s3Url == null || !s3Url.contains("s3.tenzorsoft.uz")) {
                byte[] imageBytes = attachService.getBytesById(attach.getId());
                if (imageBytes == null || imageBytes.length == 0) {
                    log.warn("Rasm bo‘sh: AttachID = {}", attach.getId());
                    continue;
                }
                s3Url = s3Service.uploadFile(imageBytes); // toza URL!
                attach.setPath(s3Url);
                attachRepository.save(attach);
                log.info("Yangi S3 URL yaratildi: {}", s3Url);
            }

            // 6. DTO yaratish
            ImageSendDTO dto = new ImageSendDTO();
            dto.setLocalId(truck.getId());
            dto.setNavoiyAzotTransferId(transferId);
            dto.setCameraName(cameraNames[i]);
            dto.setPictureUrl(s3Url);

            // 7. API ga yuborish — XAVFSIZ!
            try {
                sendSingleImage(dto);
                log.info("API muvaffaqiyatli: {} → {}", cameraNames[i], truck.getTruckNumber());
                sentCount++;
            } catch (Exception e) {
                log.error("API ga yuborishda xato: {} | Xato: {}", cameraNames[i], e.getMessage());
                // Xato bo‘lsa ham davom etamiz!
            }

            // 8. Belgilash
            attach.setIsSentToCloud(true);
            attachRepository.save(attach);

            log.info("Rasm URL yuborildi: {} → {} | URL: {}",
                    cameraNames[i], truck.getTruckNumber(), s3Url);
        }

        log.info("{} rasmlari muvaffaqiyatli yuborildi: {} ta",
                isEntrance ? "KIRISH" : "CHIQISH", sentCount);
    }

    private void sendSingleImage(ImageSendDTO image) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(login, password);

        HttpEntity<ImageSendDTO> entity = new HttpEntity<>(image, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    image_url, HttpMethod.POST, entity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("API muvaffaqiyatli: {} – URL yuborildi", image.getCameraName());
            } else {
                log.error("API xato: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("API ga ulanishda xato: {}", e.getMessage());
        }
    }


    private void savePhoto(TruckEntity truck, AttachResponse attach, AttachStatus status, String type) {
        try {
            log.info("{} fotosi qo'shish boshlandi: AttachID={}", type, attach.getId());

            AttachEntity attachEntity = attachService.findById(attach.getId());
            if (attachEntity == null) {
                log.error("{} AttachEntity topilmadi: ID={}", type, attach.getId());
                return;
            }

            TruckPhotosEntity photoEntity = new TruckPhotosEntity();
            photoEntity.setTruckPhoto(attachEntity);
            photoEntity.setAttachStatus(status);
            photoEntity.setTruck(truck);

            TruckPhotosEntity savedPhoto = truckPhotoRepository.save(photoEntity);
            truck.getTruckPhotos().add(savedPhoto);
            truckRepository.save(truck); // ← truckPhotos yangilandi

            log.info("{} FOTO SAQLANDI: PhotoID={}, AttachID={}, S3_URL={}",
                    type, savedPhoto.getId(), attach.getId(), attachEntity.getPath());

        } catch (Exception e) {
            log.error("{} fotosini saqlashda xatolik: {}", type, e.getMessage(), e);
            logService.save(new LogEntity(5L, truck.getTruckNumber(),
                    type + " FOTO XATOSI: " + e.getMessage()));
        }
    }

    @Transactional
    public void saveTruck(TruckResponse currentTruck, Integer id, List<AttachResponse> attachResponses) {

        if (attachResponses == null) {
            attachResponses = new ArrayList<>();
        }

        // ======== TRUCK NUMBER TOZALASH ========
        if (currentTruck.getTruckNumber() == null || currentTruck.getTruckNumber().trim().isEmpty()) {
            log.error("Mashina raqami kiritilmagan");
            throw new IllegalArgumentException("Mashina raqami kiritilmagan");
        }

        String rawNumber = currentTruck.getTruckNumber();
        String truckNumber = rawNumber.trim().toUpperCase().replaceAll("\\s+", "");
        currentTruck.setTruckNumber(truckNumber);
        log.info("Mashina raqami tozalandi: '{}' → '{}'", rawNumber, truckNumber);

        // ========================================
        // KIRISH (CAMERA 1)
        // ========================================
        if (id == 1) {

            log.info("KIRISH jarayoni boshlandi: {}", truckNumber);

            DriverWithTransfersImport apiTruck = importInformation(truckNumber);
            if (apiTruck == null) {
                log.error("Mashina API da topilmadi: {}", truckNumber);
                throw new IllegalArgumentException("Mashina topilmadi yoki kirish holatida emas: " + truckNumber);
            }

            ProductsEntity selectedProduct = productService.getSelectedProduct();
            if (selectedProduct == null) {
                log.error("Tanlangan mahsulot topilmadi");
                throw new IllegalStateException("Tanlangan mahsulot topilmadi.");
            }

            // Yangi truck entity yaratish
            currentTruckEntity = new TruckEntity();
            currentTruckEntity.setProducts(selectedProduct);
            currentTruckEntity.setTruckNumber(truckNumber);
            currentTruckEntity.setIsFinished(false);
            currentTruckEntity.setTruckPhotos(new ArrayList<>());

            log.info("TruckEntity yaratildi: {}", truckNumber);

            // Kirish action
            TruckActionEntity enteredAction = new TruckActionEntity();
            enteredAction.setAction(currentTruck.getEnteredStatus());
            enteredAction.setOnDuty(Instances.currentUser);
            enteredAction.setWeight(currentTruck.getEnteredWeight());
            enteredAction.setCreatedAt(currentTruck.getEnteredAt() != null ? currentTruck.getEnteredAt() : LocalDateTime.now());

            // TO‘G‘RI: PROCESSING (chunki Tara hali yuborilmagan)
            enteredAction.setActionStatus(ActionStatus.PROCESSING);

            TruckActionEntity savedAction = truckActionRepository.save(enteredAction);
            log.info("Kirish harakati saqlandi: ActionID={}, Status=PROCESSING", savedAction.getId());

            // Harakatni truckga biriktirish
            currentTruckEntity.setTruckActions(new ArrayList<>(List.of(savedAction)));

            TruckEntity savedTruck = truckRepository.save(currentTruckEntity);
            currentTruck.setId(savedTruck.getId());
            log.info("TRUCK SAQLANDI: TruckID={}, Raqam={}", savedTruck.getId(), truckNumber);

            // FOTOLARNI SAQLASH
            for (AttachResponse attach : attachResponses) {
                if (attach != null && attach.getId() != null) {
                    savePhoto(savedTruck, attach, AttachStatus.ENTRANCE_PHOTO, "KIRISH");
                }
            }

            if (attachResponses.isEmpty()) {
                log.warn("Attach null yoki ID yo'q - foto saqlanmadi");
            }

            // TARA YUBORISH – faqat muvaffaqiyatli bo‘lsa COMPLETE qilamiz
            try {
                sendTaraWeight(savedTruck.getId());
                log.info("TARA muvaffaqiyatli yuborildi → COMPLETE bo‘ldi: TruckID={}", savedTruck.getId());
            } catch (Exception e) {
                log.error("TARA yuborishda xatolik: {}. Status PROCESSING qoladi!", e.getMessage());
                Platform.runLater(() ->
                        mainController.showAlert(Alert.AlertType.WARNING, "Internet yo‘q",
                                "Tara yuborilmadi! Internet kelganda qayta urinib ko‘ring.\nStatus: Kutilmoqda"));
            }

            // RASMLAR
            try {
                sendCameraImage(savedTruck.getId(), true);
                log.info("Kirish rasmi yuborildi: TruckID={}", savedTruck.getId());
            } catch (Exception e) {
                log.error("Kirish rasmi yuborishda xatolik: {}", e.getMessage());
            }

            log.info("KIRISH JARAYONI TUGADI: {}", truckNumber);
        }

        // ========================================
        // CHIQISH (CAMERA 2)
        // ========================================
        else if (id == 2) {

            log.info("CHIQISH jarayoni boshlandi: {}", truckNumber);

            List<TruckEntity> activeTrucks = truckRepository.findEnteredTrucksReadyForExit(truckNumber);

            if (activeTrucks.isEmpty()) {
                log.error("Kirish yakunlangan faol truck topilmadi: {}", truckNumber);
                throw new IllegalStateException("Mashina topilmadi yoki kirish hali yakunlanmagan: " + truckNumber);
            }

            if (activeTrucks.size() > 1) {
                log.warn("Bir xil raqam bo'yicha bir nechta faol truck topildi. Oxirgisi tanlanadi: {}", truckNumber);
            }

            currentTruckEntity = activeTrucks.get(activeTrucks.size() - 1);
            log.info("Faol truck tanlandi: TruckID={}", currentTruckEntity.getId());

            // Eski chiqish fotolarini tozalash
            int removedCount = 0;
            Iterator<TruckPhotosEntity> iterator = currentTruckEntity.getTruckPhotos().iterator();
            while (iterator.hasNext()) {
                TruckPhotosEntity photo = iterator.next();
                if (photo.getAttachStatus() == AttachStatus.EXIT_PHOTO) {
                    iterator.remove();
                    removedCount++;
                }
            }
            if (removedCount > 0) {
                log.info("{} ta eski chiqish fotosi o'chirildi", removedCount);
            }

            // Chiqish action
            TruckActionEntity exitedAction = new TruckActionEntity();
            exitedAction.setAction(currentTruck.getExitedStatus());
            exitedAction.setOnDuty(Instances.currentUser);
            exitedAction.setWeight(currentTruck.getExitedWeight());
            exitedAction.setCreatedAt(currentTruck.getExitedAt() != null ? currentTruck.getExitedAt() : LocalDateTime.now());

            // CHIQISH uchun ham PROCESSING → keyin sendNetoWeight ichida COMPLETE
            exitedAction.setActionStatus(ActionStatus.PROCESSING);

            TruckActionEntity savedExitAction = truckActionRepository.save(exitedAction);
            log.info("Chiqish harakati saqlandi: ActionID={}, Status=PROCESSING", savedExitAction.getId());

            currentTruckEntity.getTruckActions().add(savedExitAction);
            currentTruckEntity.setIsFinished(true);

            TruckEntity savedTruck = truckRepository.save(currentTruckEntity);
            currentTruck.setId(savedTruck.getId());
            log.info("CHIQISH SAQLANDI: TruckID={}, Raqam={}", savedTruck.getId(), truckNumber);

            // CHIQISH FOTOLARI
            for (AttachResponse attach : attachResponses) {
                if (attach != null && attach.getId() != null) {
                    savePhoto(savedTruck, attach, AttachStatus.EXIT_PHOTO, "CHIQISH");
                }
            }

            if (attachResponses.isEmpty()) {
                log.warn("Chiqish uchun attach null yoki ID yo'q");
            }

            // BRUTTO YUBORISH
            try {
                sendNetoWeight(savedTruck.getId());
                log.info("Brutto muvaffaqiyatli yuborildi → COMPLETE bo‘ldi: TruckID={}", savedTruck.getId());
            } catch (Exception e) {
                log.error("Brutto yuborishda xatolik: {}. Status PROCESSING qoladi!", e.getMessage());
                Platform.runLater(() ->
                        mainController.showAlert(Alert.AlertType.WARNING, "Internet yo‘q",
                                "Brutto yuborilmadi! Internet kelganda qayta urinib ko‘ring.\nStatus: Kutilmoqda"));
            }

            // CHIQISH RASMLARI
            try {
                sendCameraImage(savedTruck.getId(), false);
                log.info("Chiqish rasmi yuborildi: TruckID={}", savedTruck.getId());
            } catch (Exception e) {
                log.error("Chiqish rasmi yuborishda xatolik: {}", e.getMessage());
            }

            log.info("CHIQISH JARAYONI TUGADI: {}", truckNumber);
        }
        else {
            log.error("Noto'g'ri kamera ID: {}", id);
            throw new IllegalArgumentException("Noto'g'ri kamera ID: " + id);
        }
    }

    public TruckEntity saveCurrentTruck(TruckResponse currentTruck, boolean isFinished) {
        currentTruckEntity.setIsFinished(isFinished);
        currentTruckEntity.setIsSentToCloud(false);
        try {
            currentTruckEntity = truckRepository.save(currentTruckEntity);
        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00015: (" + getClass().getName() + ") " + e.getMessage()));
            System.err.println(e.getMessage());
        }
        return currentTruckEntity;
    }

    public TruckEntity saveCurrentExitTruck(TruckResponse currentTruck, boolean isFinished) {
        currentExitTruckEntity.setIsFinished(isFinished);
        currentExitTruckEntity.setIsSentToCloud(false);
        try {
            currentExitTruckEntity = truckRepository.save(currentExitTruckEntity);
        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckExitNumber, "00015: (" + getClass().getName() + ") " + e.getMessage()));
            System.err.println(e.getMessage());
        }
        return currentExitTruckEntity;
    }

    @Override
    public TruckResponse entityToResponse(TruckEntity entity) {
        return null;
    }

    @Override
    public TruckEntity requestToEntity(TruckRequest request) {
        return null;
    }

    @Transactional
    public void saveTruckAttaches(TruckResponse currentTruck, AttachResponse response, AttachStatus attachStatus) {
        try {

            currentTruckEntity = truckRepository.findByTruckNumberAndIsFinished(currentTruck.getTruckNumber(), false)
                    .orElse(new TruckEntity());
            currentTruckEntity.setTruckNumber(currentTruck.getTruckNumber());
            if (response != null) {

                if (currentTruckEntity == null || currentTruckEntity.getTruckPhotos() == null) {
                    log.error("Truck entity or its photos are null. Cannot proceed.");
                    throw new IllegalStateException("Truck entity or photos list is null.");
                }

//        List<TruckPhotosEntity> truckPhotos = currentTruckEntity.getTruckPhotos();
//        truckPhotos.removeIf(photo -> photo.getAttachStatus().equals(attachStatus));

                List<TruckPhotosEntity> truckPhotos = new ArrayList<>(currentTruckEntity.getTruckPhotos());
                truckPhotos.removeIf(photo -> attachStatus.equals(photo.getAttachStatus()));


                AttachEntity attach = attachService.findById(response.getId());
                if (attach == null) {
                    log.error("Attach entity not found for id: {}", response.getId());
                    throw new EntityNotFoundException("Attach not found with ID: " + response.getId());
                }
//
                TruckPhotosEntity photosEntity = new TruckPhotosEntity(
                        attach, attachStatus
                );
                truckPhotoRepository.save(photosEntity);
//
                truckPhotos.add(photosEntity);
                currentTruckEntity.setTruckPhotos(truckPhotos);
                truckRepository.save(currentTruckEntity);

                log.info("Truck photos updated successfully for truck number: {}", currentTruck.getTruckNumber());
            }


        } catch (NullPointerException e) {
            System.out.println("Mana saveTruckAttaches() metodida xato tashadi:  NullPointerException");
        } catch (Exception e) {
            System.out.println("Mana saveTruckAttaches() metodida xato tashadi:  Exception");
        }
    }

    public void saveTruckEnteredActions(TruckResponse currentTruck) {
        saveTruckAction(
                currentTruck.getEnteredStatus(),
                currentTruck.getEnteredAt(),
                currentTruck.getEnteredWeight()
        );
        currentTruckEntity.setIsSentToCloud(false);
        currentTruckEntity.setNextEntranceTime(currentTruck.getEnteredAt().plusMinutes(EXIT_TIMEOUT));
        System.out.println("Chiqish vaqti: " + EXIT_TIMEOUT);
        truckRepository.save(currentTruckEntity);
        log.info("Truck entered action saved for status: {}", currentTruck.getEnteredStatus());
    }

    public void saveTruckExitedAction(TruckResponse currentTruck) {
        saveTruckAction(
                currentTruck.getExitedStatus(),
                currentTruck.getExitedAt(),
                currentTruck.getExitedWeight()
        );
        currentExitTruckEntity.setIsSentToCloud(false);
        currentExitTruckEntity.setNextEntranceTime(currentTruck.getExitedAt().plusMinutes(EXIT_TIMEOUT));
        System.out.println("Chiqish vaqti: " + EXIT_TIMEOUT);
        truckRepository.save(currentExitTruckEntity);
        log.info("Truck exited action saved for status: {}", currentTruck.getExitedStatus());
    }

    private void saveTruckAction(
            TruckAction status,
            LocalDateTime actionTime,
            Double weight
    ) {
        for (TruckActionEntity action : currentTruckEntity.getTruckActions()) {
            if (action.getAction() != null && action.getAction().equals(status)) {
                action.setCreatedAt(actionTime);
                action.setWeight(weight);
                action.setOnDuty(Instances.currentUser);
                action.setActionStatus(ActionStatus.COMPLETE);
                truckActionRepository.save(action);
                log.info("Action updated with status: {}, weight: {}, time: {}", status, weight, actionTime);
                break;
            }
        }
    }


    public void saveTruckStatus(TruckAction currentTruckAction, ActionStatus status) {
        for (TruckActionEntity action : currentTruckEntity.getTruckActions()) {
            if (action.getAction() == currentTruckAction) {
                action.setActionStatus(status);
                truckActionRepository.save(action);
                break;
            }
        }
        tableController.loadData();
        System.out.println("Loading table data ....saveTruckStatus");


    }


    public List<TruckEntity> findNotSentDataToMyCoal() {
        return truckRepository.findByIsSentToMyCoalAndIsFinishedAndIsDeleted(false, true, false);
    }

    public boolean isNumberExists(String truckNumber) {
        if (truckNumber == null) return false;
        return isTruckNumberExists(truckNumber);
    }

    public boolean isValidTruckNumber(String truckNumber) {
        return regexChecker(truckNumber, regexPattern);
    }

    public List<String> getNotFinishedTrucks() {
        List<TruckEntity> trucks = truckRepository.findByIsFinishedAndIsDeleted(false, false);
        List<String> result = new ArrayList<>();
        for (TruckEntity truck : trucks) {
            for (TruckActionEntity action : truck.getTruckActions()) {
                if ((action.getAction() == TruckAction.ENTRANCE ||
                        action.getAction() == TruckAction.MANUAL_ENTRANCE) &&
                        (action.getActionStatus() == null || action.getActionStatus() == ActionStatus.COMPLETE)) {
                    result.add(truck.getTruckNumber());
                }
            }
        }
        return result;
    }

    public boolean isEntranceAvailableForCamera1(String truckNumber) {
        // Check if the truck number exists in the system
        if (!isTruckNumberExists(truckNumber)) {
            return true;
        }

        // Check if the truck is currently in the system and not yet finished
        List<TruckEntity> ongoingTrucks = truckRepository.findByTruckNumberAndActionStatus(
                truckNumber, List.of(TruckAction.ENTRANCE, TruckAction.MANUAL_ENTRANCE),
                false,
                false
        );

        if (!ongoingTrucks.isEmpty()) {
            mainController.showAlert(Alert.AlertType.ERROR, "Xatolik", "Bu mashinasi hali chiqmagan!");
            return false;
        }

        // Fetch the most recent truck action to verify re-entry time
        List<TruckEntity> completedTrucks = truckRepository.findByTruckNumberAndActionStatus(
                truckNumber, List.of(TruckAction.ENTRANCE, TruckAction.MANUAL_ENTRANCE),
                true,
                false
        );

        if (!completedTrucks.isEmpty()) {
            TruckEntity lastTruck = completedTrucks.get(0);
            if (lastTruck.getNextEntranceTime().isAfter(LocalDateTime.now())) {
                mainController.showAlert(Alert.AlertType.INFORMATION, "Info", "Yuk mashinasi 5 daqiqadan keyin yana kirishi mumkin!");
                return false;
            }
        }

        return true;
    }


    private boolean isTruckNumberExists(String truckNumber) {
        return truckRepository.existsByTruckNumberAndIsDeleted(truckNumber, false);
    }

    public boolean isNotFinishedTrucksExists() {
        return truckRepository.existsByIsFinishedFalseAndIsDeletedFalse();
    }


    public boolean isEntranceAvailableForCamera2(String truckNumber) {
        //if (!isTruckNumberExists(truckNumber)) {
        if (!truckRepository.existsByIsFinishedFalseAndIsDeletedFalse()) {
            mainController.showAlert(Alert.AlertType.WARNING, "Xatolik", "Hamma moshinalar chiqib ketgan!");
            return false;
        }

        //if (isTruckNumberExists(truckNumber)) {
        List<TruckEntity> trucks = truckRepository.findByTruckNumberAndActionStatus(
                truckNumber, List.of(TruckAction.ENTRANCE, TruckAction.MANUAL_ENTRANCE),
                false, false
        );
        if (trucks.isEmpty()) {
            Map<String, String> updatedTruckNumber = mainController.showTruckNotFoundPopupWithSelectionSafe(truckNumber, getNotFinishedTrucks());
            if (updatedTruckNumber == null || updatedTruckNumber.isEmpty()) {
                return false;
            }

            String editedTruckNumber = updatedTruckNumber.get("textFieldValue");
            String selectedTruckNumber = updatedTruckNumber.get("dropDownValue");

            TruckEntity truckToUpdate = truckRepository.findByTruckNumberAndActionStatus(
                    selectedTruckNumber, List.of(TruckAction.ENTRANCE, TruckAction.MANUAL_ENTRANCE),
                    false, false
            ).get(0);

            if (!selectedTruckNumber.equals(editedTruckNumber)) {
                truckToUpdate.setTruckNumber(editedTruckNumber);
                truckToUpdate.setOriginalTruckNumber(selectedTruckNumber);
                truckRepository.save(truckToUpdate);
                tableController.loadDataNow();
            }

            if (truckToUpdate.getNextEntranceTime().isAfter(LocalDateTime.now())) {
                LocalDateTime nextEntranceTime = truckToUpdate.getNextEntranceTime();
                mainController.showAlert(Alert.AlertType.INFORMATION, "Info",
                        nextEntranceTime.getHour() + ":" + nextEntranceTime.getMinute() + ":" + nextEntranceTime.getSecond() + " dan keyin kirishi mumkin!");
                return false;
            }

            Instances.truckExitNumber = truckToUpdate.getTruckNumber();
            return true;
        }

        TruckEntity truckEntity = trucks.get(0);

        Instances.truckExitNumber = truckEntity.getTruckNumber();
        LocalDateTime nextEntranceTime = truckEntity.getNextEntranceTime();
        boolean canEnter = nextEntranceTime.isBefore(LocalDateTime.now());
        if (!canEnter) {
            mainController.showAlert(Alert.AlertType.INFORMATION, "Info",
                    nextEntranceTime.getHour() + ":" + nextEntranceTime.getMinute() + ":" + nextEntranceTime.getSecond() + " dan keyin kirishi mumkin!");
        }
        return canEnter;
    }


    private boolean regexChecker(String str, String regex) {
        if (str == null) return false;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    public List<TruckEntity> filterWithDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
        }

        if (startDate == null) {
            return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(endDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        } else if (endDate == null) {
            return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(startDate.atStartOfDay(), startDate.atTime(LocalTime.MAX));
        }

        if (startDate.isBefore(endDate)) {
            return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        }

        if (startDate.equals(endDate)) {
            return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        }

        return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
    }

    public TruckEntity saveTruck(TruckEntity truck) {
        return truckRepository.save(truck);
    }

    /**
     * API dan kelgan ma'lumotlarni bazaga saqlash yoki yangilash
     */
    @Transactional
    public TruckEntity createOrUpdateFromApi(String carNumber, String ownerPinfl, String driverName,
                                             String productName, Double quantity, String model) {
        try {
            // Input validation
            if (carNumber == null || carNumber.trim().isEmpty()) {
                log.warn("Car number bo'sh yoki null");
                return null;
            }

            carNumber = carNumber.trim().toUpperCase();

            // Duplicate processing ni oldini olish
            synchronized (carNumber.intern()) {

                // Multiple results muammosini hal qilish
                List<TruckEntity> existingTrucks = truckRepository.findAllByTruckNumberAndIsDeletedFalse(carNumber);

                TruckEntity existingTruck = null;

                if (!existingTrucks.isEmpty()) {
                    // Agar bir nechta bo'lsa, birinchisini olish
                    existingTruck = existingTrucks.get(0);

                    // Agar duplicate lar bo'lsa, log yozish
                    if (existingTrucks.size() > 1) {
                        log.warn("Duplicate trucks topildi carNumber {} uchun: {} ta record", carNumber, existingTrucks.size());

                        // Qolgan duplicate larni o'chirish (ixtiyoriy)
                        for (int i = 1; i < existingTrucks.size(); i++) {
                            TruckEntity duplicateTruck = existingTrucks.get(i);
                            duplicateTruck.setIsDeleted(true);
                            truckRepository.save(duplicateTruck);
                            log.info("Duplicate truck o'chirildi: ID {}", duplicateTruck.getId());
                        }
                    }
                }

                if (existingTruck != null) {
                    log.info("Mavjud truck yangilanmoqda: {}", carNumber);

                    // Update existing truck - faqat null bo'lmagan qiymatlar
                    if (ownerPinfl != null && !ownerPinfl.trim().isEmpty()) {
                        existingTruck.setOwnerPinfl(ownerPinfl.trim());
                    }

                    if (driverName != null && !driverName.trim().isEmpty()) {
                        existingTruck.setDriverName(driverName.trim());
                    } else {
                        log.debug("Driver name null yoki bo'sh: {}", carNumber);
                    }

                    // Product handling with fallback logic
                    ProductsEntity productToSet = null;

                    if (productName != null && !productName.trim().isEmpty()) {
                        try {
                            Optional<ProductsEntity> productOpt = productRepository.findByNameIgnoreCase(productName.trim());
                            if (productOpt.isPresent()) {
                                productToSet = productOpt.get();
                                log.debug("Product topildi va o'rnatildi: {} -> {}", carNumber, productName);
                            } else {
                                log.warn("Product topilmadi: {}, oxirgi productni o'rnatamiz", productName);
                                // Oxirgi productni olish
                                productToSet = getLastProduct();
                            }
                        } catch (Exception e) {
                            log.error("Product qidirishda xatolik: {}, oxirgi productni o'rnatamiz", e.getMessage());
                            productToSet = getLastProduct();
                        }
                    } else {
                        log.debug("Product name null yoki bo'sh: {}, birinchi productni o'rnatamiz", carNumber);
                        // Birinchi productni olish
                        productToSet = getFirstProduct();
                    }

                    // Product o'rnatish
                    if (productToSet != null) {
                        existingTruck.setProducts(productToSet);
                        log.debug("Product o'rnatildi: {} -> {} (ID: {})", carNumber, productToSet.getName(), productToSet.getId());
                    } else {
                        log.warn("Hech qanday product topilmadi, bo'sh list o'rnatildi: {}", carNumber);
                        existingTruck.setProducts(null);
                    }

                    if (quantity != null && quantity > 0) {
                        existingTruck.setQuantity(quantity);
                    }

                    if (model != null && !model.trim().isEmpty()) {
                        existingTruck.setModel(model.trim());
                    }


                    TruckEntity savedTruck = truckRepository.save(existingTruck);
                    log.info("Truck muvaffaqiyatli yangilandi: {}", carNumber);
                    return savedTruck;

                } else {
                    log.info("Yangi truck yaratilmoqda: {}", carNumber);

                    // Create new truck
                    TruckEntity newTruck = new TruckEntity();
                    newTruck.setTruckNumber(carNumber);
                    newTruck.setOwnerPinfl(ownerPinfl != null ? ownerPinfl.trim() : null);
                    newTruck.setDriverName(driverName != null ? driverName.trim() : null);
                    newTruck.setQuantity(quantity);
                    newTruck.setModel(model != null ? model.trim() : null);
                    newTruck.setCreatedAt(LocalDateTime.now());
                    newTruck.setIsDeleted(false);

                    // Product handling for new truck with fallback logic
                    ProductsEntity productToSet = null;

                    if (productName != null && !productName.trim().isEmpty()) {
                        try {
                            Optional<ProductsEntity> productOpt = productRepository.findByNameIgnoreCase(productName.trim());
                            if (productOpt.isPresent()) {
                                productToSet = productOpt.get();
                                log.debug("Yangi truck uchun product topildi: {} -> {}", carNumber, productName);
                            } else {
                                log.warn("Product topilmadi yangi truck uchun: {}, oxirgi productni o'rnatamiz", productName);
                                productToSet = getLastProduct();
                            }
                        } catch (Exception e) {
                            log.error("Yangi truck uchun product qidirishda xatolik: {}, oxirgi productni o'rnatamiz", e.getMessage());
                            productToSet = getLastProduct();
                        }
                    } else {
                        log.debug("Yangi truck uchun product name null yoki bo'sh: {}, birinchi productni o'rnatamiz", carNumber);
                        productToSet = getFirstProduct();
                    }

                    // Product o'rnatish
                    if (productToSet != null) {
                        newTruck.setProducts(productToSet);
                        log.debug("Yangi truck uchun product o'rnatildi: {} -> {} (ID: {})", carNumber, productToSet.getName(), productToSet.getId());
                    } else {
                        log.warn("Yangi truck uchun hech qanday product topilmadi: {}", carNumber);
                        newTruck.setProducts(null);
                    }

                    TruckEntity savedTruck = truckRepository.save(newTruck);
                    log.info("Yangi truck yaratildi: {}", carNumber);

                    // Create default action
                    try {
                        TruckActionEntity defaultAction = new TruckActionEntity();
                        defaultAction.setAction(TruckAction.NO_ACTION);
                        defaultAction.setActionStatus(ActionStatus.NEW);
//                        defaultAction.setTruck(savedTruck);
                        defaultAction.setCreatedAt(LocalDateTime.now());

                        truckActionRepository.save(defaultAction);
                    } catch (Exception e) {
                        log.error("Default action yaratishda xatolik: {}", e.getMessage());
                    }

                    return savedTruck;
                }
            }

        } catch (Exception e) {
            log.error("Error creating/updating truck from API for carNumber {}: {}", carNumber, e.getMessage(), e);
            return null;
        }
    }


    /**
     * Oxirgi (eng so'nggi yaratilgan) productni olish
     */
    private ProductsEntity getLastProduct() {
        try {
            Optional<ProductsEntity> lastProduct = productRepository.findFirstByOrderByCreatedAtDesc();
            if (lastProduct.isPresent()) {
                log.debug("Oxirgi product topildi: {} (ID: {})", lastProduct.get().getName(), lastProduct.get().getId());
                return lastProduct.get();
            } else {
                log.warn("Hech qanday product mavjud emas (oxirgi product)");
                return null;
            }
        } catch (Exception e) {
            log.error("Oxirgi productni olishda xatolik: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Birinchi (eng eski yaratilgan) productni olish
     */
    private ProductsEntity getFirstProduct() {
        try {
            Optional<ProductsEntity> firstProduct = productRepository.findFirstByOrderByCreatedAtAsc();
            if (firstProduct.isPresent()) {
                log.debug("Birinchi product topildi: {} (ID: {})", firstProduct.get().getName(), firstProduct.get().getId());
                return firstProduct.get();
            } else {
                log.warn("Hech qanday product mavjud emas (birinchi product)");
                return null;
            }
        } catch (Exception e) {
            log.error("Birinchi productni olishda xatolik: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Truck raqami avtorizatsiya qilinganligini tekshirish
     */
    public boolean isAuthorizedTruck(String truckNumber) {
        try {
            TruckEntity truck = truckRepository.findByTruckNumber(truckNumber);
            return truck != null;
        } catch (Exception e) {
            log.error("Error checking truck authorization: {}", e.getMessage());
            return false;
        }
    }

    /**
     * API dan truck ma'lumotlarini olish
     */
    public TruckEntity getTruckApiData(String truckNumber) {
        try {
            return truckRepository.findByTruckNumber(truckNumber);
        } catch (Exception e) {
            log.error("Error getting truck API data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Truck ma'lumotlarini API dan yangilash
     */
    public void updateTruckWithApiData(String truckNumber) {
        try {
            TruckEntity existingTruck = truckRepository.findByTruckNumber(truckNumber);
            if (existingTruck != null) {
                log.info("Truck ma'lumotlari topildi: {} - Driver: {}, Product: {}, Model: {}, Quantity: {}",
                        truckNumber,
                        existingTruck.getDriverName(),
                        existingTruck.getProducts().getName(),
                        existingTruck.getModel(),
                        existingTruck.getQuantity());
            } else {
                log.warn("Truck topilmadi: {}", truckNumber);
            }
        } catch (Exception e) {
            log.error("Error updating truck with API data: {}", e.getMessage());
        }
    }

    public TruckEntity findByTruckNumber(String truckNumber) {
        return truckRepository.findByTruckNumber(truckNumber);
    }
}
