package uz.tenzorsoft.scaleapplication.domain.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TableViewData {
    private Long id;
    private String enteredTruckNumber;
    private String enteredDate;
    private String enteredTime;
    private Double enteredWeight;
    private String enteredOnDuty;
    private String enteredActionStatus;
    private String exitedTruckNumber;
    private String exitedDate;
    private String exitedTime;
    private Double exitedWeight;
    private String exitedOnDuty;
    private String exitedActionStatus;
    private String minWeight;
    private String maxWeight;
    private String pickupWeight = "0.0";
    private String dropWeight = "0.0";
    private String productType = "";

}
