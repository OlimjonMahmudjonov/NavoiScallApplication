package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.CargoEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.ActionStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TableService {

    @Autowired
    @Lazy
    private CargoService cargoService;

    public TableViewData entityToTableData(TruckEntity truckEntity) {
        TableViewData data = new TableViewData();
        data.setId(truckEntity.getId());
        double enteredWeight = 0.0;
        double exitedWeight = 0.0;
        boolean isActionAvailable = false;
        TruckAction entranceAction = TruckAction.NO_ACTION;
        TruckAction exitAction = TruckAction.NO_ACTION;

        if (truckEntity.getTruckActions() == null || truckEntity.getTruckActions().isEmpty()) return null;

        for (TruckActionEntity action : truckEntity.getTruckActions()) {

           /// if (action.getActionStatus() != ActionStatus.COMPLETE && action.getWeight() == 0) continue;

//            if (action.getActionStatus() != ActionStatus.COMPLETE) continue;


            switch (action.getAction()) {
                case ENTRANCE, MANUAL_ENTRANCE -> {
                    data.setEnteredTruckNumber(truckEntity.getTruckNumber());
                    data.setEnteredDate(getDate(action.getCreatedAt()));
                    data.setEnteredWeight(action.getWeight() == null ? 0.0 : action.getWeight());
                    enteredWeight = action.getWeight() == null ? 0.0 : action.getWeight();
                    entranceAction = action.getAction();
                    data.setEnteredTime(getTime(action.getCreatedAt()));
                    data.setEnteredOnDuty(action.getOnDuty() == null ? "unknown" : action.getOnDuty().getPhoneNumber());

                    if (action.getActionStatus() != null) {
                       data.setEnteredActionStatus(action.getActionStatus().name());
                    }
                            isActionAvailable = true;

                }
                case EXIT, MANUAL_EXIT -> {
                    data.setExitedTruckNumber(truckEntity.getTruckNumber());
                    data.setExitedDate(getDate(action.getCreatedAt()));
                    data.setExitedWeight(action.getWeight() == null ? 0.0 : action.getWeight());
                    exitedWeight = action.getWeight() == null ? 0.0 : action.getWeight();
                    exitAction = action.getAction();
                    data.setExitedTime(getTime(action.getCreatedAt()));
                    data.setExitedOnDuty(action.getOnDuty() == null ? "unknown" : action.getOnDuty().getPhoneNumber());

                    if (action.getActionStatus() != null) {
                        data.setExitedActionStatus(action.getActionStatus().name());
                    }


                    isActionAvailable = true;
                }
            }
        }
        if (!isActionAvailable) return null;
        data.setProductType(truckEntity.getProducts() == null ? "" : truckEntity.getProducts().getName());
        CargoEntity cargo = cargoService.findByTruckId(truckEntity.getId());
        if (cargo == null) return data;
        switch (cargo.getCargoStatus()) {
            case PICKUP -> data.setPickupWeight(String.valueOf(cargo.getNetWeight()));
            case DROP -> data.setDropWeight(String.valueOf(cargo.getNetWeight()));
        }
        data.setEnteredActionStatus(getTruckActionStatus(truckEntity, entranceAction));
        data.setExitedActionStatus(getTruckActionStatus(truckEntity, exitAction));
//        data.setActionStatus(getStatus(truckEntity));
        data.setMinWeight(String.valueOf(Math.min(enteredWeight, exitedWeight)));
        data.setMaxWeight(String.valueOf(Math.max(enteredWeight, exitedWeight)));
        return data;
    }

    private String getTime(LocalDateTime dateTime) {
        return String.format("%02d:%02d", dateTime.getHour(), dateTime.getMinute());
    }

    private String getDate(LocalDateTime dateTime) {
        return dateTime.getDayOfMonth() + "." + dateTime.getMonthValue() + "." + dateTime.getYear();
    }

//    private String getStatus(TruckEntity truckEntity) {
//        StringBuilder status = new StringBuilder();
//        for (TruckActionEntity action : truckEntity.getTruckActions()) {
//            if (action.getActionStatus() != null) {
//                status.append(action.getActionStatus().name()).append(",");
//            }
//        }
//        return status.toString();
//    }

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
}
