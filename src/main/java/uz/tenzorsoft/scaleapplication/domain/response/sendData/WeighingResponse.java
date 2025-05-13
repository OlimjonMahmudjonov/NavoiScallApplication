package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.enumerators.CargoStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeighingResponse extends BaseResponse {

    private Long scaleId;

    private String truckNumber;
    private CargoStatus cargoStatus;
    private Double weight;
    private LocalDateTime createdAt;
}
