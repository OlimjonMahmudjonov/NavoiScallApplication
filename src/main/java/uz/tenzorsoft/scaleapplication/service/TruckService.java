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
import org.springframework.stereotype.Service;
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
import uz.tenzorsoft.scaleapplication.repository.TruckActionRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckPhotoRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;
import uz.tenzorsoft.scaleapplication.ui.MainController;
import uz.tenzorsoft.scaleapplication.ui.TableController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uz.tenzorsoft.scaleapplication.domain.Settings.*;

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

    public List<TableViewData> getTruckData() {
        List<TruckEntity> all = truckRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<TableViewData> data = new ArrayList<>();
        for (TruckEntity truck : all) {
            data.add(entityToTableViewData(truck));
        }
        return data;
    }

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

//        truck.getAttaches().forEach(attach -> {
//            truckPhotos.add(new TruckPhotosEntity(
//                    attachService.findById(attach.getId()), attach.getStatus()
//            ));
//        });

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
    public void saveTruck(TruckResponse currentTruck, Integer id, AttachResponse attach) {

        if (id == 1) {
            // Fetch the selected product from the database
            ProductsEntity selectedProduct = productService.getSelectedProduct();
            if (selectedProduct == null) {
                throw new IllegalStateException("Tanlangan mahsulot topilmadi. Yuk mashinasini saqlashdan oldin mahsulotni tanlang.");
            }

            currentTruckEntity = new TruckEntity();
            currentTruckEntity.setProducts(selectedProduct); // Set the selected product
            currentTruckEntity.setTruckNumber(currentTruck.getTruckNumber());
            List<TruckPhotosEntity> truckPhotos = new ArrayList<>();
            if (attach != null) {
                TruckPhotosEntity entity = truckPhotoRepository.save(
                        new TruckPhotosEntity(attachService.findById(attach.getId()), AttachStatus.ENTRANCE_PHOTO)
                );

                truckPhotos.add(entity);
            }
//            for (AttachIdWithStatus attach : currentTruck.getAttaches()) {
//                if (attach.getStatus().equals(AttachStatus.ENTRANCE_PHOTO)) {
//                    break;
//                }
//            }
            TruckActionEntity enteredAction = new TruckActionEntity();
            enteredAction.setAction(currentTruck.getEnteredStatus());
            enteredAction.setOnDuty(Instances.currentUser);
            truckActionRepository.save(enteredAction);
            currentTruckEntity.setTruckActions(List.of(enteredAction));
            currentTruckEntity.setTruckPhotos(truckPhotos);
            currentTruckEntity.setIsFinished(false);
            TruckEntity savedTruck = truckRepository.save(currentTruckEntity);
            currentTruck.setId(savedTruck == null ? null : savedTruck.getId());
        } else {
//            1 ta malumot kelmasligi mumkin
//            currentTruckEntity = truckRepository.findByTruckNumberAndIsFinished(currentTruck.getTruckNumber(), false).orElse(null);
//            if (currentTruckEntity == null)
//                throw new RuntimeException("Truck not found with truck number: " + currentTruck.getTruckNumber());
            List<TruckEntity> list = truckRepository.findByTruckNumberAndActionStatus(
                    currentTruck.getTruckNumber(), List.of(TruckAction.ENTRANCE, TruckAction.MANUAL_ENTRANCE),
                    false, false);
            if (list.isEmpty()) {
                log.error("unable to find trucks with number: {}", currentTruck.getTruckNumber());
                return;
            }
            currentExitTruckEntity = list.get(0);
            currentExitTruckEntity.getTruckPhotos().removeIf(photo -> photo.getAttachStatus() == AttachStatus.EXIT_PHOTO);

            List<TruckPhotosEntity> truckPhotos = new ArrayList<>();
            if (attach != null) {
                TruckPhotosEntity entity = truckPhotoRepository.save(
                        new TruckPhotosEntity(attachService.findById(attach.getId()), AttachStatus.EXIT_PHOTO)
                );

                truckPhotos.add(entity);
            }
//            for (AttachIdWithStatus attach : currentTruck.getAttaches()) {
//                if (attach.getStatus().equals(AttachStatus.EXIT_PHOTO)) {
//                    break;
//                }
//            }
            TruckActionEntity exitedAction = new TruckActionEntity();
            exitedAction.setAction(currentTruck.getExitedStatus());
            exitedAction.setOnDuty(Instances.currentUser);
            truckActionRepository.save(exitedAction);
            currentExitTruckEntity.getTruckActions().add(exitedAction);
            currentExitTruckEntity.getTruckPhotos().addAll(truckPhotos);
            truckRepository.save(currentTruckEntity);
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

            //        currentTruckEntity = truckRepository.findByTruckNumberAndIsFinished(currentTruck.getTruckNumber(), false)
//                .orElse(new TruckEntity());
//        currentTruckEntity.setTruckNumber(currentTruck.getTruckNumber());
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
        currentTruckEntity.setIsSentToCloud(false);
        currentTruckEntity.setNextEntranceTime(currentTruck.getExitedAt().plusMinutes(EXIT_TIMEOUT));
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


//    public void saveTruckEnteredActions(TruckResponse currentTruck) {
//        for (TruckActionEntity action : currentTruckEntity.getTruckActions()) {
//            if (action.getAction() == currentTruck.getEnteredStatus()) {
//                action.setCreatedAt(currentTruck.getEnteredAt());
//                action.setWeight(currentTruck.getEnteredWeight());
//                action.setAction(currentTruck.getEnteredStatus() == null ? TruckAction.NO_ACTION : currentTruck.getEnteredStatus());
//                action.setOnDuty(Instances.currentUser);
//                action.setActionStatus(ActionStatus.COMPLETE);
//                truckActionRepository.save(action);
//                break;
//            }
//        }
//        currentTruckEntity.setIsSentToCloud(false);
//        currentTruckEntity.setNextEntranceTime(currentTruck.getEnteredAt().plusMinutes(5));
//        truckRepository.save(currentTruckEntity);
//    }
//
//    public void saveTruckExitedAction(TruckResponse currentTruck) {
//        for (TruckActionEntity action : currentTruckEntity.getTruckActions()) {
//            if (action.getAction() == currentTruck.getExitedStatus()) {
//                action.setCreatedAt(currentTruck.getExitedAt());
//                action.setWeight(currentTruck.getExitedWeight());
//                action.setAction(currentTruck.getExitedStatus() == null ? TruckAction.NO_ACTION : currentTruck.getExitedStatus());
//                action.setOnDuty(Instances.currentUser);
//                action.setActionStatus(ActionStatus.COMPLETE);
//                truckActionRepository.save(action);
//                break;
//            }
//        }
//        currentTruckEntity.setIsSentToCloud(false);
//        currentTruckEntity.setNextEntranceTime(currentTruck.getExitedAt().plusMinutes(5));
//        truckRepository.save(currentTruckEntity);
//    }

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
/*
        // Check if the status already exists for the truck
        boolean statusExists = currentTruckEntity.getTruckActions().stream()
                .anyMatch(action -> action.getActionStatus() == status);

        if (!statusExists) {
            TruckActionEntity truckActionEntity = new TruckActionEntity();
            truckActionEntity.setCreatedAt(LocalDateTime.now());
            truckActionEntity.setWeight(currentTruck.getEnteredWeight());
            truckActionEntity.setActionStatus(status);
            truckActionEntity.setOnDuty(Instances.currentUser);
            truckActionRepository.save(truckActionEntity);
            currentTruckEntity.getTruckActions().add(truckActionEntity);
            currentTruckEntity.setIsSentToCloud(false);
            truckRepository.save(currentTruckEntity);
        }
*/

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



/*
    public boolean isEntranceAvailableForCamera2(String truckNumber) {
        if (!truckRepository.existsByIsFinishedFalseAndIsDeletedFalse()) {
            Platform.runLater(() -> mainController.showAlert(Alert.AlertType.WARNING, "Xatolik", "Hamma moshinalar chiqib ketgan!"));
            return false;
        }


        final Map<String, String>[] updatedTruckNumberHolder = new Map[1];
        Platform.runLater(() -> {
            updatedTruckNumberHolder[0] = mainController.showTruckNotFoundPopupWithSelection(truckNumber, getNotFinishedTrucks());
        });

        // Wait for Platform.runLater() to complete
        while (updatedTruckNumberHolder[0] == null) {
            try {
                Thread.sleep(50); // Small delay to prevent tight looping
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        Map<String, String> updatedTruckNumber = updatedTruckNumberHolder[0];
        if (updatedTruckNumber == null || updatedTruckNumber.isEmpty()) {
            return false;
        }

        String editedTruckNumber = updatedTruckNumber.get("textFieldValue");
        String selectedTruckNumber = updatedTruckNumber.get("dropDownValue");

        if (!selectedTruckNumber.equals(editedTruckNumber)) {
            List<TruckEntity> list = truckRepository.findByTruckNumberAndIsFinishedAndIsDeletedOrderByCreatedAtDesc(selectedTruckNumber, false, false);
            if (list.isEmpty()) {
                Platform.runLater(() -> mainController.showAlert(Alert.AlertType.ERROR, "Xatolik", selectedTruckNumber + " raqam topilmadi"));
                return false;
            }

            TruckEntity truckToUpdate = list.get(0);
            truckToUpdate.setOriginalTruckNumber(truckToUpdate.getTruckNumber());
            truckToUpdate.setTruckNumber(editedTruckNumber);
            truckRepository.save(truckToUpdate);
            Platform.runLater(() -> tableController.loadDataNow());
        }

        truckNumber = editedTruckNumber;

        List<TruckEntity> list = truckRepository.findByTruckNumberAndActionStatus(truckNumber, List.of(TruckAction.ENTRANCE, TruckAction.MANUAL_ENTRANCE), false, false);
        if (list.isEmpty()) {
            Platform.runLater(() -> mainController.showAlert(Alert.AlertType.WARNING, "Xatolik", "Bu moshina kirmagan (tarasi yo'q)!"));
            return false;
        }

        TruckEntity truckEntity = list.get(0);
        Instances.truckNumber = truckNumber;

        LocalDateTime nextEntranceTime = truckEntity.getNextEntranceTime();
        boolean b = nextEntranceTime.isBefore(LocalDateTime.now());
        if (!b) {
            Platform.runLater(() -> mainController.showAlert(Alert.AlertType.INFORMATION, "Info", nextEntranceTime.getHour() + ":" + nextEntranceTime.getMinute() + ":" + nextEntranceTime.getSecond() + " dan keyin kirishi mumkin!"));
        }
        return b;
    }
*/

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
}
