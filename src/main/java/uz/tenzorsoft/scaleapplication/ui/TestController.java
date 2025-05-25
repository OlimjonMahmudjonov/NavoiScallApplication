package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.service.AttachService;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.service.raspberry.GpioControl;
import uz.tenzorsoft.scaleapplication.ui.components.TruckScalingController;

import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.truckPosition;

@Component
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kpp")

public class TestController implements BaseController {

    private final ExecutorService executors;
    private final AttachService attachService;
    private final TruckService truckService;
    private final TableController tableController;
    private final ButtonController buttonController;
    private final LogService logService;
    @Lazy
    private final MainController mainController;
    @Lazy
    private final TruckScalingController truckScalingController;
    private final GpioControl gpioControl;

    @FXML
    private Pane testSwitchPane, sensor1Pane, sensor2Pane, sensor3Pane;

    @FXML
    private Label testLabel, sensor1Label, sensor2Label, sensor3Label;

    @FXML
    private ToggleSwitch sensor1Switch, sensor2Switch, sensor3Switch, testStatusSwitch;
    @FXML
    private ToggleSwitch gate1Switch, gate2Switch;  // Added gate switches

    @FXML
    private TextField weighInputField;

    @FXML
    private TextField truckPositionField;

    @FXML
    private TextField truckNumberFieldCamera1;
    @FXML
    private TextField truckNumberFieldCamera2;

    @FXML
    private Button camera1Button, camera2Button, weighButton;

    @FXML
    public void initialize() {
        // Initialize truck number fields
        truckNumberFieldCamera1.setEditable(true);
        truckNumberFieldCamera2.setEditable(true);

        // Listener for the "Test" switch
        testStatusSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            isTesting = newValue;
            updateToggleSwitchesState();
            updateTruckPosition();
            if (isRaspberryUsing) gpioControl.getSensorStatuses();
        });
        gate1Switch.setDisable(true); // Disable Gate 1
        gate2Switch.setDisable(true); // Disable Gate 2
        truckNumberFieldCamera1.setDisable(true);
        truckNumberFieldCamera2.setDisable(true);
        truckPositionField.setDisable(true);
        camera1Button.setDisable(true);
        camera2Button.setDisable(true);
        weighButton.setDisable(true);


        // Initialize the toggle states
        updateToggleSwitchesState();
        updateTruckPosition();
    }


    private void updateToggleSwitchesState() {
        boolean enableSensors = isTesting;

        // Enable or disable sensor switches based on "Test" switch
//        sensor1Switch.setDisable(!enableSensors);
//        sensor2Switch.setDisable(!enableSensors);
//        sensor3Switch.setDisable(!enableSensors);
        weighInputField.setDisable(!enableSensors);

        // Set switch state depending on test mode
        if (isTesting) {
            // Turn on sensors automatically if "Test" is on
//            sensor1Switch.setSelected(true);
//            sensor2Switch.setSelected(true);
//            sensor3Switch.setSelected(true);
            truckNumberFieldCamera1.setDisable(false);
            truckNumberFieldCamera2.setDisable(false);
            truckPositionField.setDisable(false);
            camera1Button.setDisable(false);
            camera2Button.setDisable(false);
            weighButton.setDisable(false);
        } else {
            // Turn off all switches if "Test" is off
//            sensor1Switch.setSelected(false);
//            sensor2Switch.setSelected(false);
//            sensor3Switch.setSelected(false);
            gate1Switch.setSelected(false);
            gate2Switch.setSelected(false);
            truckPositionField.setText(""); // Clear truck position
        }
    }

    public void getTruckNumberFromCamera1() {
        String truckNumber = truckNumberFieldCamera1.getText();
        if (!truckNumber.isEmpty()) {
//            if (!truckService.isValidTruckNumber(truckNumber) && !truckService.isStandard(truckNumber)) {
//                showAlert(Alert.AlertType.WARNING, "Not match", "Truck number does not match: " + truckNumber);
//                return;
//            }
            if (!truckService.isEntranceAvailableForCamera1(truckNumber)) {
                // showAlert(Alert.AlertType.WARNING, "Not available", "Entrance not available: " + truckNumber);
                return;
            }
            AttachResponse response = attachService.getCameraImgTesting();
            Long attachId = response.getId();
            currentTruck.getAttaches().add(new AttachIdWithStatus(attachId, AttachStatus.ENTRANCE_PHOTO));
            currentTruck.setTruckNumber(truckNumber);
            currentTruck.setEnteredStatus(TruckAction.ENTRANCE);
            truckService.saveTruck(currentTruck, 1, response);
            gate1Switch.setSelected(true);
            tableController.addLastRecord();
            truckPosition = 0;
            firstGateEntranceTime = System.currentTimeMillis();
            System.out.println("Saving truck number: " + truckNumber + " from Camera 1");
            truckNumberFieldCamera1.clear(); // Clear the text field after saving
        }
    }

    public void getTruckNumberFromCamera2() {
        String truckNumber = truckNumberFieldCamera2.getText();
        if (!truckNumber.isEmpty()) {
//            if (!truckService.isValidTruckNumber(truckNumber)) {
//                showAlert(Alert.AlertType.WARNING, "Not match", "Truck number does not match: " + truckNumber);
//                return;
//            }
            if (!truckService.isNotFinishedTrucksExists()) {
                // showAlert(Alert.AlertType.WARNING, "Not found", "All trucks are exited!");
                return;
            }
            if (!truckService.isEntranceAvailableForCamera2(truckNumber)) {
                // showAlert(Alert.AlertType.WARNING, "Not found", "Entrance not available: " + truckNumber);
                return;
            }
            AttachResponse response = attachService.getCameraImgTesting();
            Long attachId = response.getId();
            currentTruck.getAttaches().add(new AttachIdWithStatus(attachId, AttachStatus.EXIT_PHOTO));
            currentTruck.setTruckNumber(Instances.truckNumber);
            currentTruck.setExitedStatus(TruckAction.EXIT);
            truckService.saveTruck(currentTruck, 2, response);
            gate2Switch.setSelected(true);
            truckPosition = 7;
            truckNumberFieldCamera2.clear();
        }
    }

    public void setWeigh() {
        if ((truckPosition == 2 || truckPosition == 5) && !truckScalingController.isScaled()) {
            truckScalingController.setWeigh(Double.parseDouble(weighInputField.getText()));
            truckScalingController.setScaled(true);
        }
    }

    private void updateTruckPosition() {
        if (isTesting) {
            truckPositionField.setText("" + truckPosition); // Example static position
        } else {
            truckPositionField.setText("");
        }
    }

    public void setTestingStatusLabel() {
        testStatusSwitch.setSelected(!testStatusSwitch.isSelected());
        isTesting = testStatusSwitch.isSelected();
    }

    public void setSensor1StatusLabel() {
        if (isTesting || true) {
            sensor1Switch.setSelected(!sensor1Switch.isSelected());
            sensor1Connection = sensor1Switch.isSelected();
        }
    }

    public void setSensor2StatusLabel() {
        if (isTesting || true) {
            sensor2Switch.setSelected(!sensor2Switch.isSelected());
            sensor2Connection = sensor2Switch.isSelected();
        }
    }

    public void setSensor3StatusLabel() {
        if (isTesting || true) {
            sensor3Switch.setSelected(!sensor3Switch.isSelected());
            sensor3Connection = sensor3Switch.isSelected();
        }
    }

    public void setTestingStatusSwitch() {
        isTesting = testStatusSwitch.isSelected();
    }

    public void setSensor1StatusSwitch() {
        sensor1Connection = sensor1Switch.isSelected();
    }

    public void setSensor2StatusSwitch() {
        sensor2Connection = sensor2Switch.isSelected();
    }

    public void setSensor3StatusSwitch() {
        sensor3Connection = sensor3Switch.isSelected();
    }

    // Gate 1 status handler
    public void setGate1StatusSwitch() {
        if (isTesting) {
            gate1Connection = gate1Switch.isSelected(); // Update gate status manually when testing is on
            truckPosition = 0; // Update position or any other logic as needed
        }
    }

    // Gate 2 status handler
    public void setGate2StatusSwitch() {
        if (isTesting) {
            gate2Connection = gate2Switch.isSelected(); // Update gate status manually when testing is on
            truckPosition = 7; // Update position or any other logic as needed
        }
    }


    public void start() {
        executors.execute(() -> {
            while (true) {
                try {
                    if (isTesting) {
                        truckPositionField.setText("" + truckPosition);
                        sensor1Connection = sensor1Switch.isSelected();
                        sensor2Connection = sensor2Switch.isSelected();
                        sensor3Connection = sensor3Switch.isSelected();
                        gate1Connection = gate1Switch.isSelected();
                        gate2Connection = gate2Switch.isSelected();
                    }

                    Thread.sleep(1000);
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, Instances.truckNumber, "00035: (" + getClass().getName() + ") " + e.getMessage()));
                }
            }
        });
    }

    public void openGate1() {
        buttonController.openGate1();
        truckPosition = -1;
    }

    public void openGate2() {
        buttonController.openGate2();
        truckPosition = -1;
    }
    public void kppopenGate1() {
        buttonController.kppopenGate1();
        truckPosition = -1;
    }

    public void kppopenGate2() {
        buttonController.kppopenGate2();
        truckPosition = -1;
    }

    public void openExitGate1() {
        buttonController.openExitGate1();
        truckPosition = -1;
    }

    public void openExitGate2() {
        buttonController.openExitGate2();
        truckPosition = -1;
    }


    public void closeGate1() {
        buttonController.closeGate1();
        truckPosition = -1;
        currentTruck = new TruckResponse();
        truckScalingController.reinitialize();
        cargoConfirmationStatus = -1;
        isWaiting = false;
        truckNumber = "";
    }

    public void kppcloseGate1() {
        buttonController.kppcloseGate1();
        truckPosition = -1;
        currentTruck = new TruckResponse();
        truckScalingController.reinitialize();
        cargoConfirmationStatus = -1;
        isWaiting = false;
        truckNumber = "";
    }
    public void kppcloseGate2() {
        buttonController.kppcloseGate2();
        truckPosition = -1;
        currentTruck = new TruckResponse();
        truckScalingController.reinitialize();
        cargoConfirmationStatus = -1;
        isWaiting = false;
        truckNumber = "";
    }

    public void closeGate2() {
        buttonController.closeGate2();
        truckPosition = -1;
        currentTruck = new TruckResponse();
        truckScalingController.reinitialize();
        cargoConfirmationStatus = -1;
        isWaiting = false;
        truckNumber = "";
    }

    public void closeExitGate1() {
        buttonController.closeExitGate1();
        truckPosition = -1;
        currentTruck = new TruckResponse();
        truckScalingController.reinitialize();
        cargoConfirmationStatus = -1;
        isWaiting = false;
        truckNumber = "";
    }

    public void closeExitGate2() {
        buttonController.closeExitGate2();
        truckPosition = -1;
        currentTruck = new TruckResponse();
        truckScalingController.reinitialize();
        cargoConfirmationStatus = -1;
        isWaiting = false;
        truckNumber = "";
    }

    public void getWeight() {
        buttonController.getTruckWeigh();
    }


    @PostMapping("/kpp-gate1/open") // POST so'rovi uchun: http://localhost:port/kpp-gate1/open
    public ResponseEntity<String> apiKppOpenGate1() {
        try {
            // Log yozish mumkin: logService.save(...) yoki shunchaki System.out.println
            System.out.println("API orqali kppopenGate1 chaqirildi");
            kppopenGate1(); // Sizning asl metodingizni chaqirish
            return ResponseEntity.ok("KPP Darvoza 1 muvaffaqiyatli ochildi.");
        } catch (Exception e) {
            System.err.println("KPP Darvoza 1ni ochishda xatolik: " + e.getMessage());
            // Log yozish mumkin: logService.save(new LogEntity(5L, "", "API_ERROR: kppopenGate1 - " + e.getMessage()));
            return ResponseEntity.status(500).body("KPP Darvoza 1ni ochishda server xatoligi: " + e.getMessage());
        }
    }

    @PostMapping("/kpp-gate2/open") // POST so'rovi uchun: http://localhost:port/kpp-gate2/open
    public ResponseEntity<String> apiKppOpenGate2() {
        try {
            System.out.println("API orqali kppopenGate2 chaqirildi");
            kppopenGate2(); // Sizning asl metodingizni chaqirish
            return ResponseEntity.ok("KPP Darvoza 2 muvaffaqiyatli ochildi.");
        } catch (Exception e) {
            System.err.println("KPP Darvoza 2ni ochishda xatolik: " + e.getMessage());
            return ResponseEntity.status(500).body("KPP Darvoza 2ni ochishda server xatoligi: " + e.getMessage());
        }
    }

    @PostMapping("/kpp-gate1/close") // POST so'rovi uchun: http://localhost:port/kpp-gate1/close
    public ResponseEntity<String> apiKppCloseGate1() {
        try {
            System.out.println("API orqali kppcloseGate1 chaqirildi");
            kppcloseGate1(); // Sizning asl metodingizni chaqirish
            return ResponseEntity.ok("KPP Darvoza 1 muvaffaqiyatli yopildi.");
        } catch (Exception e) {
            System.err.println("KPP Darvoza 1ni yopishda xatolik: " + e.getMessage());
            return ResponseEntity.status(500).body("KPP Darvoza 1ni yopishda server xatoligi: " + e.getMessage());
        }
    }

    @PostMapping("/kpp-gate2/close") // POST so'rovi uchun: http://localhost:port/kpp-gate2/close
    public ResponseEntity<String> apiKppCloseGate2() {
        try {
            System.out.println("API orqali kppcloseGate2 chaqirildi");
            kppcloseGate2(); // Sizning asl metodingizni chaqirish
            return ResponseEntity.ok("KPP Darvoza 2 muvaffaqiyatli yopildi.");
        } catch (Exception e) {
            System.err.println("KPP Darvoza 2ni yopishda xatolik: " + e.getMessage());
            return ResponseEntity.status(500).body("KPP Darvoza 2ni yopishda server xatoligi: " + e.getMessage());
        }
    }
}
