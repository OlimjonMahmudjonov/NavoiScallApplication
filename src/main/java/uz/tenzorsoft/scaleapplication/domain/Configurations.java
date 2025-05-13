package uz.tenzorsoft.scaleapplication.domain;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Configurations implements Serializable {

    private static final long serialVersionUID = 1L;

    private String controllerIp;
    private Integer controllerPort;
    private Integer controllerConnectTimeout;

    private Integer closeGate1Timeout;
    private Integer closeGate2Timeout;

    private Long mycoalScaleId;
    private Long scaleWebId;

    private Integer scaleTimeout;
    private Integer exitTimeout;
    private String scalePort;
    private String scaleExitPort;
    private String printerName;

    private String databaseName;
    private String username;
    private String password;
    private String camera1;
    private String camera2;
    private String camera3;

    private Integer raspberryUsing = 0;
}
