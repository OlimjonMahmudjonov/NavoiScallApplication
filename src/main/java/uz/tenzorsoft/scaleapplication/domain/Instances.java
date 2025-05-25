package uz.tenzorsoft.scaleapplication.domain;

import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import uz.tenzorsoft.scaleapplication.domain.entity.UserEntity;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;

import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.RASP_GREEN_LIGHT_EXIT_2;

import java.util.HashMap;
import java.util.Map;

public class Instances<T> {
    public static UserEntity currentUser = new UserEntity();
    public static TruckResponse currentTruck = new TruckResponse();
    public static TruckResponse currentExitTruck = new TruckResponse();

    public static String truckNumber = "";
    public static String truckExitNumber = "";
    public static String WEBSOCKET_URL = "wss://api-scale.mycoal.uz/ws";
    public static String SERVER_URL = "https://api-scale.mycoal.uz";

    public static String directory = "";
    public static boolean isTesting = false;
    public static boolean isConnectedToInternet = false;
    public static boolean isConnected = false;
    public static boolean gate1Connection = false;
    public static boolean gate2Connection = false;
    public static boolean gateExit1Connection = false;
    public static boolean gateExit2Connection = false;
    public static boolean kppgate1Connection = false;
    public static boolean kppgate2Connection = false;
    public static boolean kppgateExit1Connection = false;
    public static boolean kppgateExit2Connection = false;
    public static boolean camera1Connection = false;
    public static boolean camera2Connection = false;
    public static boolean camera3Connection = false;
    public static boolean camera4Connection = false;
    public static boolean cameraRezerv1Connection = false;
    public static boolean cameraRezerv2Connection = false;
    public static boolean sensor1Connection = false;
    public static boolean sensor2Connection = false;
    public static boolean sensor3Connection = false;
    public static boolean sensorExit1Connection = false;
    public static boolean sensorExit2Connection = false;
    public static boolean sensorExit3Connection = false;
    public static boolean isWaiting = false;
    public static boolean isExitWaiting = false;
    public static boolean isScaleControlOn = true;
    public static boolean isAvailableToConnect = false;
    public static boolean isRaspberryUsing = false;
    public static short cargoConfirmationStatus = -1;
    public static short cargoConfirmationExitStatus = -1;
    public static long firstGateEntranceTime = 0;
    public static long secondGateEntranceTime = 0;
    public static long firstExitGateEntranceTime = 0;
    public static long secondExitGateEntranceTime = 0;
    public static long kppfirstGateEntranceTime = 0;
    public static long kppsecondGateEntranceTime = 0;
    public static long kppfirstExitGateEntranceTime = 0;
    public static long kppsecondExitGateEntranceTime = 0;

    public static Map<Integer, DigitalOutput> outputPins = new HashMap<>();
    public static Map<Integer, DigitalInput> inputPins = new HashMap<>();

    public static int[] CONTROL_PINS = {RASP_GREEN_LIGHT_1, RASP_GREEN_LIGHT_2, RASP_OPEN_GATE_1, RASP_CLOSE_GATE_1, RASP_OPEN_GATE_2, RASP_CLOSE_GATE_2,RASP_GREEN_LIGHT_EXIT_1, RASP_GREEN_LIGHT_EXIT_2, RASP_OPEN_GATE_EXIT_1, RASP_CLOSE_GATE_EXIT_1, RASP_OPEN_GATE_EXIT_2, RASP_CLOSE_GATE_EXIT_2,KPP_CLOSE_GATE_EXIT_1,KPP_OPEN_GATE_EXIT_1,KPP_OPEN_GATE_EXIT_2,KPP_CLOSE_GATE_EXIT_2};
    public static int[] STATUS_PINS = {RASP_SENSOR_1, RASP_SENSOR_2, RASP_SENSOR_3,RASP_SENSOR_EXIT_1, RASP_SENSOR_EXIT_2, RASP_SENSOR_EXIT_3};

    public static Configurations configurations;



    public static void reinitializeAll() {
        isConnected = false;
    }

    public static Object reinitialize(Object obj) {
        obj = null;
        return obj;
    }

    public T reinitialize(T obj, T value) {
        obj = value;
        return obj;
    }

}
