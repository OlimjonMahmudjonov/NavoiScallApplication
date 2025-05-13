package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActionResponse extends BaseResponse {

    private List<Long> attachIds;

    private String truckNumber;
    private TruckAction enteredStatus;
    private LocalDateTime enteredAt;
    private Double enteredWeight;
    private String entranceConfirmedBy;
    private TruckAction exitedStatus;
    private LocalDateTime exitedAt;
    private Double exitedWeight;
    private String exitConfirmedBy;

}
