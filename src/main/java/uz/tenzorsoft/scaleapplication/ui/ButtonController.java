package uz.tenzorsoft.scaleapplication.ui;

import com.fazecast.jSerialComm.SerialPort;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.CommandsEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.ShlagbaumEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.StatusEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.request.CheckDto;
import uz.tenzorsoft.scaleapplication.domain.request.ClientSideCheckDto;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.CheckCommandsDto;
import uz.tenzorsoft.scaleapplication.repository.CommandsRepository;
import uz.tenzorsoft.scaleapplication.repository.ShlagbaumRepository;
import uz.tenzorsoft.scaleapplication.service.*;
import uz.tenzorsoft.scaleapplication.service.raspberry.GpioControl;
import uz.tenzorsoft.scaleapplication.service.raspberry.RaspberryService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Instances.truckNumber;
import static uz.tenzorsoft.scaleapplication.domain.Settings.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.scaleExitPort;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.scalePort;
import static uz.tenzorsoft.scaleapplication.ui.MainController.*;

@Component
@RequiredArgsConstructor
public class ButtonController implements BaseController {

    private final CommandsRepository commandsRepository;
    private final ShlagbaunStatusService shlagbaunStatusService;
    private final ShlagbaumRepository shlagbaumRepository;
    private final ControllerService controllerService;
    private final TruckService truckService;
    private final CameraViewController cameraViewController;
    private final ScaleLogService scaleLogService;
    private final LogService logService;
    private final RestTemplate restTemplate;
    private static String tmpData = "";
    private static String tmpDataExit = "";

    @Autowired
    @Lazy
    private TableController tableController;
    @Autowired
    private RaspberryService raspberryService;

    @FXML
    private Button button1, button2, button4, button5;

    @FXML
    private Button buttonExit1, buttonExit2, buttonExit4, buttonExit5;

    private String commandComment = "";

    @Autowired
    private GpioControl gpioControl;

    @FXML
    public void initialize() {
        if (button1 != null) {
            setupButtonPressEffect(button1, "#4CAF50");
            setupButtonPressEffect(button2, "#4CAF50");
            setupButtonPressEffect(button4, "#D32F2F");
            setupButtonPressEffect(button5, "#D32F2F");
        }
        if (buttonExit1 != null) {
            setupButtonPressEffect(buttonExit1, "#4CAF50");
            setupButtonPressEffect(buttonExit2, "#4CAF50");
            setupButtonPressEffect(buttonExit4, "#D32F2F");
            setupButtonPressEffect(buttonExit5, "#D32F2F");
        }
    }

    private void setupButtonPressEffect(Button button, String pressedColor) {
        String originalColor = button.getStyle();

        button.setOnMousePressed(event -> button.setStyle("-fx-background-color: " + pressedColor + "; -fx-border-color: black; -fx-border-radius: 7; -fx-background-radius: 10; -fx-border-width: 0.8;"));

        button.setOnMouseReleased(event -> button.setStyle(originalColor));
    }

    public boolean openGate1() {
        try {
            if (!isTesting) {
                if (isRaspberryUsing) {
                    raspberryService.openGate1();
                } else controllerService.openGate1();
                gate1Connection = false;
                commandComment = "Finished";
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00017: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }

    public boolean kppopenGate1() {
        try {
            if (!isTesting) {
                if (isRaspberryUsing) {
                    raspberryService.kppopenGate1();
                } else controllerService.kppopenGate1();
                gate1Connection = false;
                commandComment = "Finished";
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00017: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }
    public boolean kppopenGate2() {
        try {
            if (!isTesting) {
                if (isRaspberryUsing) {
                    raspberryService.kppopenGate2();
                } else controllerService.kppopenGate2();
                kppgate2Connection = false;
                commandComment = "Finished";
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00017: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }
    public boolean openExitGate1() {
        try {
            if (!isTesting) {
                if (isRaspberryUsing) {
                    raspberryService.openExitGate1();
                } else controllerService.openExitGate1();
                gateExit1Connection = false;
                commandComment = "Finished";
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckExitNumber, "00017: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }

    public boolean openGate1(int truckPosition) {
        try {
            if (!isTesting) {
                if (isRaspberryUsing) {
                    boolean b = raspberryService.openGate1(truckPosition);
                    if (b) gate1Connection = false;
                    return b;
                }
                boolean b = controllerService.openGate1(truckPosition);
                if (b) gate1Connection = false;
                return b;
            } else {
                ScaleSystem.truckPosition = truckPosition;
                gate1Connection = true;
                return true;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00018: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }

    public boolean kppopenGate1(int truckPosition) {
        try {
            if (!isTesting) {
                if (isRaspberryUsing) {
                    boolean b = raspberryService.kppopenGate1(truckPosition);
                    if (b) kppgate1Connection = false;
                    return b;
                }
                boolean b = controllerService.kppopenGate1(truckPosition);
                if (b) kppgate1Connection = false;
                return b;
            } else {
                ScaleSystem.truckPosition = truckPosition;
                kppgate1Connection = true;
                return true;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00018: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }

    public boolean openGate1Manually() {
        try {
            if (!isConnected) {
                showAlert(Alert.AlertType.ERROR, "Error", "Controllerga ulanmagan");
                return false;
            }
            isWaiting = true;
            String truckNumber = showNumberInsertDialog();
            if (!truckNumber.isEmpty()) {
                // truckNumber == "RAQAMSIZ"
//                if (!truckService.isValidTruckNumber(truckNumber) && !truckService.isStandard(truckNumber)) {
//                    showAlert(Alert.AlertType.WARNING, "Not match", "Raqam mos kelmadi: " + truckNumber);
//                    return;
//                }
                if (!truckService.isEntranceAvailableForCamera1(truckNumber)) {
                    logService.save(new LogEntity(5L, truckNumber, "00019: (" + getClass().getName() + ") " + "Entrance not available"));
                    // showAlert(Alert.AlertType.WARNING, "Not available", truckNumber + " kirishi mumkin emas");
                    // commented because show alert will be displayed already
                    return false;
                }
                currentTruck.setTruckNumber(truckNumber);
                System.out.println("Truck Number: " + truckNumber);
                System.out.println("Kamera: " + CAMERA_1);
                firstGateEntranceTime = System.currentTimeMillis();
                openGate1(0);

                AttachResponse attachResponse = new AttachResponse();
                try {
                    attachResponse = cameraViewController.takePicture(CAMERA_1);


                    Long attachId = null;
                    if (attachResponse != null) {
                        attachId = attachResponse.getId();
                    }
                    currentTruck.getAttaches().add(new AttachIdWithStatus(attachId, AttachStatus.MANUAL_ENTRANCE_PHOTO));
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, truckNumber, "00019-1: (" + getClass().getName() + ") " + e.getMessage()));
                    e.printStackTrace();
                }
                currentTruck.setEnteredStatus(TruckAction.MANUAL_ENTRANCE);
                truckService.saveTruck(currentTruck, 1, attachResponse);
                tableController.addLastRecord();

                isWaiting = false;
                System.out.println("Saving truck number: " + truckNumber + " from Camera 1");
                return true;
            }
        } catch (Exception e) {
            isWaiting = false;
            logService.save(new LogEntity(5L, truckNumber, "00020: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
        }
        return false;
    }

    public boolean closeGate1() {
        try {
            firstGateEntranceTime = 0;
            if (!isTesting) {
                if (isRaspberryUsing) {
                    System.out.println("Closing Gate 1 ....");
                    raspberryService.closeGate1();
                } else controllerService.closeGate1();
                commandComment = "Finished";
                gate1Connection = true;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00024: (" + getClass().getName() + ") " + e.getMessage()));
            if (truckNumber.length() >= 3) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        return false;
    }

    public boolean kppcloseGate1() {
        try {
            kppfirstGateEntranceTime = 0;
            if (!isTesting) {
                if (isRaspberryUsing) {
                    System.out.println("Closing KPP Gate 1 ....");
                    raspberryService.kppcloseGate1();
                } else controllerService.kppcloseGate1();
                commandComment = "Finished";
                kppgate1Connection = true;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00024: (" + getClass().getName() + ") " + e.getMessage()));
            if (truckNumber.length() >= 3) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        return false;
    }
    public boolean kppcloseGate2() {
        try {
            kppsecondGateEntranceTime = 0;
            if (!isTesting) {
                if (isRaspberryUsing) {
                    System.out.println("Closing KPP Gate 2 ....");
                    raspberryService.kppcloseGate2();
                } else controllerService.kppcloseGate2();
                commandComment = "Finished";
                kppgate2Connection = true;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00024: (" + getClass().getName() + ") " + e.getMessage()));
            if (truckNumber.length() >= 3) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        return false;
    }
    public boolean closeExitGate1() {
        try {
            firstExitGateEntranceTime = 0;
            if (!isTesting) {
                if (isRaspberryUsing) {
                    System.out.println("Closing Gate 1 ....");
                    raspberryService.closeExitGate1();
                } else controllerService.closeExitGate1();
                commandComment = "Finished";
                gateExit1Connection = true;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckExitNumber, "00024: (" + getClass().getName() + ") " + e.getMessage()));
            if (truckExitNumber.length() >= 3) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        return false;
    }

//    public boolean closeGate1(String truckNumber) {
//        try {
//            if (!isTesting) {
//                if (isRaspberryUsing) {
//                    raspberryService.closeGate1();
//                } else controllerService.closeGate1();
//                commandComment = "Finished";
//                gate1Connection = true;
//            }
//        } catch (Exception e) {
//            commandComment = e.getMessage();
//            System.err.println(e.getMessage());
//            logService.save(new LogEntity(5L, truckNumber, "00024: (" + getClass().getName() + ") " + e.getMessage()));
//            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
//        }
//        return false;
//    }

    public boolean openGate2() {
        try {
            if (!isTesting) {
                if (isRaspberryUsing) {
                    boolean b = raspberryService.openGate2();
                    if (b) gate2Connection = false;
                    return b;
                }
                boolean b = controllerService.openGate2();
                if (b) gate2Connection = false;
                return b;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00025: (" + getClass().getName() + ") " + e.getMessage()));
        }
        return false;
    }

    public boolean openExitGate2() {
        try {
            if (!isTesting) {
                if (isRaspberryUsing) {
                    boolean b = raspberryService.openExitGate2();
                    if (b) gateExit2Connection = false;
                    return b;
                }
                boolean b = controllerService.openExitGate2();
                if (b) gateExit2Connection = false;
                return b;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckExitNumber, "00025: (" + getClass().getName() + ") " + e.getMessage()));
        }
        return false;
    }

    public boolean openGate2(int truckPosition) {
        try {
            if (!isTesting) {
                if (isRaspberryUsing) {
                    boolean b = raspberryService.openGate2(truckPosition);
                    if (b) gate2Connection = false;
                    return b;
                }
                boolean b = controllerService.openGate2(truckPosition);
                if (b) gate2Connection = false;
                return b;
            } else {
                ScaleSystem.truckPosition = truckPosition;
                gate2Connection = true;
                return true;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00026: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }

    public boolean openExitGate1(int truckPosition) {
        try {
            if (!isTesting) {
                if (isRaspberryUsing) {
                    boolean b = raspberryService.openExitGate1(truckPosition);
                    if (b) gateExit2Connection = false;
                    return b;
                }
                boolean b = controllerService.openExitGate1(truckPosition);
                if (b) gateExit2Connection = false;
                return b;
            } else {
                ScaleSystem.truckExitPosition = truckPosition;
                gateExit2Connection = true;
                return true;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckExitNumber, "00026: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }

    public boolean openGate2Manually() {
        try {
            if (!isConnected) {
                showAlert(Alert.AlertType.ERROR, "Xatolik", "Controllerga ulanmagan");
                return false;
            }
            isWaiting = true;
            String truckNumber = showNotFinishedTrucksDialog(truckService.getNotFinishedTrucks());
            if (!truckNumber.isEmpty()) {
                if (!truckService.isNotFinishedTrucksExists()) {
                    logService.save(new LogEntity(5L, truckNumber, "00021: (" + getClass().getName() + ") " + "All trucks are exited!"));
                    return false;
                }
                if (!truckService.isEntranceAvailableForCamera2(truckNumber)) {
                    logService.save(new LogEntity(5L, truckNumber, "00022: (" + getClass().getName() + ") " + "Entrance not available"));
                    return false;
                }
                currentTruck.setTruckNumber(truckNumber);
                System.out.println("Truck Number: " + truckNumber);
                System.out.println("Kamera: " + CAMERA_3);
                AttachResponse attachResponse = new AttachResponse();
                try {
                    attachResponse = cameraViewController.takePicture(CAMERA_3);
                    Long attachId = null;
                    if (attachResponse != null) {
                        attachId = attachResponse.getId();
                    }
                    currentTruck.getAttaches().add(new AttachIdWithStatus(attachId, AttachStatus.MANUAL_EXIT_PHOTO));
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, truckNumber, "00022-1: (" + getClass().getName() + ") " + e.getMessage()));
                    e.printStackTrace();
                }

                currentTruck.setExitedStatus(TruckAction.MANUAL_EXIT);
                truckService.saveTruck(currentTruck, 2, attachResponse);
                secondGateEntranceTime = System.currentTimeMillis();
                openGate2(7);
                isWaiting = false;
                return true;
            }
        } catch (Exception e) {
            logService.save(new LogEntity(5L, truckNumber, "00023: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
        }
        isWaiting = false;
        return false;
    }

    public boolean closeGate2() {
        try {
            secondGateEntranceTime = 0;
            if (!isTesting) {
                if (isRaspberryUsing) {
                    raspberryService.closeGate2();
                } else controllerService.closeGate2();
                commandComment = "Finished";
                gate2Connection = true;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00027: (" + getClass().getName() + ") " + e.getMessage()));
            if (truckNumber.length() >= 3) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        return false;
    }

    public boolean closeExitGate2() {
        try {
            secondExitGateEntranceTime = 0;
            if (!isTesting) {
                if (isRaspberryUsing) {
                    raspberryService.closeExitGate2();
                } else controllerService.closeExitGate2();
                commandComment = "Finished";
                gateExit2Connection = true;
            }
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckExitNumber, "00027: (" + getClass().getName() + ") " + e.getMessage()));
            if (truckExitNumber.length() >= 3) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        return false;
    }

    public void connect() {
        try {
            if (isRaspberryUsing) {
                gpioControl.initialize();
                gpioControl.getSensorStatuses();
                isConnected = isAvailableToConnect;
                closeGate1();
                closeGate2();
                kppcloseGate2();
                kppcloseGate1();

                closeExitGate1();
                closeExitGate2();

                return;
            }
            controllerService.connect();
        } catch (Exception e) {
            logService.save(new LogEntity(5L, truckNumber, "00028: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }

    }

    public void disconnect() {
        try {
            if (isRaspberryUsing) {
                isConnected = false;
                gpioControl.shutdown();
                return;
            }
            controllerService.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleServerCommands(CommandsEntity commands) {
        commandsRepository.save(commands);
        commandComment = ""; // Har bir buyruq oldidan kommentni tozalash
        boolean actionTaken = false;
        if (commands.getOpenGate1()) {
            kppopenGate1();
            actionTaken = true;
        } else if (commands.getOpenGate2()) {
            kppopenGate2();
            actionTaken = true;
        } else if (commands.getCloseGate1()) {
            kppcloseGate1();
            actionTaken = true;
        } else if (commands.getCloseGate2()) {
            kppcloseGate2();
            actionTaken = true;
        }

        if (actionTaken) {
            sendCommandStatus(commands.getServerId(), commandComment.isEmpty() ? "Finished" : commandComment);
        } else {
            // Hech qanday amal bajarilmagan bo'lsa ham status yuborish mumkin
            // sendCommandStatus(commands.getServerId(), "No action taken for command");
        }
        commandComment = ""; // Kommentni tozalash
    }

//    public void handleShlagbaunAction(Integer shlagbaunNumber, Boolean openStatus, Long commandId) {
//        commandComment = ""; // Har bir buyruq oldidan kommentni tozalash
//        boolean actionTaken = false;
//        boolean currentActionSuccess = false;
//
//
//
//        System.out.println("ShlagbaunAction: Raqam=" + shlagbaunNumber + ", Status=" + openStatus + ", BuyruqID=" + commandId);
//        logService.save(new LogEntity(5L, Instances.truckNumber, "ButtonController: ShlagbaunAction boshlandi: Raqam=" + shlagbaunNumber + ", Status=" + openStatus + ", BuyruqID=" + commandId));
//
//        if (shlagbaunNumber == 1) {
//            if (openStatus) {
//                System.out.println("KPP1 shlagbaumni ochish...");
//                kppopenGate1();
//
//            } else {
//                System.out.println("KPP1 shlagbaumni yopish...");
//                kppcloseGate1();
//            }
//            actionTaken = true;
//        } else if (shlagbaunNumber == 2) {
//            if (openStatus) {
//                System.out.println("KPP2 shlagbaumni ochish...");
//                kppopenGate2();
//            } else {
//                System.out.println("KPP2 shlagbaumni yopish...");
//                kppcloseGate2();
//            }
//            actionTaken = true;
//        } else {
//            commandComment = "Noma'lum shlagbaum raqami: " + shlagbaunNumber;
//            System.err.println(commandComment);
//            logService.save(new LogEntity(5L, Instances.truckNumber, commandComment));
//        }
//
//        if (commandId != null && commandId > 0) { // Faqat yaroqli commandId lar uchun
//            ShlagbaumEntity entity= this.shlagbaumRepository.findByCommandId(commandId);
//            entity.setStatus(true);
//            entity.setLocalid(entity.getId());
//            this.shlagbaumRepository.save(entity);
//            shlagbaunStatusService.recordStatusCommand(commandId, currentActionSuccess && actionTaken);
//        } else {
//            logService.save(new LogEntity(5L, Instances.truckNumber, "ButtonController: Yaroqsiz commandId (" + commandId + ") uchun status saqlanmadi."));
//        }
//
//        if (actionTaken) {
//            // commandComment ni kppopenGate1() kabi metodlar o'rnatgan bo'lishi kerak
//            // Agar currentActionSuccess true bo'lsa va commandComment bo'sh bo'lsa, "Finished" deb o'rnatamiz
//            if (currentActionSuccess && commandComment.isEmpty()) {
//                commandComment = "Finished";
//            } else if (!currentActionSuccess && commandComment.isEmpty()) {
//                commandComment = "Failed (unknown reason)"; // Agar xatolik bo'lsa-yu, komment bo'sh qolsa
//            }
//            sendCommandStatus(commandId, commandComment); // commandComment o'zgaruvchisi metod ichida to'ldiriladi
//        } else if (!commandComment.isEmpty()) {
//            sendCommandStatus(commandId, commandComment);
//        }
//        commandComment = ""; // Kommentni keyingi chaqiruv uchun tozalash
//    }
public void handleShlagbaunAction(Integer shlagbaunNumber, Boolean openStatus, Long commandId) {
    this.commandComment = ""; // Har bir chaqiruv oldidan tozalash
    boolean actionTaken = false;
    boolean currentActionSuccess = false;

    System.out.println("[" + LocalDateTime.now() + "] ShlagbaunAction: Raqam=" + shlagbaunNumber +
            ", StatusBuyruq=" + openStatus + ", HaqiqiyCommandID=" + commandId);
    if (logService != null) {
        logService.save(new LogEntity(5L, Instances.truckNumber, "ButtonController: ShlagbaunAction boshlandi: Raqam=" + shlagbaunNumber +
                ", StatusBuyruq=" + openStatus + ", HaqiqiyCommandID=" + commandId));
    }

    if (shlagbaunNumber == 1) {
        if (openStatus) {
            System.out.println("[" + LocalDateTime.now() + "] KPP1 shlagbaumni ochishga urinish...");
            currentActionSuccess = kppopenGate1();
        } else {
            System.out.println("[" + LocalDateTime.now() + "] KPP1 shlagbaumni yopishga urinish...");
            currentActionSuccess = kppcloseGate1();
        }
        actionTaken = true;
    } else if (shlagbaunNumber == 2) {
        if (openStatus) {
            System.out.println("[" + LocalDateTime.now() + "] KPP2 shlagbaumni ochishga urinish...");
            currentActionSuccess = kppopenGate2();
        } else {
            System.out.println("[" + LocalDateTime.now() + "] KPP2 shlagbaumni yopishga urinish...");
            currentActionSuccess = kppcloseGate2();
        }
        actionTaken = true;
    } else {
        this.commandComment = "Noma'lum shlagbaum raqami: " + shlagbaunNumber;
        System.err.println("[" + LocalDateTime.now() + "] " + this.commandComment);
        if (logService != null) logService.save(new LogEntity(5L, Instances.truckNumber, this.commandComment));
        currentActionSuccess = false; // Noma'lum raqam uchun amal bajarilmadi
        actionTaken = false; // Muhim: actionTaken false bo'lishi kerak
    }

    if (commandId != null && commandId > 0) {
        Optional<ShlagbaumEntity> entityOptional = this.shlagbaumRepository.findByCommandId(commandId);
        if (entityOptional.isPresent()) {
            ShlagbaumEntity entity = entityOptional.get();
            System.out.println("[" + LocalDateTime.now() + "] ShlagbaumEntity topildi (HaqiqiyCommandID: " + commandId + ", DB ID=" + entity.getId() + ")");
            entity.setStatus(currentActionSuccess); // Haqiqiy amal natijasini yozamiz
            entity.setLocalid(entity.getId());
            try {
                this.shlagbaumRepository.save(entity);
                System.out.println("[" + LocalDateTime.now() + "] ShlagbaumEntity (DB ID: " + entity.getId() + ") statusi yangilandi: " + currentActionSuccess);
            } catch (Exception e) {
                System.err.println("[" + LocalDateTime.now() + "] ShlagbaumEntity (DB ID: " + entity.getId() + ") statusini yangilashda xatolik: " + e.getMessage());
                if(logService != null) logService.save(new LogEntity(5L, Instances.truckNumber, "Entity.status yangilashda xato (CommandID: " + commandId + "): " + e.getMessage()));
                // Agar saqlashda xato bo'lsa, currentActionSuccess ni false qilish kerakmi?
                // Yoki bu shunchaki log uchunmi? Agar serverga xabar yuborishda bu status ishlatilsa, false qilish kerak.
                currentActionSuccess = false; // Saqlashda xato bo'lsa, umumiy muvaffaqiyatni false deb hisoblaymiz
            }
        } else {
            // Agar actionTaken true bo'lsa ham (shlagbaum ochildi/yopildi), lekin DB da yozuv topilmasa,
            // bu holatni logga yozamiz va commandComment ga qo'shamiz.
            // currentActionSuccess bu yerda shlagbaum amaliyoti haqida, DB yozuvi haqida emas.
            String notFoundComment = "DB da ShlagbaumEntity topilmadi (HaqiqiyCommandID: " + commandId + "). Fizik amal statusi: " + currentActionSuccess;
            System.err.println("[" + LocalDateTime.now() + "] " + notFoundComment);
            if (logService != null) logService.save(new LogEntity(5L, Instances.truckNumber, notFoundComment));
            if (this.commandComment.isEmpty()) {
                this.commandComment = notFoundComment;
            } else {
                this.commandComment += "; " + notFoundComment;
            }
            // currentActionSuccess o'zgarishsiz qoladi, chunki u fizik amal natijasi.
            // sendCommandStatus da bu komment yuboriladi.
        }
    } else {
        this.commandComment = (this.commandComment.isEmpty() ? "" : this.commandComment + "; ") + "Yaroqsiz commandId (" + commandId + ") uchun DB da status saqlanmadi.";
        System.err.println("[" + LocalDateTime.now() + "] " + this.commandComment);
        if (logService != null) logService.save(new LogEntity(5L, Instances.truckNumber, "Yaroqsiz commandId (" + commandId + ")"));
        // Agar commandId yaroqsiz bo'lsa, currentActionSuccess ni false deb hisoblashimiz mumkin,
        // chunki buyruqni to'liq qayta ishlab bo'lmadi.
        currentActionSuccess = false;
    }

    // Kommentni yakuniy shakllantirish
    if (actionTaken) { // Agar fizik amal bajarilgan bo'lsa
        if (currentActionSuccess && this.commandComment.isEmpty()) {
            this.commandComment = "Finished";
        } else if (!currentActionSuccess && this.commandComment.isEmpty()) {
            this.commandComment = "Failed (unknown physical action reason)";
        }
    } else { // Agar fizik amal bajarilmagan bo'lsa (masalan, noma'lum shlagbaum raqami)
        if (this.commandComment.isEmpty()) { // Agar komment hali ham bo'sh bo'lsa
            this.commandComment = "Action not taken (e.g., unknown шлагбаум number)";
        }
        // currentActionSuccess allaqachon false bo'lishi kerak bu holatda
    }

    // Statusni serverga yuborish
    // commandId yaroqli bo'lsa ham, yaroqsiz bo'lsa ham (serverga xatolikni bildirish uchun) status yuborilishi mumkin.
    // Lekin yaroqsiz commandId (-1 yoki 0) bilan serverga yuborishdan ma'no yo'q.
    if (commandId != null && commandId > 0) {
        sendCommandStatus(commandId, this.commandComment); // currentActionSuccess ni ham yuboramiz
    } else if (!this.commandComment.isEmpty()) {
        // Agar commandId yaroqsiz, lekin komment bor bo'lsa (masalan, "Yaroqsiz commandId")
        // Bu vaziyatda serverga xabar yuborish kerakmi? Agar ha bo'lsa, qanday commandId bilan?
        // Hozircha faqat logga yozamiz.
//        System.err.println("[" + LocalDateTime.now() + "] sendCommandStatus chaqirilmadi yaroqsiz commandId ("+commandId+") tufayli. Komment: " + this.commandComment);
    }
    // this.commandComment = ""; // Metod oxirida tozalash yaxshi, lekin sendCommandStatus uni ishlatib bo'lgach
}
    //    private void sendCommandStatus(long commandId, String commandComment) {
//        boolean isFinished = !commandComment.isEmpty();
//        CheckCommandsDto request = new CheckCommandsDto(commandId, commandComment, isFinished);
//        restTemplate.postForEntity(
//                Instances.SERVER_URL + "/api/v1/shlagbaun/check",
//                request, Void.class
//        );
//    }
//    private void sendCommandStatus(long commandId, String comment) {
//        boolean success = !commandComment.isEmpty();
//        long id = commandId;
//
//        CheckDto requestPayload = new CheckDto(id,  success);
//        HttpEntity<CheckDto> entity = new HttpEntity<>(requestPayload); // Sarlavhalar bilan entity yaratish
//
//        try {
//            System.out.println("Serverga buyruq statusi yuborilmoqda (avtorizatsiya bilan): " + requestPayload);
//            restTemplate.postForEntity(
//                    Instances.SERVER_URL+"/shlagbaun/check",
//                    entity, // HttpEntity ni yuborish
//                    Void.class
//            );
//            System.out.println("Buyruq statusi muvaffaqiyatli yuborildi.");
//        } catch (HttpClientErrorException e) { // Xususan 401 va 403 xatoliklarini ushlash
//            System.err.println("Buyruq statusini yuborishda HTTP xatoligi: " + e.getStatusCode() + " : " + e.getResponseBodyAsString());
//            logService.save(new LogEntity(5L, Instances.truckNumber, "sendCommandStatus HTTP xatoligi (" + commandId + "): " + e.getStatusCode() + " - " + e.getResponseBodyAsString()));
//            // Agar 401 bo'lsa, foydalanuvchini qayta login qilishga yo'naltirish kerak bo'lishi mumkin
//        } catch (Exception e) {
//            System.err.println("Buyruq statusini yuborishda umumiy xatolik: " + e.getMessage());
//            logService.save(new LogEntity(5L, Instances.truckNumber, "sendCommandStatus umumiy xatoligi (" + commandId + "): " + e.getMessage()));
//        }
//    }
    public void sendCommandStatus(long commandId, String commandComment) {
        System.out.println("[" + LocalDateTime.now() + "] sendCommandStatus chaqirildi: HaqiqiyCommandID=" + commandId + ", Komment='" + commandComment + "'");

        // Repository metodining Optional<ShlagbaumEntity> qaytarishini taxmin qilamiz
        Optional<ShlagbaumEntity> entityOptional = shlagbaumRepository.findByCommandId(commandId);

        ClientSideCheckDto checkData = new ClientSideCheckDto();
        checkData.setComment(commandComment); // Kommentni har doim o'rnatamiz

        if (!entityOptional.isPresent()) {
            System.err.println("[" + LocalDateTime.now() + "] sendCommandStatus: ShlagbaumEntity topilmadi (HaqiqiyCommandID: " + commandId + "). Serverga 'not found' statusi yuborilmoqda.");
            if (logService != null) {
                logService.save(new LogEntity(5L, Instances.truckNumber, "sendCommandStatus: DB da Entity topilmadi, HaqiqiyCommandID=" + commandId + ". Komment: " + commandComment));
            }

            // Entity topilmagan holat uchun ClientSideCheckDto ni sozlash
            checkData.setSuccess(false); // Muvaffaqiyatsizlik deb belgilaymiz
            // checkData.setId(null); // Server bu maydonni qanday kutishiga bog'liq, entity yo'q
            // checkData.setNumber(null); // Entity yo'q bo'lgani uchun raqami ham noma'lum
            // checkData.setLocalId(commandId); // Serverga qaysi commandId topilmaganini bildirish uchun jo'natish mumkin
        } else {
            ShlagbaumEntity entity = entityOptional.get(); // Entity mavjud, uni olamiz
            System.out.println("[" + LocalDateTime.now() + "] sendCommandStatus: ShlagbaumEntity topildi (HaqiqiyCommandID: " + commandId + ", DB ID: " + entity.getId() + ")");

            checkData.setId(entity.getId());       // Clientdagi DB ID si
            checkData.setNumber(entity.getNumber()); // Agar kerak bo'lsa
            checkData.setLocalId(entity.getLocalid()); // Agar kerak bo'lsa
            checkData.setSuccess(entity.getStatus());
//            System.out.println("[" + LocalDateTime.now() + "] sendCommandStatus: Entity statusi ("+entity.getStatus()+") 'success' uchun o'rnatildi.");
        }

        // Sizning asl kodingizdagi successStatus logikasi (agar hali ham kerak bo'lsa):
        // boolean successBasedOnComment = (commandComment != null && !commandComment.isEmpty());
        // Agar bu logikani `checkData.setSuccess()` uchun ishlatmoqchi bo'lsangiz, yuqoridagi
        // `checkData.setSuccess(entity.getStatus());` ni o'zgartiring yoki bu logikani qo'shing/birlashtiring.
        // Misol uchun:
        // if (entityOptional.isPresent()) {
        //     checkData.setSuccess(entityOptional.get().getStatus()); // Avval entity statusini olamiz
        //     if (!successBasedOnComment && entityOptional.get().getStatus()) {
        //         // Agar kommentga ko'ra "false", lekin entity statusi "true" bo'lsa, qaysi biri ustun?
        //         // Bu logikani aniqlashtirish kerak.
        //     }
        // } else {
        //     checkData.setSuccess(false); // Topilmasa aniq false
        // }


        List<ClientSideCheckDto> requestPayloadList = new ArrayList<>();
        requestPayloadList.add(checkData);

        HttpEntity<List<ClientSideCheckDto>> httpEntity = new HttpEntity<>(requestPayloadList); // O'zgaruvchi nomini entity1 dan httpEntity ga o'zgartirdim
        String url = Instances.SERVER_URL + "/shlagbaun/check";

        try {
            System.out.println("[" + LocalDateTime.now() + "] Serverga (" + url + ") buyruq statusi yuborilmoqda: " + requestPayloadList);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    url,
                    httpEntity,
                    String.class
            );

            System.out.println("[" + LocalDateTime.now() + "] Serverdan javob muvaffaqiyatli qabul qilindi (HaqiqiyCommandID: " + commandId + ").");
            System.out.println("Status kodi: " + responseEntity.getStatusCode());
            System.out.println("Server javobi (body): " + responseEntity.getBody());
            System.out.println("Buyruq statusi ro'yxati serverga muvaffaqiyatli yuborildi va javob olindi.");

        } catch (HttpClientErrorException e) {
            System.err.println("[" + LocalDateTime.now() + "] Buyruq statusini yuborishda HTTP xatoligi (HaqiqiyCommandID: " + commandId + "): " + e.getStatusCode() + " : " + e.getResponseBodyAsString());
            if (logService != null) {
                logService.save(new LogEntity(5L, Instances.truckNumber, "sendCommandStatus HTTP xatoligi (" + commandId + "): " + e.getStatusCode() + " - " + e.getResponseBodyAsString()));
            }
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] Buyruq statusini yuborishda umumiy xatolik (HaqiqiyCommandID: " + commandId + "): " + e.getMessage());
            // e.printStackTrace(); // Debug uchun kerak bo'lsa aktivlashtiring
            if (logService != null) {
                logService.save(new LogEntity(5L, Instances.truckNumber, "sendCommandStatus umumiy xatoligi (" + commandId + "): " + e.getMessage()));
            }
        }
    }
//    public void sendCommandStatus(long commandId, String commandComment) {
//        // `success` holatini `commandComment` bo'sh emasligiga qarab aniqlaymiz
//        boolean successStatus = (commandComment != null && !commandComment.isEmpty());
//
//        // API kutayotgan DTO (ClientSideCheckDto) obyektini yaratamiz
//        ClientSideCheckDto checkData = new ClientSideCheckDto();
//        checkData.setId(commandId);
//        checkData.setSuccess(successStatus);
//        checkData.setComment(commandComment); // comment ni ham o'rnatamiz
//        // Agar `number` va `localId` maydonlari kerak bo'lsa va ularning qiymatlari mavjud bo'lsa, ularni ham o'rnating:
//        // checkData.setNumber(...);
//        // checkData.setLocalId(...);
//
//        // API ro'yxat kutganligi uchun, yaratilgan DTO ni ro'yxatga qo'shamiz
//        List<ClientSideCheckDto> requestPayloadList = new ArrayList<>();
//        requestPayloadList.add(checkData);
//
//        // HttpEntity ni ro'yxat bilan yaratamiz
//        HttpEntity<List<ClientSideCheckDto>> entity = new HttpEntity<>(requestPayloadList);
//
//        try {
//            System.out.println("Serverga buyruq statusi ro'yxati yuborilmoqda: " + requestPayloadList);
//            restTemplate.postForEntity(
//                    Instances.SERVER_URL + "/shlagbaun/check", // URL to'g'riligini tekshiring
//                    entity,
//                    Void.class
//            );
//            System.out.println("Buyruq statusi ro'yxati muvaffaqiyatli yuborildi.");
//        } catch (HttpClientErrorException e) {
//            System.err.println("Buyruq statusini yuborishda HTTP xatoligi: " + e.getStatusCode() + " : " + e.getResponseBodyAsString());
//            if (logService != null) { // logService mavjudligini tekshirish
//                logService.save(new LogEntity(5L, Instances.truckNumber, "sendCommandStatus HTTP xatoligi (" + commandId + "): " + e.getStatusCode() + " - " + e.getResponseBodyAsString()));
//            }
//        } catch (Exception e) {
//            System.err.println("Buyruq statusini yuborishda umumiy xatolik: " + e.getMessage());
//            if (logService != null) { // logService mavjudligini tekshirish
//                logService.save(new LogEntity(5L, Instances.truckNumber, "sendCommandStatus umumiy xatoligi (" + commandId + "): " + e.getMessage()));
//            }
//        }
//    }
    //    public double getTruckWeigh() {
//        try {
//            if (isTesting) {
//                return (int) ((Math.random() * 10) + 100);
//            }
//
//            if (scalePort.isOpen()) {
//                scalePort.closePort();
//            }
//            scalePort.openPort();
//            scalePort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
//            scalePort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
//
//            byte[] readBuffer = new byte[1024];
//            int bytesRead = scalePort.readBytes(readBuffer, readBuffer.length);
//
//            if (bytesRead == -1) {
//                commandComment = "Error: No data received (bytesRead == -1). Check device connection or configuration.";
//                System.err.println(commandComment);
//                return 0.0;
//            }
//
//            System.out.println("ScalePort read bytes: " + bytesRead);
//            if (bytesRead > 0) {
//                String data = new String(readBuffer, 0, bytesRead).trim();
//                System.out.println("Scale data: " + data);
//
//                double numericValue = parseWeightData(data);
//                if (numericValue != 0.0) {
//                    scaleLogService.save(data, String.valueOf(numericValue));
//                }
//                System.out.println("Kg: " + numericValue);
//                commandComment = "Finished";
//                return numericValue;
//            } else {
//                commandComment = "No data received (bytesRead == 0).";
//            }
//        } catch (Exception e) {
//            commandComment = "Unexpected error: " + e.getMessage();
//            System.err.println(e.getMessage());
//        } finally {
//            if (scalePort.isOpen()) {
//                scalePort.closePort();
//            }
//        }
//        return 0.0;
//    }

    public double getTruckWeigh() {
        try {
            if (isTesting) return (int) ((Math.random() * 10) + 100);

//            return isRaspberryUsing ? getTruckWeightRaspberry() : getTruckWeightWindows();
            return 5000.0;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public double getTruckExitWeigh() {
        try {
            if (isTesting) return (int) ((Math.random() * 10) + 100);

//            return isRaspberryUsing ? getTruckWeightRaspberryExit() : getTruckWeightWindowsExit();
            return 10000.0;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }


    private double getTruckWeightWindows() {
        try {
            if (isTesting) return (int) ((Math.random() * 10) + 100);
            if (scalePort == null) {
                scalePort = SerialPort.getCommPort("COM3");
                scalePort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                scalePort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
                scalePort.openPort();
            }

            byte[] readBuffer = new byte[1024];
            int bytesRead = scalePort.readBytes(readBuffer, readBuffer.length);
            if (bytesRead > 0) {
                String data = new String(readBuffer, 0, bytesRead, StandardCharsets.UTF_8).trim();
                if (!tmpData.equals(data)) {
                    System.out.println(LocalDateTime.now() + " Scale Data: " + data);
                    tmpData = data;
                }
                return parseWeightData(data);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0.0;
    }

    private double getTruckWeightWindowsExit() {
        try {
            if (isTesting) return (int) ((Math.random() * 10) + 100);
            if (scaleExitPort == null) {
                scaleExitPort = SerialPort.getCommPort("COM3");
                scaleExitPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                scaleExitPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
                scaleExitPort.openPort();
            }

            byte[] readBuffer = new byte[1024];
            int bytesRead = scaleExitPort.readBytes(readBuffer, readBuffer.length);
            if (bytesRead > 0) {
                String data = new String(readBuffer, 0, bytesRead, StandardCharsets.UTF_8).trim();
                if (!tmpDataExit.equals(data)) {
                    System.out.println(LocalDateTime.now() + " Scale Data: " + data);
                    tmpDataExit = data;
                }
                return parseWeightData(data);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0.0;
    }

    private double getTruckWeightRaspberry() {
        String portName = "/dev/ttyUSB0";
        if (scalePort == null) {
            scalePort = SerialPort.getCommPort(portName);
            scalePort.setBaudRate(9600);
            scalePort.setNumDataBits(8);
            scalePort.setParity(SerialPort.NO_PARITY);
            scalePort.setNumStopBits(SerialPort.ONE_STOP_BIT);

            if (!scalePort.openPort()) {
                System.out.println("Failed to open port : " + portName);
                return 0.0;
            }
            System.out.println("Port opened: " + portName);
        }

        byte[] readBuffer = new byte[1024];
        int bytesRead = scalePort.readBytes(readBuffer, readBuffer.length);
        if (bytesRead > 0) {
            String data = new String(readBuffer, 0, bytesRead, StandardCharsets.UTF_8).trim();
            if (!tmpData.equals(data)) {
                System.out.println(LocalDateTime.now() + " Scale Data: " + data);
                tmpData = data;
            }
            return parseWeightData(data);
        }
        return 0.0;
    }

    private double getTruckWeightRaspberryExit() {
        String portName = "/dev/ttyUSB0";
        if (scaleExitPort == null) {
            scaleExitPort = SerialPort.getCommPort(portName);
            scaleExitPort.setBaudRate(9600);
            scaleExitPort.setNumDataBits(8);
            scaleExitPort.setParity(SerialPort.NO_PARITY);
            scaleExitPort.setNumStopBits(SerialPort.ONE_STOP_BIT);

            if (!scaleExitPort.openPort()) {
                System.out.println("Failed to open port : " + portName);
                return 0.0;
            }
            System.out.println("Port opened: " + portName);
        }

        byte[] readBuffer = new byte[1024];
        int bytesRead = scaleExitPort.readBytes(readBuffer, readBuffer.length);
        if (bytesRead > 0) {
            String data = new String(readBuffer, 0, bytesRead, StandardCharsets.UTF_8).trim();
            if (!tmpDataExit.equals(data)) {
                System.out.println(LocalDateTime.now() + " Scale Data: " + data);
                tmpDataExit = data;
            }
            return parseWeightData(data);
        }
        return 0.0;
    }

    private double parseWeightData(String data) {
        try {
            Pattern pattern = Pattern.compile("\\+\\d{7}\\w");
            Matcher matcher = pattern.matcher(data);

            if (matcher.find()) {
                String entry = matcher.group();
                String numericPart = entry.substring(1, entry.length() - 2);
                return Double.parseDouble(numericPart);
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("Unable to parse weight!!! \n" + e.getMessage());
        }
        return 0.0;
    }

    public void closePort() {
        if (scalePort.isOpen() && scalePort != null) {
            scalePort.closePort();
            System.out.println("Port closed");
        }
    }

    public void closePort2() {
        if (scaleExitPort.isOpen() && scaleExitPort != null) {
            scaleExitPort.closePort();
            System.out.println("Port closed");
        }
    }


}
