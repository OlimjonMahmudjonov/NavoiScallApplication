package uz.tenzorsoft.scaleapplication.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarInfoDto {
    private String carNumber;
    private String ownerPinfl;
    private Double quantity;
    private String driverName;
    private String productName;
    private Timestamp enterDate;
    private String model;
}
