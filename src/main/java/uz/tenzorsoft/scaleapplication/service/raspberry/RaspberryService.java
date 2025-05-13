package uz.tenzorsoft.scaleapplication.service.raspberry;

import com.ghgande.j2mod.modbus.ModbusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.enumerators.PinState;
import uz.tenzorsoft.scaleapplication.service.ScaleSystem;

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

    public boolean openGate2(int truckPosition) throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
        ScaleSystem.truckPosition = truckPosition;
        return openGate2();
    }

    public boolean openExitGate2(int truckPosition) throws Exception {
        if (!isConnected) {
            throw new RuntimeException("Raspberryga ulanmagan");
        }
        ScaleSystem.truckExitPosition = truckPosition;
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

    private void sendCommand(Integer pinNumber, PinState state) {
        gpioControl.controlPin(pinNumber, state);
    }

}
