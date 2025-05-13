package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.enumerators.CargoStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CargoResponse {
    private Long id;
    private String truckNumber;
    private CargoStatus cargoStatus;
    private Double weight;
    private Long scaleId;
    private LocalDateTime createdAt;
    private Long idOnServer;
}
