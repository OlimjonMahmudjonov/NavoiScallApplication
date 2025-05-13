package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.*;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class TruckResponse {
    private Long id;
    private String  truckNumber;

    private List<AttachIdWithStatus> attaches = new ArrayList<>();
    private TruckAction enteredStatus;
    private LocalDateTime enteredAt;
    private Double enteredWeight;
    private String entranceConfirmedBy;

    private TruckAction exitedStatus;
    private LocalDateTime exitedAt;
    private Double exitedWeight;
    private String exitConfirmedBy;

}
