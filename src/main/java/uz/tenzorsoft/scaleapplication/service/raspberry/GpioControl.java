package uz.tenzorsoft.scaleapplication.service.raspberry;

import com.pi4j.Pi4J;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalOutputProvider;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalInputProvider;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.platform.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.enumerators.PinState;
import uz.tenzorsoft.scaleapplication.service.LogService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Consumer;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;

@Service
@RequiredArgsConstructor
public class GpioControl {
    private final LogService logService;

    private Context pi4jOut;
    private Context pi4jIn;

    public void initialize() {
        try {
            if (pi4jOut == null) {
                pi4jOut = Pi4J.newContextBuilder()
                        .add(GpioDDigitalOutputProvider.newInstance())
                        .build();
            }

            if (pi4jIn == null) {
                pi4jIn = Pi4J.newContextBuilder()
                        .add(GpioDDigitalInputProvider.newInstance())
                        .build();
            }

            Platform platform = pi4jOut.platform();
            System.out.println("Platform: " + (platform != null ? platform.name() : "Not Initialized"));

            controlPinsInitialization(pi4jOut);
            statusPinsInitialization(pi4jIn);
            setStatusListeners();
        } catch (Exception e) {
            System.err.println("GPIO pin " + pi4jIn + " da xatolik: " + e.getMessage());
            System.err.println("GPIO pin " + pi4jOut + " da xatolik: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setStatusListeners() {
        Map<Integer, Consumer<Boolean>> pinToSensorMap = Map.of(
                RASP_SENSOR_1, (status) -> sensor1Connection = status,
                RASP_SENSOR_2, (status) -> sensor2Connection = status,
                RASP_SENSOR_3, (status) -> sensor3Connection = status,
                RASP_SENSOR_EXIT_1, (status) -> sensorExit1Connection = status,
                RASP_SENSOR_EXIT_2, (status) -> sensorExit2Connection = status,
                RASP_SENSOR_EXIT_3, (status) -> sensorExit3Connection = status
        );

        for (int pin : STATUS_PINS) {
            try {
                DigitalInput input = inputPins.get(pin);
                if (input != null && pinToSensorMap.containsKey(pin)) {
                    Consumer<Boolean> sensorUpdater = pinToSensorMap.get(pin);
                    input.addListener((event) -> {
                        boolean isHigh = event.state().isHigh();
                        sensorUpdater.accept(isHigh);
                    });
                }
            } catch (Exception e) {
                System.out.println("mana hato");
            }
        }
    }

    public boolean controlPin(int pin, PinState state) {
        System.out.println("pin = " + pin + " -> " + state);
        if (!outputPins.containsKey(pin)) {
            System.out.println("Pin number not found " + pin);
            resetPin(pin);
            if (!outputPins.containsKey(pin)){
                throw new RuntimeException("Pin number not found " + pin + " again");
            }
        }

        DigitalOutput output = outputPins.get(pin);

        try {
            if (state == PinState.HIGH) {
                output.low();
                return true;
            } else if (state == PinState.LOW) {
                output.high();
                return true;
            }
        } catch (Exception e) {
            System.out.println("control pinsda  hato");
        }
        return false;
    }

    private void statusPinsInitialization(Context pi4jIn) {
        for (int pinAddress : STATUS_PINS) {
            try {
                DigitalInputConfigBuilder config = DigitalInput.newConfigBuilder(pi4jIn)
                        .id("pin-" + pinAddress)
                        .name("Status Pin " + pinAddress)
                        .address(pinAddress)
                        .pull(PullResistance.PULL_DOWN);
                inputPins.put(pinAddress, pi4jIn.create(config));
                isAvailableToConnect = true;
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    private void resetPin(int pin) {
        try {
            System.out.println("Pin qaytadan o'rnatilyapti: " + pin);
            DigitalOutputConfigBuilder config = DigitalOutput.newConfigBuilder(pi4jOut)
                    .id("pin-" + pin)
                    .name("Control Pin " + pin)
                    .address(pin)
                    .shutdown(DigitalState.LOW)
                    .initial(DigitalState.LOW);
            outputPins.put(pin, pi4jOut.create(config));
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void controlPinsInitialization(Context pi4jOut) {
        for (int pinAddress : CONTROL_PINS) {
            try {
                DigitalOutputConfigBuilder config = DigitalOutput.newConfigBuilder(pi4jOut)
                        .id("pin-" + pinAddress)
                        .name("Control Pin " + pinAddress)
                        .address(pinAddress)
                        .shutdown(DigitalState.HIGH)
                        .initial(DigitalState.LOW);
                outputPins.put(pinAddress, pi4jOut.create(config));
                isAvailableToConnect = true;
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    public void shutdown() {
        sensor1Connection = false;
        sensor2Connection = false;
        sensor3Connection = false;
        gate1Connection = false;
        gate2Connection = false;
        kppgate1Connection = false;
        kppgate2Connection = false;
        sensorExit1Connection = false;
        sensorExit2Connection = false;
        sensorExit3Connection = false;
        gateExit1Connection = false;
        gateExit2Connection = false;
        if (pi4jOut != null) pi4jOut.shutdown();
        if (pi4jIn != null) pi4jIn.shutdown();
    }

    //    public void shutdownCompletely() {
//        if (pi4jOut != null) pi4jOut.shutdown();
//        if (pi4jIn != null) pi4jIn.shutdown();
//    }
//
    public void shutdownCompletely() {
        if (pi4jOut != null) {
            if (outputPins != null && !outputPins.isEmpty()) {
                outputPins.forEach((pin, digitalOutput) -> {
                    try {
                        String pinId = digitalOutput.id(); // Get the ID of the pin
                        pi4jOut.registry().remove(pinId); // Unregister the pin using its ID
                    } catch (Exception e) {
                        System.err.println("Error releasing pin: " + pin + " -> " + e.getMessage());
                    }
                });
            } else {
                System.err.println("No DigitalOutput objects found in registry.");
            }

            try {
                Thread.sleep(500); // Small delay before shutting down
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            pi4jOut.shutdown();
        }

        if (pi4jIn != null) {
            pi4jIn.shutdown();
        }
    }


    public void getSensorStatuses() {
        for (int i = 0; i < STATUS_PINS.length; i++) {
            try {
                DigitalInput input = inputPins.get(STATUS_PINS[i]);
                if (input != null) {
                    switch (i) {
                        case 0 -> {
                            sensor1Connection = input.isHigh();
                            System.out.println(input.isHigh());
                        }

                        case 1 -> {
                            sensor2Connection = input.isHigh();
                            System.out.println(input.isHigh());
                        }
                        case 2 -> {
                            sensor3Connection = input.isHigh();
                            System.out.println(input.isHigh());
                        }
                        case 3 -> {
                            sensorExit1Connection = input.isHigh();
                            System.out.println(input.isHigh());
                        }
                        case 4 -> {
                            sensorExit2Connection = input.isHigh();
                            System.out.println(input.isHigh());
                        }
                        case 5 -> {
                            sensorExit3Connection = input.isHigh();
                            System.out.println(input.isHigh());
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("mana hato");
            }
        }
    }

    public void removeAllPins() {
        if (pi4jOut != null && outputPins != null && !outputPins.isEmpty()) {
            for (int controlPin : CONTROL_PINS) {
                pi4jOut.registry().remove("pin-" + controlPin);
            }
            outputPins.clear();
        }
        if (pi4jIn != null && inputPins != null && !inputPins.isEmpty()) {
            for (int controlPin : STATUS_PINS) {
                pi4jIn.registry().remove("pin-" + controlPin);
            }
            inputPins.clear();
        }
    }

    public void soutStatuses() {

        for (int pin : STATUS_PINS) {
            String directionPath = "/sys/class/gpio/gpio" + pin + "/direction";

            try {
                // Fayldan yo'nalishni o'qish (in/out)
                String direction = Files.readString(Paths.get(directionPath)).trim();
                System.out.println(pin + " -> " + direction);
            } catch (IOException e) {
                System.out.println(pin + " -> not exported or inaccessible");
            }
        }
        for (int pin : CONTROL_PINS) {
            String directionPath = "/sys/class/gpio/gpio" + pin + "/direction";

            try {
                // Fayldan yo'nalishni o'qish (in/out)
                String direction = Files.readString(Paths.get(directionPath)).trim();
                System.out.println(pin + " -> " + direction);
            } catch (IOException e) {
                System.out.println(pin + " -> not exported or inaccessible");
            }
        }
//        for (int i = 0; i < CONTROL_PINS.length; i++) {
//            DigitalOutput output = outputPins.get(CONTROL_PINS[i]);
//            if (output != null)
//                System.out.println(output.isHigh() ? CONTROL_PINS[i] + "-High" : CONTROL_PINS[i] + "-Low");
//        }
    }

    public boolean isOutputPinInitialized(int pinAddress) {
        return outputPins.containsKey(pinAddress) && outputPins.get(pinAddress) != null;
    }
}

