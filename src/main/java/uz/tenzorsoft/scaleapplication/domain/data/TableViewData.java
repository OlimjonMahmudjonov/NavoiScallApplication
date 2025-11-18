package uz.tenzorsoft.scaleapplication.domain.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

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
    private String productType = "ko'mir";

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TableViewData that = (TableViewData) obj;
        return Objects.equals(id, that.id) &&
                Objects.equals(enteredTruckNumber, that.enteredTruckNumber) &&
                Objects.equals(enteredDate, that.enteredDate) &&
                Objects.equals(enteredTime, that.enteredTime) &&
                Objects.equals(enteredWeight, that.enteredWeight) &&
                Objects.equals(enteredOnDuty, that.enteredOnDuty) &&
                Objects.equals(enteredActionStatus, that.enteredActionStatus) &&
                Objects.equals(exitedTruckNumber, that.exitedTruckNumber) &&
                Objects.equals(exitedDate, that.exitedDate) &&
                Objects.equals(exitedTime, that.exitedTime) &&
                Objects.equals(exitedWeight, that.exitedWeight) &&
                Objects.equals(exitedOnDuty, that.exitedOnDuty) &&
                Objects.equals(exitedActionStatus, that.exitedActionStatus) &&
                Objects.equals(minWeight, that.minWeight) &&
                Objects.equals(maxWeight, that.maxWeight) &&
                Objects.equals(pickupWeight, that.pickupWeight) &&
                Objects.equals(dropWeight, that.dropWeight) &&
                Objects.equals(productType, that.productType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enteredTruckNumber, enteredDate, enteredTime, enteredWeight, enteredOnDuty, enteredActionStatus, exitedTruckNumber, exitedDate, exitedTime, exitedWeight, exitedOnDuty, exitedActionStatus, minWeight, maxWeight, pickupWeight, dropWeight, productType);
    }

}
