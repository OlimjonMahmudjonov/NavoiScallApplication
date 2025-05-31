package uz.tenzorsoft.scaleapplication.ui.components;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.ActionStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.service.CargoService;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.PrintCheck;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.ui.*;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.truckExitPosition;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.truckPosition;
import static uz.tenzorsoft.scaleapplication.ui.MainController.showCargoScaleConfirmationDialog;

@Component
@RequiredArgsConstructor
@Slf4j
@Setter
@Getter
public class TruckScalingController {
    private final ButtonController buttonController;
    private final CameraViewController cameraViewController;
    private final PrintCheck printCheck;
    private final TableController tableController;
    private final TruckService truckService;
    private final CargoService cargoService;
    private final ExecutorService executors;
    private final LogService logService;
    private final ImageController imageController;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ControlPane controlPane;
    private final MainController mainController;

    private boolean isTruckEntered = false, isTruckExited = false,
            isOnScale = false, isScaled = false, isCargoPhotoTaken = false,
            isCargoConfirmationDialogOpened = false, isTimeoutChanged = false, checkPrinted = false;

    private boolean isTruckEntered2 = false, isTruckExited2 = false,
            isOnScale2 = false, isScaled2 = false, isCargoPhotoTaken2 = false,
            isCargoConfirmationDialogOpened2 = false, isTimeoutChanged2 = false, checkPrinted2 = false;
    @Setter
    @Getter
    private double weigh = 0.0;
    private double weigh2 = 0.0;
    private Integer truckPos = 10;
    private Integer truckExitPos = 10;
    private long gate1OpenedMillis = 0;

    public void start() {
//        scheduler.scheduleAtFixedRate(() -> {
        executors.execute(() -> {
            while (true) {
//                System.out.println("////////-----enter-----////////");
                startScaleEntrance();
//                System.out.println("////////-----exit-----////////");
                startScaleExit();
            }
        });
    }

    private void startScaleEntrance() {
        try {
            if (truckPos != truckPosition) {
                System.out.println(LocalDateTime.now() + " truckPosition = " + truckPosition);
                truckPos = truckPosition;
            }

//            System.out.println("Gate 1 Connection old: " + gate1Connection);
//            System.out.println("TruckPosition: " + truckPosition);
//            System.out.println("Sensor 1 Connnection: " + sensor1Connection);

            if (!sensor1Connection && truckPosition == 0) {
                imageController.setIncomePhoto(new Image("/images/in/1.png"));
                truckPosition = 1;
                isTruckEntered = true;
            }

            if (!gate1Connection && truckPosition == 0) {
                System.out.println("Entered gate 1 Connection: " + gate1Connection);
                System.out.println("FirstGateEntranceTime " + firstGateEntranceTime);
                if (System.currentTimeMillis() - firstGateEntranceTime >= 60000) {
                    System.out.println("Shlagbaum yopiladi");
                    buttonController.closeGate1();
                    System.out.println("Truck Id: " + currentTruck == null ? null : currentTruck.getId());
                    imageController.setIncomePhoto(new Image("/images/in/-1.png"));
                    truckPosition = -1;
                    if (currentTruck.getId() != null) {
                        truckService.deleteTruckById(currentTruck.getId());
                    }
                }
            }

            if (truckPosition == 1 && sensor1Connection && (!sensor2Connection || isOnScale)) {
                // Save status as PROCESSING
                imageController.setIncomePhoto(new Image("/images/in/1.1.png"));
                truckService.saveTruckStatus(currentTruck.getEnteredStatus(), ActionStatus.PROCESSING);
                truckPosition = 2;
                buttonController.closeGate1();
            }

            if ((!sensor2Connection || isOnScale) && (truckPosition == 2) &&
                    (currentTruck.getEnteredStatus() == TruckAction.ENTRANCE ||
                            currentTruck.getEnteredStatus() == TruckAction.MANUAL_ENTRANCE)) {
                imageController.setIncomePhoto(new Image("/images/in/2.png"));
                System.out.println("Scaling ---- isScaled = " + isScaled + " Status " + currentTruck.getEnteredStatus());
                if (controlPane.getButton3().isDisable()) {
                    controlPane.getButton3().setDisable(false);
                }
                System.out.println("SCALE_TIMEOUT = " + SCALE_TIMEOUT);
                if (!isScaled && weigh <= 0) {
                    Timer timer1 = new Timer();
                    timer1.schedule((new TimerTask() {
                        @Override
                        public void run() {
                            if (weigh > 0) {
                                timer1.cancel();
                            } else {
                                setRescaleAttributes();
                            }
                        }
                    }), 60000);

                }
                Timer timer = new Timer();
                timer.schedule((new TimerTask() {
                    @Override
                    public void run() {
                        double holder = buttonController.getTruckWeigh();
                        double helper = holder != 0 ? holder : weigh;
                        if (helper > 0.0) System.out.println("helper weigh = " + helper);

                        if (weigh != helper && helper != 0.0 /*&& !isTesting*/) {
                            weigh = helper;
                        } else if (weigh == helper && helper != 0) {
                            log.info("Truck weigh: {}", weigh);
                            isScaled = true;
                        }

                        if (isScaled && !isCargoPhotoTaken && weigh > 0) { // weigh > 0
                            try {
                                AttachResponse response = cameraViewController.takePicture(CAMERA_2);
                                System.out.println("AttachResponse: " + response);
                                if (response != null) {
                                    truckService.saveTruckAttaches(currentTruck, response, AttachStatus.ENTRANCE_CARGO_PHOTO);
                                }
                                isCargoPhotoTaken = true;
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
//                                    saveOnScalePhoto(AttachStatus.ENTRANCE_CARGO_PHOTO);

                            if (isCargoPhotoTaken) {
                                System.out.println("Opening gate 2");
                                buttonController.openGate2(); // Open Gate 2
//                                if (weigh > 0) {
                                currentTruck.setEnteredWeight(weigh);

                                log.info("Truck entered weigh: {}", currentTruck.getEnteredWeight());
                                currentTruck.setEnteredAt(LocalDateTime.now());
                                currentTruck.setEntranceConfirmedBy(currentUser.getPhoneNumber());
                                //truckService.saveTruckStatus(currentTruck.getEnteredStatus(), ActionStatus.COMPLETE);
                                truckService.saveTruckEnteredActions(currentTruck);
                            }
                            // Save status as COMPLETE
                            try {
                                truckService.saveCurrentTruck(currentTruck, false);
                            } catch (Exception e) {
                                logService.save(new LogEntity(5L, Instances.truckNumber, "00042: (" + getClass().getName() + ") " + e.getMessage()));
                                e.printStackTrace();
                            }
                            tableController.updateTableRow(truckService.getCurrentTruckEntity());
                            isTruckEntered = true;
//                                } else {
//                                    isScaled = false;
//                                }
                        }
                    }
                }), SCALE_TIMEOUT);
                imageController.setIncomePhoto(new Image("/images/in/3.png"));
            }

            if (truckPosition == 2 && sensor2Connection && !sensor3Connection && isScaled) {
                imageController.setIncomePhoto(new Image("/images/in/3.1.png"));
                truckPosition = 3;
                System.out.println("truckPosition = " + truckPosition);
                controlPane.getButton3().setDisable(true);
                truckService.setCurrentTruckEntity(new TruckEntity());
            }

            if (truckPosition == 3 && sensor2Connection && sensor3Connection && isScaled) {
                System.out.println("Gate 2 is closing");
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("isTruckEntered = " + isTruckEntered);
                        if (isTruckEntered) {
                            currentTruck = new TruckResponse();
                            imageController.setIncomePhoto(new Image("/images/in/-1.png"));
                            truckPosition = -1;
                            checkPrinted = false;
                            isScaled = false;
                            buttonController.closeGate2();
                            isTruckEntered = false;
                            isCargoPhotoTaken = false;
                            isCargoConfirmationDialogOpened = false;
                            cargoConfirmationStatus = -1;
//                                isOnScale = false;
                            weigh = 0.0;
                        }
                    }
                }, CLOSE_GATE2_TIMEOUT);
            }

            if (isTimeoutChanged) {
                CLOSE_GATE2_TIMEOUT = configurations.getCloseGate2Timeout();
            }

            Thread.sleep(500);

        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00046: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
        }
//        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void startScaleExit() {
        try {
            if (truckExitPos != truckExitPosition) {
                System.out.println(LocalDateTime.now() + "Exit truckPosition = " + truckExitPosition);
                truckExitPos = truckExitPosition;
            }

//            System.out.println("Exit Gate 1 Connection old: " + gateExit1Connection);
//            System.out.println("Exit TruckPosition: " + truckExitPosition);
//            System.out.println("Exit Sensor 1 Connnection: " + sensorExit1Connection);

            if (!sensorExit1Connection && truckExitPosition == 0) {
                imageController.setOutPhoto(new Image("/images/out/1.png"));
                truckExitPosition = 1;
                isTruckEntered2 = true;
            }

            if (!gateExit1Connection && truckExitPosition == 0) {
                System.out.println("Exited gate 1 Connection: " + gateExit1Connection);
                System.out.println("FirstExitGateEntranceTime " + firstExitGateEntranceTime);
                if (System.currentTimeMillis() - firstExitGateEntranceTime >= 60000) {
                    System.out.println("Shlagbaum yopiladi");
                    buttonController.closeExitGate1();
                    System.out.println("Exit Truck Id: " + currentExitTruck == null ? null : currentExitTruck.getId());
                    imageController.setOutPhoto(new Image("/images/out/-1.png"));
                    truckExitPosition = -1;
                        if (currentExitTruck.getId() != null){
                            truckService.deleteTruckById(currentExitTruck.getId());
                        }
                }
            }

            if (truckExitPosition == 1 && sensorExit1Connection && (!sensorExit2Connection || isOnScale2)) {
                // Save status as PROCESSING
                //todo
                imageController.setOutPhoto(new Image("/images/out/1.1.png"));
                truckService.saveTruckStatus(currentExitTruck.getExitedStatus(), ActionStatus.PROCESSING);
                truckExitPosition = 2;
                buttonController.closeExitGate1();
            }

            if ((!sensorExit2Connection || isOnScale2) && (truckExitPosition == 2) &&
                    (currentExitTruck.getEnteredStatus() == TruckAction.EXIT ||
                            currentExitTruck.getEnteredStatus() == TruckAction.MANUAL_EXIT)) {
                imageController.setOutPhoto(new Image("/images/out/2.png"));
                System.out.println("Exit Scaling ---- isScaled = " + isScaled2 + " Status " + currentExitTruck.getEnteredStatus());
                //todo
                if (controlPane.getButton3().isDisable()) {
                    controlPane.getButton3().setDisable(false);
                }
                System.out.println("EXIT_TIMEOUT = " + EXIT_TIMEOUT);
                if (!isScaled2 && weigh2 <= 0) {
                    Timer timer1 = new Timer();
                    timer1.schedule((new TimerTask() {
                        @Override
                        public void run() {
                            if (weigh2 > 0) {
                                timer1.cancel();
                            } else {
                                setExitRescaleAttributes();
                            }
                        }
                    }), 60000);

                }
                Timer timer = new Timer();
                timer.schedule((new TimerTask() {
                    @Override
                    public void run() {
                        double holder = buttonController.getTruckExitWeigh();
                        double helper = holder != 0 ? holder : weigh2;
                        if (helper > 0.0) System.out.println("helper weigh2 = " + helper);

                        if (weigh2 != helper && helper != 0.0 /*&& !isTesting*/) {
                            weigh2 = helper;
                        } else if (weigh2 == helper && helper != 0) {
                            log.info("Truck weigh2: {}", weigh2);
                            isScaled2 = true;
                        }
                        if (isScaleControlOn) cargoConfirmationExitStatus = 1;

                        if (!isCargoConfirmationDialogOpened2 && isScaled2 && !isScaleControlOn && cargoConfirmationExitStatus == -1) {
                            isCargoConfirmationDialogOpened2 = true;
                            cargoConfirmationExitStatus = showCargoScaleConfirmationDialog(truckService.getCurrentExitTruckEntity(), weigh2);
                            System.out.println("cargoConfirmationStatus = " + cargoConfirmationExitStatus);
                        }

                        if (cargoConfirmationExitStatus == 2 && isScaled2 && isCargoConfirmationDialogOpened2) {
                            isScaled2 = false;
                            isCargoConfirmationDialogOpened2 = false;
                            weigh2 = 0.0;
                            cargoConfirmationExitStatus = -1;
                        }

                        if (isScaled2 && weigh2 > 0.0 && !isCargoPhotoTaken2 && cargoConfirmationExitStatus == 1) {
                            try {
                                AttachResponse response = cameraViewController.takePicture(CAMERA_3);
                                if (response != null) {
                                    truckService.saveTruckAttaches(currentExitTruck, response, AttachStatus.EXIT_CARGO_PHOTO);
                                }
                                isCargoPhotoTaken2 = true;
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
//                                    saveOnScalePhoto(AttachStatus.EXIT_CARGO_PHOTO);

                            if (isCargoPhotoTaken2) {
                                currentExitTruck.setExitedWeight(weigh2);
                                log.info("Truck weigh: {}", currentExitTruck.getExitedWeight());
                                currentExitTruck.setExitedAt(LocalDateTime.now());
                                System.out.println("currentUser.getPhoneNumber() = " + currentUser.getPhoneNumber());
                                isTruckExited2 = true;
                                currentExitTruck.setExitConfirmedBy(currentUser.getPhoneNumber());
                                truckService.saveTruckExitedAction(currentExitTruck);
                                //truckService.saveTruckStatus(currentTruck.getExitedStatus(), ActionStatus.COMPLETE);
                                TruckEntity truck = null;
                                try {
                                    truck = truckService.saveCurrentExitTruck(currentExitTruck, true);
                                } catch (Exception e) {
                                    logService.save(new LogEntity(5L, Instances.truckNumber, "00043: (" + getClass().getName() + ") " + e.getMessage()));
                                    e.printStackTrace();
                                }
                                try {
                                    cargoService.saveCargo(truck);
                                } catch (Exception e) {
                                    logService.save(new LogEntity(5L, Instances.truckExitNumber, "00044: (" + getClass().getName() + ") " + e.getMessage()));
                                    e.printStackTrace();
                                }
                                try {
//                                    Timer timer1 = new Timer();
//                                    timer1.schedule(new TimerTask() {
//                                        @Override
//                                        public void run() {
                                    if (!checkPrinted2){
                                        printCheck.printReceipt(truckService.getCurrentExitTruckEntity());
                                        checkPrinted2 = true;
                                    }
                                    truckService.save(truckService.getCurrentExitTruckEntity());
//                                        }
//                                    }, 50);
                                } catch (Exception e) {
                                    logService.save(new LogEntity(5L, Instances.truckExitNumber, "00045: (" + getClass().getName() + ") " + e.getMessage()));
                                    e.printStackTrace();
                                    System.out.println(e.getMessage());
                                }
                                tableController.updateTableRow(truckService.getCurrentExitTruckEntity());
                            }

                        }
                    }
                }), SCALE_TIMEOUT);
                imageController.setOutPhoto(new Image("/images/out/3.png"));
            }

            if (truckExitPosition == 2 && (!sensorExit2Connection || isOnScale2) && cargoConfirmationExitStatus == 0) { // removed isScaled boolean condition
                truckService.getCurrentExitTruckEntity().setNextEntranceTime(LocalDateTime.now().plusMinutes(EXIT_TIMEOUT));
                System.out.println("Chiqish vaqti: " + EXIT_TIMEOUT);
                truckService.save(truckService.getCurrentExitTruckEntity());
                buttonController.openExitGate2();
                truckExitPosition = 2;
                isScaled2 = true; // needed for exiting
                CLOSE_GATE2_EXIT_TIMEOUT = CLOSE_GATE2_EXIT_TIMEOUT * 3;
                isTimeoutChanged2 = true;
                cargoConfirmationExitStatus = -1;
                isCargoConfirmationDialogOpened2 = true;
                isTruckEntered2 = true;
                isTruckExited2 = false;
                currentExitTruck = new TruckResponse();
                System.out.println("Gate 2 is opening");
            }

            if (truckExitPosition == 2 && (!sensorExit2Connection || isOnScale2) && isScaled2 && cargoConfirmationExitStatus == 1 && gateExit2Connection) {
                imageController.setOutPhoto(new Image("/images/out/3.png"));
                System.out.println("Opening gate 1");
                buttonController.openExitGate2();
            }

            if (truckExitPosition == 2 && sensorExit2Connection && !sensorExit3Connection && isScaled2 && cargoConfirmationExitStatus == 1 && !gateExit1Connection) {
                imageController.setIncomePhoto(new Image("/images/out/3.1.png"));
                isTruckExited2 = true;
                truckExitPosition = 3;
                controlPane.getButton3().setDisable(true);
                truckService.setCurrentExitTruckEntity(new TruckEntity());
            }

            if (truckExitPosition == 3 && sensorExit2Connection && sensorExit3Connection && isScaled2 && cargoConfirmationExitStatus == 1) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (isTruckExited2) {
                            currentExitTruck = new TruckResponse();
                            imageController.setIncomePhoto(new Image("/images/out/-1.png"));
                            isScaled2 = false;
                            buttonController.closeExitGate2();
                            isTruckExited2 = false;
                            isCargoPhotoTaken2 = false;
                            isCargoConfirmationDialogOpened2 = false;
//                                isOnScale = false;
                            cargoConfirmationExitStatus = -1;
                            truckExitPosition = -1;
                            checkPrinted2 = false;
                            weigh2 = 0.0;
                        }
                    }
                }, CLOSE_GATE1_EXIT_TIMEOUT);
            }

            Thread.sleep(500);

        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00046: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
        }
//        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void saveOnScalePhoto(AttachStatus exitCargoPhoto) {
        new Thread(() -> {
            try {
                AttachResponse response = cameraViewController.takePicture(CAMERA_2);
                truckService.saveTruckAttaches(currentTruck, response, exitCargoPhoto);
                isCargoPhotoTaken = true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }).start();

    }

    public void reinitialize() {
        isTruckEntered = false;
        isTruckExited = false;
        isOnScale = false;
        isScaled = false;
        isCargoPhotoTaken = false;
        isCargoConfirmationDialogOpened = false;
        isTimeoutChanged = false;
        isWaiting = false;
        weigh = 0.0;
    }

    public void reinitializeExit() {
        isTruckEntered2 = false;
        isTruckExited2 = false;
        isOnScale2 = false;
        isScaled2 = false;
        isCargoPhotoTaken2 = false;
        isCargoConfirmationDialogOpened2 = false;
        isTimeoutChanged2 = false;
        isExitWaiting = false;
        weigh2 = 0.0;
    }

//    public void setRescaleAttributes() {
//        if (truckExitPosition != 2 && truckExitPosition != 5) {
//            System.out.println("Moshina belgilangan pozitsiyada emas!");
//            return;
//        }
//        if (truckExitPosition == 2) {
//            isScaled2 = false;
//            isTruckEntered2 = false;
//            isCargoPhotoTaken2 = false;
//            cargoConfirmationExitStatus = -1;
//            weigh = 0.0;
//        }
//        if (truckExitPosition == 5) {
//            isScaled2 = false;
//            isTruckExited2 = false;
//            isCargoPhotoTaken2 = false;
//            isCargoConfirmationDialogOpened2 = false;
//            weigh2 = 0.0;
//        }
//    }

    public void setRescaleAttributes() {
        if (truckPosition != 2) {
            System.out.println("Moshina belgilangan pozitsiyada emas!");
            return;
        }
        if (truckPosition == 2) {
            isScaled = false;
            isTruckEntered = false;
            isCargoPhotoTaken = false;
            cargoConfirmationStatus = -1;
            weigh = 0.0;
        }
    }

    public void setExitRescaleAttributes() {
        if (truckExitPosition != 2) {
            System.out.println("Moshina belgilangan pozitsiyada emas!");
            return;
        }
        if (truckExitPosition == 2) {
            isScaled2 = false;
            isTruckExited = false;
            isCargoPhotoTaken2 = false;
            cargoConfirmationExitStatus = -1;
            weigh2 = 0.0;
        }
    }
}
