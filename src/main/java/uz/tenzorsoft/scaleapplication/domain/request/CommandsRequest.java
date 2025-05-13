package uz.tenzorsoft.scaleapplication.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandsRequest {

    private Long id;
    private Long scaleId;
    private Boolean openGate1;
    private Boolean closeGate1;
    private Boolean weighing;
    private Boolean openGate2;
    private Boolean closeGate2;


}
