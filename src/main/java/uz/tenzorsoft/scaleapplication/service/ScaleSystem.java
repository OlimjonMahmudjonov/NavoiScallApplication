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
    public static Integer RASP_GREEN_LIGHT_1 = 21;
    public static Integer RASP_GREEN_LIGHT_2 = 20;
    public static Integer RASP_OPEN_GATE_1 = 23;
    public static Integer RASP_CLOSE_GATE_1 = 24;
    public static Integer RASP_OPEN_GATE_2 = 25;
    public static Integer RASP_CLOSE_GATE_2 = 12;
    public static Integer RASP_SENSOR_1 = 17;
    public static Integer RASP_SENSOR_2 = 27;
    public static Integer RASP_SENSOR_3 = 22;
    /*--------------------------------------------------------------*/

    public static Integer COIL_GREEN_LIGHT_1 = 0;
    public static Integer COIL_RED_LIGHT_2 = 0;
    public static Integer COIL_GREEN_LIGHT_2 = 1;
    public static Integer COIL_OPEN_GATE_1 = 2;
    public static Integer COIL_CLOSE_GATE_1 = 3;
    public static Integer COIL_OPEN_GATE_2 = 4;
    public static Integer COIL_CLOSE_GATE_2 = 5;
    public static Integer COIL_SENSOR_1 = 15;
    public static Integer COIL_SENSOR_2 = 16;
    public static Integer COIL_SENSOR_3 = 17;
    public static String GOOGLE_DNS = "8.8.8.8";

    public static Integer RASP_GREEN_LIGHT_EXIT_1 = 5;
    public static Integer RASP_GREEN_LIGHT_EXIT_2 = 6;
    public static Integer RASP_OPEN_GATE_EXIT_1 = 2;
    public static Integer RASP_CLOSE_GATE_EXIT_1 = 3;
    public static Integer RASP_OPEN_GATE_EXIT_2 = 19;
    public static Integer RASP_CLOSE_GATE_EXIT_2 = 26;
    public static Integer RASP_SENSOR_EXIT_1 = 14;
    public static Integer RASP_SENSOR_EXIT_2 = 15;
    public static Integer RASP_SENSOR_EXIT_3 = 18;

    public static Integer COIL_GREEN_LIGHT_EXIT_1 = 0;
    public static Integer COIL_RED_LIGHT_EXIT_2 = 0;
    public static Integer COIL_GREEN_LIGHT_EXIT_2 = 1;
    public static Integer COIL_OPEN_GATE_EXIT_1 = 2;
    public static Integer COIL_CLOSE_GATE_EXIT_1 = 3;
    public static Integer COIL_OPEN_GATE_EXIT_2 = 4;
    public static Integer COIL_CLOSE_GATE_EXIT_2 = 5;
    public static Integer COIL_SENSOR_EXIT_1 = 15;
    public static Integer COIL_SENSOR_EXIT_2 = 16;
    public static Integer COIL_SENSOR_EXIT_3 = 17;

    public static Image greenLight = new Image("/images/green_light.png");
    public static Image redLight = new Image("/images/red_light.png");

    public static ModbusTCPTransaction transaction;
    public static TCPMasterConnection connection;
    public static InetAddress address;
    public static Integer truckPosition = -1;
    public static Integer truckExitPosition = -1;
    public static SerialPort scalePort;
    public static SerialPort scaleExitPort;

}
