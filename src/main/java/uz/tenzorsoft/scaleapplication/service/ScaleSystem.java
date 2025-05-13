package uz.tenzorsoft.scaleapplication.service;

import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import javafx.scene.image.Image;
import uz.tenzorsoft.scaleapplication.domain.Settings;

import java.net.InetAddress;

import static uz.tenzorsoft.scaleapplication.domain.Settings.SCALE_PORT;

public class ScaleSystem {
    /*--------------------------------------------------------------
                            Raspberry pins
    --------------------------------------------------------------*/
    public static final Integer RASP_GREEN_LIGHT_1 = 21;
    public static final Integer RASP_GREEN_LIGHT_2 = 20;
    public static final Integer RASP_OPEN_GATE_1 = 23;
    public static final Integer RASP_CLOSE_GATE_1 = 24;
    public static final Integer RASP_OPEN_GATE_2 = 25;
    public static final Integer RASP_CLOSE_GATE_2 = 12;
    public static final Integer RASP_SENSOR_1 = 17;
    public static final Integer RASP_SENSOR_2 = 27;
    public static final Integer RASP_SENSOR_3 = 22;
    /*--------------------------------------------------------------*/

    public static final Integer COIL_GREEN_LIGHT_1 = 0;
    public static final Integer COIL_RED_LIGHT_2 = 0;
    public static final Integer COIL_GREEN_LIGHT_2 = 1;
    public static final Integer COIL_OPEN_GATE_1 = 2;
    public static final Integer COIL_CLOSE_GATE_1 = 3;
    public static final Integer COIL_OPEN_GATE_2 = 4;
    public static final Integer COIL_CLOSE_GATE_2 = 5;
    public static final Integer COIL_SENSOR_1 = 15;
    public static final Integer COIL_SENSOR_2 = 16;
    public static final Integer COIL_SENSOR_3 = 17;
    public static final String GOOGLE_DNS = "8.8.8.8";

    public static final Integer RASP_GREEN_LIGHT_EXIT_1 = 21;
    public static final Integer RASP_GREEN_LIGHT_EXIT_2 = 20;
    public static final Integer RASP_OPEN_GATE_EXIT_1 = 23;
    public static final Integer RASP_CLOSE_GATE_EXIT_1 = 24;
    public static final Integer RASP_OPEN_GATE_EXIT_2 = 25;
    public static final Integer RASP_CLOSE_GATE_EXIT_2 = 12;
    public static final Integer RASP_SENSOR_EXIT_1 = 30;
    public static final Integer RASP_SENSOR_EXIT_2 = 31;
    public static final Integer RASP_SENSOR_EXIT_3 = 32;

    public static final Integer COIL_GREEN_LIGHT_EXIT_1 = 0;
    public static final Integer COIL_RED_LIGHT_EXIT_2 = 0;
    public static final Integer COIL_GREEN_LIGHT_EXIT_2 = 1;
    public static final Integer COIL_OPEN_GATE_EXIT_1 = 2;
    public static final Integer COIL_CLOSE_GATE_EXIT_1 = 3;
    public static final Integer COIL_OPEN_GATE_EXIT_2 = 4;
    public static final Integer COIL_CLOSE_GATE_EXIT_2 = 5;
    public static final Integer COIL_SENSOR_EXIT_1 = 15;
    public static final Integer COIL_SENSOR_EXIT_2 = 16;
    public static final Integer COIL_SENSOR_EXIT_3 = 17;

    public static final Image greenLight = new Image("/images/green_light.png");
    public static final Image redLight = new Image("/images/red_light.png");

    public static ModbusTCPTransaction transaction;
    public static TCPMasterConnection connection;
    public static InetAddress address;
    public static Integer truckPosition = -1;
    public static Integer truckExitPosition = -1;
    public static SerialPort scalePort;
    public static SerialPort scaleExitPort;

}
