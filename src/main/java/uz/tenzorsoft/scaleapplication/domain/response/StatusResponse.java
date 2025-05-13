package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class StatusResponse {
    private Boolean controller;
    private Boolean gate1;
    private Boolean gate2;
    private Boolean camera1;
    private Boolean camera2;
    private Boolean camera3;
    private Boolean sensor1;
    private Boolean sensor2;
    private Boolean sensor3;
    private Long scaleId;
}
