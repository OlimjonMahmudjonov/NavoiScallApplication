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
    private Integer pinInIn;
    private Integer pinInOut;
    private Integer pinInIn2;
    private Integer pinInOut2;

    private Integer pinOutIn;
    private Integer pinOutOut;
    private Integer pinOutIn2;
    private Integer pinOutOut2;

    private Integer sensorIn1;
    private Integer sensorIn2;
    private Integer sensorIn3;
    private Integer sensorOut1;
    private Integer sensorOut2;
    private Integer sensorOut3;

    private Integer closeGateExit1Timeout;
    private Integer closeGateExit2Timeout;
    private Integer pinOut;
    private Integer pinOut2;

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
    private String camera4;
    private String cameraRezerv1;
    private String cameraRezerv2;

    private Integer kppcloseGate1Timeout;
    private Integer kppcloseGate2Timeout;
    private Integer kpppinInIn;
    private Integer kpppinInOut;
    private Integer kpppinOutIn;
    private Integer kpppinOutOut;


    private Integer raspberryUsing = 0;
}
