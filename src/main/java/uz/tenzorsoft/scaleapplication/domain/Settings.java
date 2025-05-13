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

    public static Long MYCOAL_SCALE_ID;
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
    public static String CAMERA_2;
    public static String CAMERA_3;
    public static boolean IS_RASPBERRY_USING;

    private SerialPort serialPort;

    public Settings(String scalePort) {
        this.serialPort = SerialPort.getCommPort(scalePort);
    }
}
