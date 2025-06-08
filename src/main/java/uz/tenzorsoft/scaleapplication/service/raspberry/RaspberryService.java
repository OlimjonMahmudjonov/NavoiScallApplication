package uz.tenzorsoft.scaleapplication.service.raspberry;

import com.ghgande.j2mod.modbus.ModbusException;
import javafx.scene.image.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.enumerators.PinState;
import uz.tenzorsoft.scaleapplication.service.ScaleSystem;
import uz.tenzorsoft.scaleapplication.ui.ImageController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static uz.tenzorsoft.scaleapplication.domain.Instances.gate1Connection;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnected;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;

@Service
@RequiredArgsConstructor
public class RaspberryService {

    private final GpioControl gpioControl;
    private final ImageController imageController;

    // ===============================
    // ENTRANCE GATE 1 METHODS
    // ===============================

    public boolean openGate1() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }

        sendCommand(RASP_GREEN_LIGHT_1, PinState.HIGH);
        sendCommand(RASP_OPEN_GATE_1, PinState.HIGH);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            sendCommand(RASP_OPEN_GATE_1, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);

        return true;
    }

    public boolean openGate1(int truckPosition) throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
        imageController.setIncomePhoto(new Image("/images/in/0.png"));
        ScaleSystem.truckPosition = truckPosition;
        return openGate1();
    }

    public boolean closeGate1() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }

        sendCommand(RASP_GREEN_LIGHT_1, PinState.LOW);
        sendCommand(RASP_CLOSE_GATE_1, PinState.HIGH);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            sendCommand(RASP_CLOSE_GATE_1, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);

        return true;
    }

    // ===============================
    // ENTRANCE GATE 2 METHODS
    // ===============================

    public boolean openGate2() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
        sendCommand(RASP_GREEN_LIGHT_2, PinState.HIGH);
        sendCommand(RASP_OPEN_GATE_2, PinState.HIGH);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            sendCommand(RASP_OPEN_GATE_2, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);
        return true;
    }

    public boolean openGate2(int truckPosition) throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
        ScaleSystem.truckPosition = truckPosition;
        return openGate2();
    }

    public boolean closeGate2() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
        sendCommand(RASP_GREEN_LIGHT_2, PinState.LOW);
        sendCommand(RASP_CLOSE_GATE_2, PinState.HIGH);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            sendCommand(RASP_CLOSE_GATE_2, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);
        return true;
    }

    // ===============================
    // EXIT GATE 1 METHODS
    // ===============================

    public boolean openExitGate1() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }

        sendCommand(RASP_GREEN_LIGHT_EXIT_1, PinState.HIGH);
        sendCommand(RASP_OPEN_GATE_EXIT_1, PinState.HIGH);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            sendCommand(RASP_OPEN_GATE_EXIT_1, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);

        return true;
    }

    public boolean openExitGate1(int truckPosition) throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
        imageController.setOutPhoto(new Image("/images/out/0.png"));
        ScaleSystem.truckExitPosition = truckPosition;
        return openExitGate1();
    }

    public boolean closeExitGate1() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }

        sendCommand(RASP_GREEN_LIGHT_EXIT_1, PinState.LOW);
        sendCommand(RASP_CLOSE_GATE_EXIT_1, PinState.HIGH);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            sendCommand(RASP_CLOSE_GATE_EXIT_1, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);

        return true;
    }

    // ===============================
    // EXIT GATE 2 METHODS
    // ===============================

    public boolean openExitGate2() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
        sendCommand(RASP_GREEN_LIGHT_EXIT_2, PinState.HIGH);
        sendCommand(RASP_OPEN_GATE_EXIT_2, PinState.HIGH);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            sendCommand(RASP_OPEN_GATE_EXIT_2, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);
        return true;
    }

    public boolean openExitGate2(int truckPosition) throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
        ScaleSystem.truckExitPosition = truckPosition;
        return openExitGate2();
    }

    public boolean closeExitGate2() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
        sendCommand(RASP_GREEN_LIGHT_EXIT_2, PinState.LOW);
        sendCommand(RASP_CLOSE_GATE_EXIT_2, PinState.HIGH);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            sendCommand(RASP_CLOSE_GATE_EXIT_2, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);
        return true;
    }

    // ===============================
    // KPP GATE 1 METHODS
    // ===============================

    public boolean kppopenGate1() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
//        sendCommand(RASP_GREEN_LIGHT_2, PinState.HIGH);
        sendCommand(KPP_OPEN_GATE_EXIT_1, PinState.HIGH);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() ->{
            sendCommand(KPP_OPEN_GATE_EXIT_1, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);

        return true;
    }

    public boolean kppopenGate1(int truckPosition) throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
//        imageController.setIncomePhoto(new Image("/images/in/0.png"));
        ScaleSystem.truckPosition = truckPosition;
        return kppopenGate1();
    }

    public boolean kppcloseGate1() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }

//        sendCommand(RASP_GREEN_LIGHT_1, PinState.LOW);
        sendCommand(KPP_CLOSE_GATE_EXIT_1, PinState.HIGH);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            sendCommand(KPP_CLOSE_GATE_EXIT_1, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);

        return true;
    }

    // ===============================
    // KPP GATE 2 METHODS
    // ===============================

    public boolean kppopenGate2() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
//        sendCommand(RASP_GREEN_LIGHT_2, PinState.HIGH);
        sendCommand(KPP_OPEN_GATE_EXIT_2, PinState.HIGH);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() ->{
            sendCommand(KPP_OPEN_GATE_EXIT_2, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);

        return true;
    }

    public boolean kppcloseGate2() throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }

//        sendCommand(RASP_GREEN_LIGHT_1, PinState.LOW);
        sendCommand(KPP_CLOSE_GATE_EXIT_2, PinState.HIGH);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            sendCommand(KPP_CLOSE_GATE_EXIT_2, PinState.LOW);
            scheduler.shutdown();
        }, 500, TimeUnit.MILLISECONDS);

        return true;
    }

    // ===============================
    // PRIVATE HELPER METHOD
    // ===============================

    private void sendCommand(Integer pinNumber, PinState state) {
        gpioControl.controlPin(pinNumber, state);
    }

}