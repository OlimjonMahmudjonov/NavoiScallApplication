package uz.tenzorsoft.scaleapplication.ui;

import com.fazecast.jSerialComm.SerialPort;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.CommandsEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.CheckCommandsDto;
import uz.tenzorsoft.scaleapplication.service.*;
import uz.tenzorsoft.scaleapplication.service.raspberry.GpioControl;
import uz.tenzorsoft.scaleapplication.service.raspberry.RaspberryService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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

    private String commandComment = "";

    @Autowired
    private GpioControl gpioControl;

    @FXML
    public void initialize() {
        setupButtonPressEffect(button1, "#4CAF50");
        setupButtonPressEffect(button2, "#4CAF50");
        setupButtonPressEffect(button4, "#D32F2F");
        setupButtonPressEffect(button5, "#D32F2F");
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
                System.out.println("Truck Number: "  + truckNumber);
                System.out.println("Kamera: "  + CAMERA_1);
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
                    boolean b = raspberryService.openExitGate2(truckPosition);
                    if (b) gateExit2Connection = false;
                    return b;
                }
                boolean b = controllerService.openExitGate2(truckPosition);
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
        if (commands.getOpenGate1()) {
            openGate1();
            sendCommandStatus(commands.getServerId(), commandComment);
        } else if (commands.getOpenGate2()) {
            openGate2();
            sendCommandStatus(commands.getServerId(), commandComment);
        } else if (commands.getCloseGate1()) {
            closeGate1();
            sendCommandStatus(commands.getServerId(), commandComment);
        } else if (commands.getCloseGate2()) {
            closeGate2();
            sendCommandStatus(commands.getServerId(), commandComment);
        } else if (commands.getWeighing()) {
            getTruckWeigh();
            sendCommandStatus(commands.getServerId(), commandComment);
        }
        commandComment = "";
    }

    private void sendCommandStatus(long commandId, String commandComment) {
        boolean isFinished = !commandComment.isEmpty();
        CheckCommandsDto request = new CheckCommandsDto(commandId, commandComment, isFinished);
        restTemplate.postForEntity(
                Instances.SERVER_URL + "/commands/checkCommands",
                request, Void.class
        );
    }

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

            return isRaspberryUsing ? getTruckWeightRaspberry() : getTruckWeightWindows();

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    public double getTruckExitWeigh() {
        try {
            if (isTesting) return (int) ((Math.random() * 10) + 100);

            return isRaspberryUsing ? getTruckWeightRaspberryExit() : getTruckWeightWindowsExit();

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
