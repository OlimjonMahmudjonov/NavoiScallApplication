package uz.tenzorsoft.scaleapplication.domain;

import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Settings {
    public static String CONTROLLER_IP;
    public static Integer CONTROLLER_PORT;
    public static Integer CONTROLLER_CONNECT_TIMEOUT;

    public static Integer CLOSE_GATE1_TIMEOUT;
    public static Integer CLOSE_GATE2_TIMEOUT;

    public static Integer PIN_IN_IN;
    public static Integer PIN_IN_OUT;
    public static Integer PIN_IN_IN2;
    public static Integer PIN_IN_OUT2;

    public static Integer CLOSE_GATE1_EXIT_TIMEOUT;
    public static Integer CLOSE_GATE2_EXIT_TIMEOUT;

    public static Integer PIN_OUT_IN;
    public static Integer PIN_OUT_OUT;
    public static Integer PIN_OUT_IN2;
    public static Integer PIN_OUT_OUT2;

    public static Long MYCOAL_SCALE_ID = 1L;
    public static Long SCALE_WEB_ID;

    public static Integer SCALE_TIMEOUT;
    public static Integer EXIT_TIMEOUT;
    public static String SCALE_PORT;
    public static String SCALE_EXIT_PORT;
    public static String PRINTER_NAME;

    public static String DATABASE_NAME;
    public static String USERNAME;
    public static String PASSWORD;
    public static String CAMERA_1;
    public static String CAMERA_REZERV1;
    public static String CAMERA_REZERV2;
    public static String CAMERA_2;
    public static String CAMERA_3;
    public static String CAMERA_4;
    public static boolean IS_RASPBERRY_USING;


    public static Integer KPP_CLOSE_GATE1_TIMEOUT;
    public static Integer KPP_CLOSE_GATE2_TIMEOUT;
    public static Integer KPP_PIN_IN_IN;
    public static Integer KPP_PIN_IN_OUT;
    public static Integer KPP_EXIT_PIN_IN_IN;
    public static Integer KPP_EXIT_PIN_IN_OUT;

    public static Integer SENSOR_IN1;
    public static Integer SENSOR_IN2;
    public static Integer SENSOR_IN3;
    public static Integer SENSOR_OUT1;
    public static Integer SENSOR_OUT2;
    public static Integer SENSOR_OUT3;

    private SerialPort serialPort;

    public Settings(String scalePort) {
        this.serialPort = SerialPort.getCommPort(scalePort);
    }
}
