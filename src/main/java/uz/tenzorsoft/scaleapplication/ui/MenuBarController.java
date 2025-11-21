package uz.tenzorsoft.scaleapplication.ui;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.repository.TruckActionRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;
import uz.tenzorsoft.scaleapplication.service.ConfigUtilsService;
import uz.tenzorsoft.scaleapplication.service.ExcelService;
import uz.tenzorsoft.scaleapplication.service.PrintCheck;
import uz.tenzorsoft.scaleapplication.service.raspberry.GpioControl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;

@Component
@RequiredArgsConstructor
public class MenuBarController implements BaseController {

    private final ConfigUtilsService configUtilsService;
    private final PrintCheck printCheck;
    private final TruckRepository truckRepository;
    private final TruckActionRepository truckActionRepository;
    private final ControlPane controlPane;
    private final GpioControl gpioControl;

    @Autowired
    @Lazy
    private ConnectionsController connectionsController;



    @Autowired
    private TableController tableController;

//    @FXML
//    private void handleKppRequestsMenuAction() {
//        if (kppRequestService == null) {
//            showAlert(Alert.AlertType.ERROR, "Xizmat Topilmadi", "KPP So'rovlar xizmati yuklanmagan.");
//            return;
//        }
//        if (gpioControl == null) { // GpioControl mavjudligini tekshirish
//            showAlert(Alert.AlertType.ERROR, "Xizmat Topilmadi", "GPIO Boshqaruv xizmati yuklanmagan.");
//            return;
//        }
//
//        Stage dialogStage = new Stage();
//        dialogStage.initModality(Modality.APPLICATION_MODAL);
//        dialogStage.setTitle("KPP Kirish So'rovlari");
//
//        ListView<KppAccessRequest> requestListView = new ListView<>();
//        requestListView.setItems(kppRequestService.getAllKppRequests()); // ObservableList ni bog'lash
//
//        requestListView.setCellFactory(lv -> new ListCell<KppAccessRequest>() {
//            private final Button approveButton = new Button("Tasdiqlash va Ochish");
//            private final HBox hbox = new HBox(15); // Elementlar orasidagi masofa
//            private final Label requestTextLabel = new Label();
//
//            {
//                hbox.setAlignment(Pos.CENTER_LEFT);
//                approveButton.setOnAction(event -> {
//                    KppAccessRequest request = getItem();
//                    if (request != null && !request.isApproved()) {
//                        // KppRequestService.approveRequestAndOpenGate endi GpioControl ni o'zida ishlatadi
//                        boolean success = kppRequestService.approveRequestAndOpenGate(request.getId());
//                        if (!success) {
//                            showAlert(Alert.AlertType.ERROR, "Xatolik", "KPP darvozasini ochishda muammo yuz berdi. Loglarni tekshiring.");
//                        }
//                        // UI avtomatik yangilanishi kerak (BooleanProperty tufayli)
//                    }
//                });
//            }
//
//            @Override
//            protected void updateItem(KppAccessRequest request, boolean empty) {
//                super.updateItem(request, empty);
//                if (empty || request == null) {
//                    setText(null);
//                    setGraphic(null);
//                    // Eski bog'lanishlarni tozalash (agar bo'lsa)
//                    if (getGraphic() instanceof HBox) {
//                        Node buttonNode = ((HBox) getGraphic()).getChildren().get(1);
//                        if(buttonNode instanceof Button) {
//                            buttonNode.visibleProperty().unbind();
//                            buttonNode.managedProperty().unbind();
//                        }
//                    }
//                } else {
//                    requestTextLabel.setText(request.toString());
//
//                    // Tugmani faqat tasdiqlanmagan so'rovlar uchun sozlash
//                    approveButton.visibleProperty().bind(request.approvedProperty().not());
//                    approveButton.managedProperty().bind(request.approvedProperty().not());
//
//                    hbox.getChildren().setAll(requestTextLabel, approveButton);
//                    setGraphic(hbox);
//                }
//            }
//        });
//
//        Button clearApprovedBtn = new Button("Tasdiqlanganlarni O'chirish");
//        clearApprovedBtn.setOnAction(e -> kppRequestService.clearApprovedRequests());
//
//        Label titleLabel = new Label("Aktiv KPP So'rovlari:");
//        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
//
//        VBox layout = new VBox(10, titleLabel, requestListView, clearApprovedBtn);
//        layout.setPadding(new Insets(15));
//        layout.setPrefSize(650, 450);
//
//        Scene scene = new Scene(layout);
//        dialogStage.setScene(scene);
//        dialogStage.showAndWait();
//    }
    public void initialize() {

        camera1Field.setText(CAMERA_1);
        cameraRezervField1.setText(CAMERA_REZERV1);
        camera2Field.setText(CAMERA_2);


        camera3Field.setText(CAMERA_3);
        camera4Field.setText(CAMERA_4);
        cameraRezervField2.setText(CAMERA_REZERV2);

        controllerAddressField.setText(CONTROLLER_IP);
        portFieldController.setText(CONTROLLER_PORT.toString());
        timeoutField.setText(CONTROLLER_CONNECT_TIMEOUT.toString());

        kppkirishshalgbaum.setText(KPP_CLOSE_GATE1_TIMEOUT + "");
        kppchiqishshalgbaum.setText(KPP_CLOSE_GATE2_TIMEOUT +"");
        kpppinInIn.setText(KPP_PIN_IN_IN != null ? KPP_PIN_IN_IN.toString() : "");
        kpppinInOut.setText(KPP_PIN_IN_OUT != null ? KPP_PIN_IN_OUT.toString() : "");
        kpppinOutIn.setText(KPP_EXIT_PIN_IN_IN != null ? KPP_EXIT_PIN_IN_IN.toString() : "");
        kpppinOutOut.setText(KPP_EXIT_PIN_IN_OUT != null ? KPP_EXIT_PIN_IN_OUT.toString() : "");

        firstShlagbaumField.setText("" + CLOSE_GATE1_TIMEOUT);
        secondShlagbaumField.setText("" + CLOSE_GATE2_TIMEOUT);
        pinInIn.setText(PIN_IN_IN != null ? PIN_IN_IN.toString() : "");
        pinInOut.setText(PIN_IN_OUT != null ? PIN_IN_OUT.toString() : "");
        pinInIn2.setText(PIN_IN_IN2 != null ? PIN_IN_IN2.toString() : "");
        pinInOut2.setText(PIN_IN_OUT2 != null ? PIN_IN_OUT2.toString() : "");

        firstShlagbaumFieldExit.setText("" + CLOSE_GATE1_EXIT_TIMEOUT);
        secondShlagbaumFieldExit.setText("" + CLOSE_GATE2_EXIT_TIMEOUT);
        pinOutIn.setText(PIN_OUT_IN != null ? PIN_OUT_IN.toString() : "");
        pinOutOut.setText(PIN_OUT_OUT != null ? PIN_OUT_OUT.toString() : "");
        pinOutIn2.setText(PIN_OUT_IN2 != null ? PIN_OUT_IN2.toString() : "");
        pinOutOut2.setText(PIN_OUT_OUT2 != null ? PIN_OUT_OUT2.toString() : "");

        massTimeField.setText(SCALE_TIMEOUT != null ? SCALE_TIMEOUT.toString() : "");
        portField.setText(SCALE_PORT);

        massTimeFieldOut.setText(EXIT_TIMEOUT != null ? EXIT_TIMEOUT.toString() : "");
        portFieldOut.setText(SCALE_EXIT_PORT);
        printerNameField.setText(PRINTER_NAME);

        sensorPinIn1.setText(SENSOR_IN1 != null ? SENSOR_IN1.toString() : "");
        sensorPinIn2.setText(SENSOR_IN2 != null ? SENSOR_IN2.toString() : "");
        sensorPinIn3.setText(SENSOR_IN3 != null ? SENSOR_IN3.toString() : "");

        sensorPinOut1.setText(SENSOR_OUT1 != null ? SENSOR_OUT1.toString() : "");
        sensorPinOut2.setText(SENSOR_OUT2 != null ? SENSOR_OUT2.toString() : "");
        sensorPinOut3.setText(SENSOR_OUT3 != null ? SENSOR_OUT3.toString() : "");
    }

    private Node incoms() {
        AnchorPane camera = showCameraPopup();
//        Node shlagbaum = showshlagbaumPopup();
        Node node = showShlagbaumPopup();
        Node node1 = showTaroziPopup();
        Node node2 = showKPPINPopup();
        AnchorPane sensor = sensorIn();
        return new AnchorPane(new VBox(5, node2, camera,  node, node1, sensor));
    }

    private Node outs() {
        AnchorPane camera = showCameraPopupOut();
        Node node = showShlagbaumPopupOut();
        Node node1 = showTaroziPopupOut();
        Node node2 = showKPPOutPopup();
        AnchorPane sensor = sensorOut();
        return new AnchorPane(new VBox(5,node2, camera, node, node1, sensor));
    }

    private void setSensorPins() {
        RASP_SENSOR_1 = SENSOR_IN1;
        RASP_SENSOR_2 = SENSOR_IN2;
        RASP_SENSOR_3 = SENSOR_IN3;

        RASP_SENSOR_EXIT_1 = SENSOR_OUT1;
        RASP_SENSOR_EXIT_2 = SENSOR_OUT2;
        RASP_SENSOR_EXIT_3 = SENSOR_OUT3;

        RASP_OPEN_GATE_1 = PIN_IN_IN;
        RASP_CLOSE_GATE_1 = PIN_IN_OUT;
        RASP_OPEN_GATE_2 = PIN_IN_IN2;
        RASP_CLOSE_GATE_2 = PIN_IN_OUT2;

        RASP_OPEN_GATE_EXIT_1 = PIN_OUT_IN;
        RASP_CLOSE_GATE_EXIT_1 = PIN_OUT_OUT;
        RASP_OPEN_GATE_EXIT_2 = PIN_OUT_IN2;
        RASP_CLOSE_GATE_EXIT_2 = PIN_OUT_OUT2;

        STATUS_PINS = new int[]{RASP_SENSOR_1, RASP_SENSOR_2, RASP_SENSOR_3, RASP_SENSOR_EXIT_1, RASP_SENSOR_EXIT_2, RASP_SENSOR_EXIT_3};
        CONTROL_PINS = new int[]{RASP_GREEN_LIGHT_1, RASP_GREEN_LIGHT_2, RASP_OPEN_GATE_1, RASP_CLOSE_GATE_1, RASP_OPEN_GATE_2, RASP_CLOSE_GATE_2, RASP_GREEN_LIGHT_EXIT_1, RASP_GREEN_LIGHT_EXIT_2, RASP_OPEN_GATE_EXIT_1, RASP_CLOSE_GATE_EXIT_1, RASP_OPEN_GATE_EXIT_2, RASP_CLOSE_GATE_EXIT_2};
    }

    @FXML
    private void settingsTabs() {
        initialize();
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Sozlamalar");

        TabPane tabPane = new TabPane();

        Button saveButton = new Button("Saqlash");
        Button cancelButton = new Button("Bekor qilish");

        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    kppkirishshalgbaum.getText().equals(KPP_CLOSE_GATE1_TIMEOUT) &&
                            kppchiqishshalgbaum.getText().equals(KPP_CLOSE_GATE2_TIMEOUT) &&
                            camera1Field.getText().equals(CAMERA_1) &&
                            cameraRezervField1.getText().equals(CAMERA_REZERV1) &&
                            camera2Field.getText().equals(CAMERA_2) &&
                            camera3Field.getText().equals(CAMERA_3) &&
                            camera4Field.getText().equals(CAMERA_4) &&
                            cameraRezervField2.getText().equals(CAMERA_REZERV2) &&
                            controllerAddressField.getText().equals(CONTROLLER_IP) &&
                            portFieldController.getText().equals(CONTROLLER_PORT) &&
                            timeoutField.getText().equals(CONTROLLER_CONNECT_TIMEOUT) &&
                            firstShlagbaumField.getText().equals(CLOSE_GATE1_TIMEOUT) &&
                            secondShlagbaumField.getText().equals(CLOSE_GATE2_TIMEOUT) &&
                            pinInIn.getText().equals(PIN_IN_IN) &&
                            pinInOut.getText().equals(PIN_IN_OUT) &&
                            pinInIn2.getText().equals(PIN_IN_IN2) &&
                            pinInOut2.getText().equals(PIN_IN_OUT2) &&
                            kpppinInIn.getText().equals(KPP_PIN_IN_IN) &&
                            kpppinInOut.getText().equals(KPP_PIN_IN_OUT) &&
                            kpppinOutIn.getText().equals((KPP_EXIT_PIN_IN_IN))&&
                            kpppinOutOut.getText().equals((KPP_EXIT_PIN_IN_OUT))&&
                            firstShlagbaumFieldExit.getText().equals(CLOSE_GATE1_EXIT_TIMEOUT) &&
                            secondShlagbaumFieldExit.getText().equals(CLOSE_GATE2_EXIT_TIMEOUT) &&
                            pinOutIn.getText().equals(PIN_OUT_IN) &&
                            pinOutOut.getText().equals(PIN_OUT_OUT) &&
                            pinOutIn2.getText().equals(PIN_OUT_IN2) &&
                            pinOutOut2.getText().equals(PIN_OUT_OUT2) &&
                            massTimeField.getText().equals(SCALE_TIMEOUT) &&
                            portField.getText().equals(SCALE_PORT) &&
                            massTimeFieldOut.getText().equals(EXIT_TIMEOUT) &&
                            portFieldOut.getText().equals(SCALE_EXIT_PORT) &&
                            printerNameField.getText().equals(PRINTER_NAME) &&
                            sensorPinIn1.getText().equals(SENSOR_IN1) &&
                            sensorPinIn2.getText().equals(SENSOR_IN2) &&
                            sensorPinIn3.getText().equals(SENSOR_IN3) &&
                            sensorPinOut1.getText().equals(SENSOR_OUT1) &&
                            sensorPinOut2.getText().equals(SENSOR_OUT2) &&
                            sensorPinOut3.getText().equals(SENSOR_OUT3)
            );
        };

        kppkirishshalgbaum.textProperty().addListener(changeListener);
        kppchiqishshalgbaum.textProperty().addListener(changeListener);
        camera1Field.textProperty().addListener(changeListener);
        cameraRezervField1.textProperty().addListener(changeListener);
        camera2Field.textProperty().addListener(changeListener);
        camera3Field.textProperty().addListener(changeListener);
        camera4Field.textProperty().addListener(changeListener);
        cameraRezervField2.textProperty().addListener(changeListener);
        controllerAddressField.textProperty().addListener(changeListener);
        portFieldController.textProperty().addListener(changeListener);
        timeoutField.textProperty().addListener(changeListener);
        firstShlagbaumField.textProperty().addListener(changeListener);
        secondShlagbaumField.textProperty().addListener(changeListener);
        pinInIn.textProperty().addListener(changeListener);
        pinInOut.textProperty().addListener(changeListener);
        pinInIn2.textProperty().addListener(changeListener);
        pinInOut2.textProperty().addListener(changeListener);
        kpppinInIn.textProperty().addListener(changeListener);
        kpppinInOut.textProperty().addListener(changeListener);
        kpppinOutIn.textProperty().addListener(changeListener);
        kpppinOutOut.textProperty().addListener(changeListener);
        firstShlagbaumFieldExit.textProperty().addListener(changeListener);
        secondShlagbaumFieldExit.textProperty().addListener(changeListener);
        pinOutIn.textProperty().addListener(changeListener);
        pinOutOut.textProperty().addListener(changeListener);
        pinOutIn2.textProperty().addListener(changeListener);
        pinOutOut2.textProperty().addListener(changeListener);
        massTimeField.textProperty().addListener(changeListener);
        portField.textProperty().addListener(changeListener);
        massTimeFieldOut.textProperty().addListener(changeListener);
        portFieldOut.textProperty().addListener(changeListener);
        printerNameField.textProperty().addListener(changeListener);
        sensorPinIn1.textProperty().addListener(changeListener);
        sensorPinIn2.textProperty().addListener(changeListener);
        sensorPinIn3.textProperty().addListener(changeListener);
        sensorPinOut1.textProperty().addListener(changeListener);
        sensorPinOut2.textProperty().addListener(changeListener);
        sensorPinOut3.textProperty().addListener(changeListener);

        saveButton.setDisable(true);
        saveButton.setOnAction(event -> {
            gpioControl.soutStatuses();
            // gpioControl.removeAllPins();
            //  gpioControl.soutStatuses();
            //   gpioControl.shutdown();
//            gpioControl.shutdownCompletely();
            //  gpioControl.initialize();

            KPP_CLOSE_GATE1_TIMEOUT = Integer.parseInt(kppkirishshalgbaum.getText());
            KPP_CLOSE_GATE2_TIMEOUT = Integer.parseInt(kppkirishshalgbaum.getText());
            KPP_PIN_IN_IN = Integer.parseInt(pinInIn.getText());
            KPP_PIN_IN_OUT = Integer.parseInt(pinInOut.getText());
            KPP_EXIT_PIN_IN_IN = Integer.parseInt(pinOutIn.getText());
            KPP_EXIT_PIN_IN_OUT = Integer.parseInt(pinOutOut.getText());

            CAMERA_1 = camera1Field.getText();
            CAMERA_REZERV1 = cameraRezervField1.getText();
            CAMERA_2 = camera2Field.getText();
            CAMERA_3 = camera3Field.getText();
            CAMERA_4 = camera4Field.getText();
            CAMERA_REZERV2 = cameraRezervField2.getText();
            CONTROLLER_IP = controllerAddressField.getText();
            CONTROLLER_PORT = Integer.parseInt(portFieldController.getText());
            CONTROLLER_CONNECT_TIMEOUT = Integer.parseInt(timeoutField.getText());
            CLOSE_GATE1_TIMEOUT = Integer.parseInt(firstShlagbaumField.getText());
            CLOSE_GATE2_TIMEOUT = Integer.parseInt(secondShlagbaumField.getText());
            PIN_IN_IN = Integer.parseInt(pinInIn.getText());
            PIN_IN_OUT = Integer.parseInt(pinInOut.getText());
            PIN_IN_IN2 = Integer.parseInt(pinInIn2.getText());
            PIN_IN_OUT2 = Integer.parseInt(pinInOut2.getText());
            CLOSE_GATE1_EXIT_TIMEOUT = Integer.parseInt(firstShlagbaumFieldExit.getText());
            CLOSE_GATE2_EXIT_TIMEOUT = Integer.parseInt(secondShlagbaumFieldExit.getText());
            PIN_OUT_IN = Integer.parseInt(pinOutIn.getText());
            PIN_OUT_OUT = Integer.parseInt(pinOutOut.getText());
            PIN_OUT_IN2 = Integer.parseInt(pinOutIn2.getText());
            PIN_OUT_OUT2 = Integer.parseInt(pinOutOut2.getText());
            SCALE_TIMEOUT = Integer.parseInt(massTimeField.getText());
            SCALE_PORT = portField.getText();
            EXIT_TIMEOUT = Integer.parseInt(massTimeFieldOut.getText());
            SCALE_EXIT_PORT = portFieldOut.getText();
            PRINTER_NAME = printerNameField.getText();
            SENSOR_IN1 = Integer.parseInt(sensorPinIn1.getText());
            SENSOR_IN2 = Integer.parseInt(sensorPinIn2.getText());
            SENSOR_IN3 = Integer.parseInt(sensorPinIn3.getText());
            SENSOR_OUT1 = Integer.parseInt(sensorPinOut1.getText());
            SENSOR_OUT2 = Integer.parseInt(sensorPinOut2.getText());
            SENSOR_OUT3 = Integer.parseInt(sensorPinOut3.getText());


            configurations.setKppcloseGate1Timeout(Integer.parseInt(kppkirishshalgbaum.getText()));
            configurations.setKpppinInIn(Integer.parseInt(kpppinInIn.getText()));
            configurations.setKpppinInOut(Integer.parseInt(kpppinInOut.getText()));
            configurations.setKpppinOutIn(Integer.parseInt(kpppinOutIn.getText()));
            configurations.setKpppinOutOut(Integer.parseInt(kpppinOutOut.getText()));
            configurations.setKppcloseGate2Timeout(Integer.parseInt(kppchiqishshalgbaum.getText()));
            configurations.setCamera1(camera1Field.getText());
            configurations.setCamera2(camera2Field.getText());
            configurations.setCamera3(camera3Field.getText());
            configurations.setCamera4(camera4Field.getText());
            configurations.setCameraRezerv1(cameraRezervField1.getText());
            configurations.setCameraRezerv2(cameraRezervField2.getText());
            configurations.setControllerIp(controllerAddressField.getText());
            configurations.setControllerPort(Integer.parseInt(portFieldController.getText()));
            configurations.setCloseGate1Timeout(Integer.parseInt(firstShlagbaumField.getText()));
            configurations.setCloseGate2Timeout(Integer.parseInt(secondShlagbaumField.getText()));
            configurations.setPinInIn(Integer.parseInt(pinInIn.getText()));
            configurations.setPinInOut(Integer.parseInt(pinInOut.getText()));
            configurations.setPinInIn2(Integer.parseInt(pinInIn2.getText()));
            configurations.setPinInOut2(Integer.parseInt(pinInOut2.getText()));
            configurations.setCloseGateExit1Timeout(Integer.parseInt(firstShlagbaumFieldExit.getText()));
            configurations.setCloseGateExit2Timeout(Integer.parseInt(secondShlagbaumFieldExit.getText()));
            configurations.setPinOutIn(Integer.parseInt(pinOutIn.getText()));
            configurations.setPinOutOut(Integer.parseInt(pinOutOut.getText()));
            configurations.setPinOutIn2(Integer.parseInt(pinOutIn2.getText()));
            configurations.setPinOutOut2(Integer.parseInt(pinOutOut2.getText()));
            configurations.setScaleTimeout(Integer.parseInt(massTimeField.getText()));
            configurations.setScalePort(portField.getText());
            configurations.setExitTimeout(Integer.parseInt(massTimeFieldOut.getText()));
            configurations.setScaleExitPort(portFieldOut.getText());
            configurations.setPrinterName(printerNameField.getText());
            configurations.setSensorIn1(Integer.parseInt(sensorPinIn1.getText()));
            configurations.setSensorIn2(Integer.parseInt(sensorPinIn2.getText()));
            configurations.setSensorIn3(Integer.parseInt(sensorPinIn3.getText()));
            configurations.setSensorOut1(Integer.parseInt(sensorPinOut1.getText()));
            configurations.setSensorOut2(Integer.parseInt(sensorPinOut2.getText()));
            configurations.setSensorOut3(Integer.parseInt(sensorPinOut3.getText()));

//            DATABASE_NAME = nameField.getText();
//            USERNAME = loginField.getText();
//            PASSWORD = passwordField.getText();
//            configurations.setDatabaseName(nameField.getText());
//            configurations.setUsername(loginField.getText());
//            configurations.setPassword(passwordField.getText());
            configUtilsService.saveConfig(configurations);
            // Add save logic here
            setSensorPins();

            gpioControl.initialize();
            gpioControl.getSensorStatuses();

            connectionsController.updateConnections();
//            connectionsController.showConnections();

            popupStage.close();
        });
        cancelButton.setOnAction(event -> {
            setDefoultSettings();
            popupStage.close();
        });

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        Tab inCamera = new Tab("Kirish");
        inCamera.setContent(incoms());
        inCamera.setClosable(false);

        Tab outCamera = new Tab("Chiqish");
        outCamera.setContent(outs());
        outCamera.setClosable(false);

        Tab printer = new Tab("Printer");
        printer.setContent(showPrinterPopup());
        printer.setClosable(false);
        Tab controller = new Tab("Controller");
        controller.setContent(showControllerPopup());
        controller.setClosable(false);

        tabPane.getTabs().add(inCamera);
        tabPane.getTabs().add(outCamera);
        tabPane.getTabs().add(printer);
        tabPane.getTabs().add(controller);

        HBox hBox = new HBox(10, saveButton, cancelButton);
        AnchorPane anchorPane = new AnchorPane(tabPane, hBox);
        AnchorPane.setBottomAnchor(hBox, 10.0);
        AnchorPane.setRightAnchor(hBox, 10.0);

        AnchorPane.setBottomAnchor(tabPane, 40.0);

        Scene popupScene = new Scene(anchorPane);
        popupStage.setScene(popupScene);
        popupStage.setResizable(false);
        popupStage.show();
    }

    private void setDefoultSettings() {
        RASP_SENSOR_1 = SENSOR_IN1;
        RASP_SENSOR_2 = SENSOR_IN2;
        RASP_SENSOR_3 = SENSOR_IN3;

        RASP_SENSOR_EXIT_1 = SENSOR_OUT1;
        RASP_SENSOR_EXIT_2 = SENSOR_OUT2;
        RASP_SENSOR_EXIT_3 = SENSOR_OUT3;

        RASP_OPEN_GATE_1 = PIN_IN_IN;
        RASP_CLOSE_GATE_1 = PIN_IN_OUT;
        RASP_OPEN_GATE_2 = PIN_IN_IN2;
        RASP_CLOSE_GATE_2 = PIN_IN_OUT2;

        RASP_OPEN_GATE_EXIT_1 = PIN_OUT_IN;
        RASP_CLOSE_GATE_EXIT_1 = PIN_OUT_OUT;
        RASP_OPEN_GATE_EXIT_2 = PIN_OUT_IN2;
        RASP_CLOSE_GATE_EXIT_2 = PIN_OUT_OUT2;

        KPP_OPEN_GATE_EXIT_1 = KPP_PIN_IN_IN;
        KPP_OPEN_GATE_EXIT_2 = KPP_EXIT_PIN_IN_IN;
        KPP_CLOSE_GATE_EXIT_1 = KPP_PIN_IN_OUT;
        KPP_CLOSE_GATE_EXIT_2 = KPP_EXIT_PIN_IN_OUT;

        STATUS_PINS = new int[]{RASP_SENSOR_1, RASP_SENSOR_2, RASP_SENSOR_3, RASP_SENSOR_EXIT_1, RASP_SENSOR_EXIT_2, RASP_SENSOR_EXIT_3};
        CONTROL_PINS = new int[]{RASP_GREEN_LIGHT_1, RASP_GREEN_LIGHT_2, RASP_OPEN_GATE_1, RASP_CLOSE_GATE_1, RASP_OPEN_GATE_2, RASP_CLOSE_GATE_2, RASP_GREEN_LIGHT_EXIT_1, RASP_GREEN_LIGHT_EXIT_2, RASP_OPEN_GATE_EXIT_1, RASP_CLOSE_GATE_EXIT_1, RASP_OPEN_GATE_EXIT_2, RASP_CLOSE_GATE_EXIT_2};

    }


    private static Node anchorSet(Node node, Double left, Double top, Double right, Double bottom) {
        if (left != null)
            AnchorPane.setLeftAnchor(node, left);
        if (left != null)
            AnchorPane.setTopAnchor(node, top);
        if (left != null)
            AnchorPane.setRightAnchor(node, right);
        if (left != null)
            AnchorPane.setBottomAnchor(node, bottom);
        return node;
    }

    @FXML
    private void onCameraMenuSelected() {
        showCameraPopup();
    }



    @FXML
    private void onAboutMenuSelected() {
        aboutMenuSelected();
    }

    @FXML
    private void onInstructionSelected() {
        showInstructionPopup();
    }

    @Autowired
    private ApplicationContext applicationContext;


    @FXML
    private void onHisobotMenuSelected() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportDialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean); // Assuming Spring context integration
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Hisobotlar");
            stage.setScene(new Scene(root, 700, 700)); // Increased size
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onHisobotExcelMenuSelected() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportExcel.fxml"));
            Parent root = loader.load();
            ReportExcelController controller = loader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Export to Excel");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isConfirmed()) {
                // Fetch filtered data
                List<TableViewData> filteredData = tableController.getFilteredData();

                // Open FileChooser for user to save the file
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Excel File");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    // Define report title
                    String reportTitle = "Baxt Komir Ombori";

                    // Export data to the chosen file
                    ExcelService.export(filteredData, file, reportTitle, null);

                    showAlert(Alert.AlertType.INFORMATION, "Muvaffaqiyatli", "Maʼlumotlar muvaffaqiyatli eksport qilindi!");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
    }


    private void showDatabasePopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Ma'lumotlar bazasi");

        // Form Elements
        Label nameLabel = new Label("Ma'lumotlar bazasi nomi:");
        TextField nameField = new TextField(DATABASE_NAME);

        Label loginLabel = new Label("Login:");
        TextField loginField = new TextField(USERNAME);

        Label passwordLabel = new Label("Parol:");
        PasswordField passwordField = new PasswordField();
        passwordField.setText(PASSWORD);

        TextField visiblePasswordField = new TextField(PASSWORD);
        visiblePasswordField.setVisible(false);

        ImageView openEye = new ImageView(new Image("/images/opened-eye.png"));
        ImageView closedEye = new ImageView(new Image("/images/closed-eye.png"));
        openEye.setFitWidth(20);
        openEye.setFitHeight(20);
        closedEye.setFitWidth(20);
        closedEye.setFitHeight(20);

        Button eyeButton = new Button("", closedEye);

        eyeButton.setOnAction(event -> {
            boolean isPasswordVisible = visiblePasswordField.isVisible();
            visiblePasswordField.setVisible(!isPasswordVisible);
            passwordField.setVisible(isPasswordVisible);
            eyeButton.setGraphic(isPasswordVisible ? closedEye : openEye);
            if (isPasswordVisible) {
                passwordField.setText(visiblePasswordField.getText());
            } else {
                visiblePasswordField.setText(passwordField.getText());
            }
        });

        HBox passwordBox = new HBox(passwordField, visiblePasswordField, eyeButton);
        passwordBox.setSpacing(5);
        passwordBox.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        Button saveButton = new Button("Saqlash");
        Button cancelButton = new Button("Bekor qilish");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    nameField.getText().equals(DATABASE_NAME) &&
                            loginField.getText().equals(USERNAME) &&
                            passwordField.getText().equals(PASSWORD)
            );
        };

        nameField.textProperty().addListener(changeListener);
        loginField.textProperty().addListener(changeListener);
        passwordField.textProperty().addListener(changeListener);
        visiblePasswordField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
            DATABASE_NAME = nameField.getText();
            USERNAME = loginField.getText();
            PASSWORD = passwordField.getText();
            configurations.setDatabaseName(nameField.getText());
            configurations.setUsername(loginField.getText());
            configurations.setPassword(passwordField.getText());
            configUtilsService.saveConfig(configurations);
            // Add save logic here
            popupStage.close();
        });
        cancelButton.setOnAction(event -> popupStage.close());

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(saveButton, cancelButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(5, nameLabel, nameField, loginLabel, loginField, passwordLabel, passwordBox, buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 350, 300);
        popupStage.setScene(popupScene);
        popupStage.setResizable(false);
        popupStage.show();
    }

    TextField camera1Field = new TextField();
    TextField cameraRezervField1 = new TextField();
    TextField camera2Field = new TextField();


    private AnchorPane showCameraPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Kirish Kamerasi
        Label camera = new Label("Camera");

        Label camera1Label = new Label("1-kamera kirish IP manzili:");
        camera1Field.setPrefWidth(80);

        Label rezervCam = new Label("Rezerv kamera:");
        cameraRezervField1.setPrefWidth(80);

        // Yuk Kamerasi
        Label camera2Label = new Label("2-kamera yuk IP manzili:");
        camera2Field.setPrefWidth(80);


        VBox v = new VBox(5, rezervCam, cameraRezervField1);
        // Layout
        VBox h = new VBox(5, camera,
                new HBox(20,
                        new VBox(5,
                                new VBox(5,
                                        camera1Label, camera1Field),
                                new VBox(5,
                                        camera2Label, camera2Field)
                        ), v
                ));
        h.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(anchorSet(h
                ,
                0., 0., 0., 0.));
//        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

//    private TextField kppkirishshalgbaum = new TextField();
//    private TextField kppchiqishshalgbaumr = new TextField();
//
//    private Node showshlagbaumPopup() {
//        Stage popupStage = new Stage();
//        popupStage.initModality(Modality.APPLICATION_MODAL);
//
//        //KPP kirish shlagbaum
//        Label shlagbaum = new Label("KPP Shlagbaum");
//
//        Label shlagbaumLabel1 = new Label("KPP  kirish shlagbaum pin:");
//        kppkirishshalgbaum.setPrefWidth(80);
//
//        Label shlagbaumLabel2 = new Label("KPP  chiqish shlagbaum pin:");
//        kppkirishshalgbaum.setPrefWidth(80);
//
//
//        // Layout
//        VBox h = new VBox(5, shlagbaum, shlagbaumLabel1);
//        new HBox(20,
//                new VBox(5,
//                        new VBox(5,
//                                shlagbaumLabel1, kppkirishshalgbaum),
//                        new VBox(5,
//                                shlagbaumLabel2, kppchiqishshalgbaumr)
//                )
//        );
//        h.setAlignment(Pos.CENTER);
//        AnchorPane layout = new AnchorPane(anchorSet(h, 0., 0., 0., 0.));
////        layout.setSpacing(15);
//        layout.setPadding(new Insets(15));
//        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");
//
//        return layout;
//    }

    private TextField camera3Field = new TextField();
    private TextField camera4Field = new TextField();
    private TextField cameraRezervField2 = new TextField();


    private AnchorPane showCameraPopupOut() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Chiqish Kamerasi
        Label camera3Label = new Label("1-kamera kirish IP manzili:");
        Label camera = new Label("Camera");

        camera3Field.setPrefWidth(80);
        Label camera4Label = new Label("2-kamera yuk IP manzili:");

        camera4Field.setPrefWidth(80);
        Label rezervCam = new Label("Rezerv kamera:");
        cameraRezervField2.setPrefWidth(80);
        VBox v = new VBox(5, rezervCam, cameraRezervField2);
        // Layout
        VBox h = new VBox(5, camera,
                new HBox(20,
                        new VBox(5,
                                new VBox(5,
                                        camera3Label, camera3Field),
                                new VBox(5,
                                        camera4Label, camera4Field)
                        ), v
                ));
        h.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(anchorSet(
                h, 0., 0., 0., 0.));
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    private TextField sensorPinIn1 = new TextField();
    private TextField sensorPinIn2 = new TextField();
    private TextField sensorPinIn3 = new TextField();

    private AnchorPane sensorIn() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Chiqish Kamerasi
        Label sensor = new Label("Sensor");
        Label sensorL1 = new Label("1-sensor:");
        Label sensorL2 = new Label("2-sensor:");
        Label sensorL3 = new Label("3-sensor:");

        sensorPinIn1.setPrefWidth(40);
        sensorPinIn2.setPrefWidth(40);
        sensorPinIn3.setPrefWidth(40);

        Button testButton = new Button("Test");
        testButton.prefWidth(40);
        testButton.prefHeight(20);
        testButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 15;");
        testButton.setOnAction(event -> {
            sensorInTest();
        });

        HBox v = new HBox(20,
                new VBox(5, sensorL1, sensorPinIn1, testButton),
                new VBox(5, sensorL2, sensorPinIn2),
                new VBox(5, sensorL3, sensorPinIn3)
        );

        v.setAlignment(Pos.CENTER);
        VBox h = new VBox(5, sensor, v);
        h.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(anchorSet(
                h, 0., 0., 0., 0.));
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    private void sensorInTest() {
        RASP_SENSOR_1 = Integer.parseInt(sensorPinIn1.getText());
        RASP_SENSOR_2 = Integer.parseInt(sensorPinIn2.getText());
        RASP_SENSOR_3 = Integer.parseInt(sensorPinIn3.getText());
    }

    private TextField sensorPinOut1 = new TextField();
    private TextField sensorPinOut2 = new TextField();
    private TextField sensorPinOut3 = new TextField();

    private AnchorPane sensorOut() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Chiqish Kamerasi
        Label sensor = new Label("Sensor");
        Label sensorL1 = new Label("1-sensor:");
        Label sensorL2 = new Label("2-sensor:");
        Label sensorL3 = new Label("3-sensor:");

        sensorPinOut1.setPrefWidth(40);
        sensorPinOut2.setPrefWidth(40);
        sensorPinOut3.setPrefWidth(40);

        Button testButton = new Button("Test");
        testButton.prefWidth(40);
        testButton.prefHeight(20);
        testButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 15;");

        testButton.setOnAction(event -> {
            sensorOutTest();
        });

        HBox v = new HBox(20,
                new VBox(5, sensorL1, sensorPinOut1, testButton),
                new VBox(5, sensorL2, sensorPinOut2),
                new VBox(5, sensorL3, sensorPinOut3)
        );

        v.setAlignment(Pos.CENTER);
        VBox h = new VBox(5, sensor, v);
        h.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(anchorSet(
                h, 0., 0., 0., 0.));
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    private void sensorOutTest() {
        RASP_SENSOR_EXIT_1 = Integer.parseInt(sensorPinOut1.getText());
        RASP_SENSOR_EXIT_2 = Integer.parseInt(sensorPinOut2.getText());
        RASP_SENSOR_EXIT_3 = Integer.parseInt(sensorPinOut3.getText());
    }

    TextField controllerAddressField = new TextField();
    TextField portFieldController = new TextField();
    TextField timeoutField = new TextField();
    ToggleSwitch toggleSwitch = new ToggleSwitch();

    private Node showControllerPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Controller Sozlamalari");

        Label controllerAddressLabel = new Label("Controller addresi:");
        Label portLabel = new Label("Porti:");
        Label timeoutLabel = new Label("Time out (ms):");
        Label toggleSwitchLabel = new Label("Raspberrydan foylanilyapti:");

        VBox layout = new VBox(5,
                toggleSwitchLabel, toggleSwitch,
                controllerAddressLabel, controllerAddressField,
                portLabel, portFieldController,
                timeoutLabel, timeoutField);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    private TextField firstShlagbaumField = new TextField();
    private TextField secondShlagbaumField = new TextField();
    private TextField pinInIn = new TextField();
    private TextField pinInOut = new TextField();
    private TextField pinInIn2 = new TextField();
    private TextField pinInOut2 = new TextField();

    private Node showShlagbaumPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Form Elements
        Label firstShlagbaumLabel = new Label("1 yopilish vaqti (ms):");
        Label shlakbaum = new Label("Shlagbaum");
        firstShlagbaumField.setPrefWidth(80);

        Label secondShlagbaumLabel = new Label("2 yopilish vaqti (ms):");
        secondShlagbaumField.setPrefWidth(80);

        Label pinLabelIn = new Label("Pin In");
        Label pinLabelOut = new Label("Pin Out");
        Label pinLabelIn2 = new Label("Pin In");
        Label pinLabelOut2 = new Label("Pin Out");
        pinInIn.setPrefWidth(40);
        pinInIn2.setPrefWidth(40);
        pinInOut2.setPrefWidth(40);

        pinInOut.setPrefWidth(40);
        secondShlagbaumField.setPrefWidth(80);

        // Layout
        VBox v = new VBox(5, shlakbaum, new VBox(5,
                new HBox(20, new VBox(5, firstShlagbaumLabel, firstShlagbaumField), new VBox(5, pinLabelIn, pinInIn), new VBox(5, pinLabelOut, pinInOut)),
                new HBox(20, new VBox(5, secondShlagbaumLabel, secondShlagbaumField), new VBox(5, pinLabelIn2, pinInIn2), new VBox(5, pinLabelOut2, pinInOut2))
        ));
        v.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(
                anchorSet(v,
                        0., 0., 0., 0.));
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    private TextField firstShlagbaumFieldExit = new TextField();
    private TextField secondShlagbaumFieldExit = new TextField();
    private TextField pinOutIn = new TextField();
    private TextField pinOutOut = new TextField();
    private TextField pinOutIn2 = new TextField();
    private TextField pinOutOut2 = new TextField();

    private Node showShlagbaumPopupOut() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        Label firstShlagbaumLabelExit = new Label("1 yopilish vaqti (ms):");
        Label shlagbaum = new Label("Shlagbaum");
        firstShlagbaumFieldExit.setPrefWidth(80);
        Label secondShlagbaumLabelExit = new Label("2 yopilish vaqti (ms):");
        secondShlagbaumFieldExit.setPrefWidth(80);
        Label pinLabelIn = new Label("Pin In");
        Label pinLabelOut = new Label("Pin Out");
        Label pinLabelIn2 = new Label("Pin In");
        Label pinLabelOut2 = new Label("Pin Out");

        pinOutIn.setPrefWidth(40);
        pinOutOut.setPrefWidth(40);
        pinOutIn2.setPrefWidth(40);
        pinOutOut2.setPrefWidth(40);

        VBox v = new VBox(5, shlagbaum, new VBox(5,
                new HBox(20, new VBox(5, firstShlagbaumLabelExit, firstShlagbaumFieldExit), new VBox(5, pinLabelIn, pinOutIn), new VBox(5, pinLabelOut, pinOutOut)),
                new HBox(20, new VBox(5, secondShlagbaumLabelExit, secondShlagbaumFieldExit), new VBox(5, pinLabelIn2, pinOutIn2), new VBox(5, pinLabelOut2, pinOutOut2))
        ));
        v.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(
                anchorSet(v,
                        0., 0., 0., 0.));
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    private TextField kppkirishshalgbaum = new TextField();
    private TextField kpppinInIn = new TextField();
    private TextField kpppinInOut = new TextField();
    private TextField kpppinOutIn = new TextField();
    private TextField kpppinOutOut = new TextField();
    private TextField kppchiqishshalgbaum = new TextField();

    private Node showKPPINPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        Label kppfirstShlagbaumLabelExit = new Label("1 yopilish vaqti (ms):");
        Label shlagbaum = new Label("KPP Shlagbaum");
        kppkirishshalgbaum.setPrefWidth(80);
        Label pinLabelIn = new Label("Pin In");
        Label pinLabelOut = new Label("Pin Out");

        kpppinInIn.setPrefWidth(40);
        kpppinInOut.setPrefWidth(40);

        VBox v = new VBox(5, shlagbaum, new VBox(5,
                new HBox(20, new VBox(5, kppfirstShlagbaumLabelExit, kppkirishshalgbaum), new VBox(5, pinLabelIn, kpppinInIn), new VBox(5, pinLabelOut, kpppinInOut))
        ));
        v.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(
                anchorSet(v, 0., 0., 0., 0.)
        );
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    private Node showKPPOutPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        Label kppfirstShlagbaumLabelExit = new Label("1 yopilish vaqti (ms):");
        Label shlagbaum = new Label("KPP Shlagbaum");
        kppchiqishshalgbaum.setPrefWidth(80);
        Label pinLabelIn = new Label("Pin In");
        Label pinLabelOut = new Label("Pin Out");

        kpppinOutIn.setPrefWidth(40);
        kpppinOutOut.setPrefWidth(40);

        VBox v = new VBox(5, shlagbaum, new VBox(5,
                new HBox(20, new VBox(5, kppfirstShlagbaumLabelExit, kppchiqishshalgbaum), new VBox(5, pinLabelIn, kpppinOutIn), new VBox(5, pinLabelOut, kpppinOutOut))
        ));
        v.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(
                anchorSet(v, 0., 0., 0., 0.)
        );
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    private TextField massTimeField = new TextField();
    private TextField portField = new TextField();

    private Node showTaroziPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Form Elements
        Label massTimeLabel = new Label("Vazn aniqlash vaqti (ms):");
        Label tarozi = new Label("Tarozi");
        massTimeField.setPrefWidth(80);

        Label portLabel = new Label("Porti:");
        portField.setPrefWidth(80);

        // Layout
        VBox v = new VBox(5, tarozi, new HBox(20,
                new VBox(5,
                        massTimeLabel, massTimeField
                ),
                new VBox(5,
                        portLabel, portField)
        ));
        v.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(
                anchorSet(v, 0., 0., 0., 0.)
        );
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    private TextField massTimeFieldOut = new TextField();
    private TextField portFieldOut = new TextField();

    private Node showTaroziPopupOut() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Form Elements
        Label massTimeLabel = new Label("Vazn aniqlash vaqti (ms):");
        Label tarozi = new Label("Tarozi");

        massTimeFieldOut.setPrefWidth(80);
        Label portLabel = new Label("Porti:");

        portFieldOut.setPrefWidth(80);
        // Layout
        VBox v = new VBox(5, tarozi, new HBox(20,
                new VBox(5,
                        massTimeLabel, massTimeFieldOut
                ),
                new VBox(5,
                        portLabel, portFieldOut)
        ));
        v.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(
                anchorSet(v, 0., 0., 0., 0.)
        );
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }


    private void showExportData() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Export Scale ID");

        // Form Elements
        Label mycoalLabel = new Label("Mycoal tarozi id:");
        TextField mycoalField = new TextField(MYCOAL_SCALE_ID.toString());

        Label scaleWebLabel = new Label("Scale Web id:");
        TextField scaleWebField = new TextField(SCALE_WEB_ID.toString());

        // Buttons
        Button saveButton = new Button("Saqlash");
        Button cancelButton = new Button("Bekor qilish");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    mycoalField.getText().equals(SCALE_TIMEOUT.toString()) &&
                            scaleWebField.getText().equals(SCALE_PORT)
            );
        };

        mycoalField.textProperty().addListener(changeListener);
        scaleWebField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
            MYCOAL_SCALE_ID = Long.parseLong(mycoalField.getText());
            SCALE_WEB_ID = Long.parseLong(scaleWebField.getText());
            configurations.setMycoalScaleId(Long.parseLong(mycoalField.getText()));
            configurations.setScaleWebId(Long.parseLong(scaleWebField.getText()));
            configUtilsService.saveConfig(configurations);
            // Add save logic here
            popupStage.close();
        });

        cancelButton.setOnAction(event -> popupStage.close());

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(saveButton, cancelButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(5,
                mycoalLabel, mycoalField,
                scaleWebLabel, scaleWebField,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 450, 300);
        popupStage.setScene(popupScene);
        popupStage.show();
    }


    private void showExitTimePopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Chiqish vaqti");

        // Form Elements
        Label exitTimeLabel = new Label("Mashinaning qaytib chiqish vaqti (minut):");
        TextField exitTimeField = new TextField(EXIT_TIMEOUT.toString());

        // Buttons
        Button saveButton = new Button("Saqlash");
        Button cancelButton = new Button("Bekor qilish");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(exitTimeField.getText().equals(EXIT_TIMEOUT.toString())
            );
        };

        exitTimeField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
            EXIT_TIMEOUT = Integer.parseInt(exitTimeField.getText());
            configurations.setExitTimeout(Integer.parseInt(exitTimeField.getText()));
            configUtilsService.saveConfig(configurations);
            // Add save logic here
            popupStage.close();
        });

        cancelButton.setOnAction(event -> popupStage.close());

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(saveButton, cancelButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(5,
                exitTimeLabel, exitTimeField,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 350, 200);
        popupStage.setScene(popupScene);
        popupStage.show();
    }

    TextField printerNameField = new TextField();

    private Node showPrinterPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Printer Sozlamalari");

        Label printerNameLabel = new Label("Printer nomi:");

        Button testButton = new Button("Test");

        testButton.setOnAction(event -> {
            printCheck.testCheckRecipient();
        });


        testButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(testButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Layout
        VBox layout = new VBox(5,
                printerNameLabel, printerNameField,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    public void aboutMenuSelected() {
        Stage aboutStage = new Stage();
        aboutStage.initModality(Modality.APPLICATION_MODAL);
        aboutStage.setTitle("About");

        // Company information
        String companyInfo = """
                Ishlab chiqaruvchi: Tenzor Soft MCHJ
                Manzil: Toshkent sh. Yashnaobod t. Maxtumquli 87-uy
                Telefon: 95 460 10 10
                Veb-sayt: www.tenzorsoft.com
                Email: info@tenzorsoft.com
                """;

        Label infoLabel = new Label(companyInfo);
        infoLabel.setWrapText(true);

        // Close button
        Button closeButton = new Button("Yopish");
        closeButton.setOnAction(event -> aboutStage.close());
        closeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");

        // Layout
        VBox layout = new VBox(5, infoLabel, closeButton);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 200);
        aboutStage.setScene(scene);
        aboutStage.show();
    }


    public void showInstructionPopup() {
        Stage instructionStage = new Stage();
        instructionStage.initModality(Modality.APPLICATION_MODAL);
        instructionStage.setTitle("Yo'riqnoma");

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #f9f9f9;");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Introductory text
        Label introLabel = new Label(
                "Assalomu alaykum. Platformaga xush kelibsiz.\nBizni tanlab adashmaganingiz aniq!\n\n" +
                        "Bu yerda platformani ishlatish bo’yicha batafsil “Yo’riqnoma” bilan tanishib chiqishingiz mumkin."
        );
        introLabel.setWrapText(true);
        introLabel.setStyle("-fx-font-size: 16px;"); // Bigger font size

        // Box with label and download button
        HBox downloadBox = new HBox(10);
        downloadBox.setPadding(new Insets(10));
        downloadBox.setStyle("-fx-background-color: #E8EAF6; -fx-border-color: #3F51B5; -fx-border-radius: 5; -fx-border-width: 1;");
        downloadBox.setAlignment(Pos.CENTER);

        Label documentLabel = new Label("Yo'riqnomani dokumentatsiyasini yuklab olishingiz mumkin");
        documentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3F51B5;");

        Button downloadButton = new Button("Yuklab olish");
        downloadButton.setOnAction(event -> downloadInstructionFile());
        downloadButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-padding: 5 15;");
        downloadButton.setCursor(Cursor.HAND); // Change cursor to hand on hover

        downloadBox.getChildren().addAll(documentLabel, downloadButton);

        mainLayout.getChildren().addAll(introLabel, new Separator(), downloadBox, new Separator());

        // Add sections with clickable images and separators
        mainLayout.getChildren().addAll(
                createSectionWithSeparator(
                        "Platformaga registratsiya qilib kirganingizda sizda mana shunday ekran chiqadi.",
                        "/images/main-menu.png"
                ),
                createSectionWithSeparator(
                        "Platformani ishlatish uchun oldin Controllerga ulangan bo’lishi kerak. 1-kirganingizda platforma Controllerga ulanmagan bo’ladi. Buni ulash uchun oldin bitta tovar qo’shishingiz kerak bo’ladi. Tovar qo’shilmaguncha Controllerga ulanib bo’lmaydi. Tovar qo’shish uchun o’ng taraf tepada, sensorlar ustidagi qutichani bosishingiz kerak bo’ladi.",
                        "/images/controller.png"
                ),
                createSectionWithSeparator(
                        "Mahsulot qo’shishni bosganda shunaqa oyna chiqadi. Shu yerda siz qaysi tovarni tarozidan olib-chiqib ketmoqchi bo’lsangiz o’sha tovarni qo’shishingiz kerak.",
                        "/images/mahsulot.png"
                ),
                createSectionWithSeparator(
                        "Masalan, aytaylik, “Komir” degan tovar qo’shmoqchimiz. Bo’sh yozadigan joyga “Komir” deb yozib turib, OK tugmasini bosamiz.",
                        "/images/mahsulot-qoshish.png"
                ),
                createSectionWithSeparator(
                        "Tovar qo’shilganligi haqida sizga ma’lumot beriladi va endi siz Controllerga ulansangiz bo’ladi. Buning uchun Connect tugmasini bosishingiz kifoya.",
                        "/images/mahsulot-qoshildi.png"
                ),
                createSectionWithSeparator(
                        "Controllerga ulangandan keyin hamma narsa ishlashni boshlaydi. Endi bemalol mashinalar kirib-chiqsa bo’ladi.",
                        "/images/controller-connected.png"
                )
        );

        // Group image rows
        mainLayout.getChildren().addAll(
                createRowWithImages(
                        new String[]{"/images/moshina-kirish1.png", "/images/moshina-kirish2.png"},
                        "Avtomobil kirganda jadvalga yozib qo’yadi. Moshina raqami, kirgan kuni, oyi, yili..."
                ),
                createRowWithImages(
                        new String[]{"/images/moshina-rasm1.png", "/images/moshina-rasm2.png", "/images/moshina-rasm3.png"},
                        "Avtomobil kirganda uni rasmlarini ham platformada ko’rishingiz mumkin..."
                ),
                createRowWithImages(
                        new String[]{"/images/moshina-chiqish1.png", "/images/moshina-chiqish2.png"},
                        "Berilgan vaqt o’tganidan keyin moshina chiqib ketishi mumkin bo’ladi..."
                ),
                createRowWithImages(
                        new String[]{"/images/shlagbaum-buttons.png", "/images/avto-nomer-tanlash.png"},
                        "Bir moshina chiqib ketmoqchi bo’lsa va 2-shlagbaumni qo’lda ochib chiqarib yubormoqchi..."
                )
        );

        mainLayout.getChildren().addAll(
                createSectionWithSeparator(
                        "Avtomabilni o’zingiz ham 1-shlagbaumni qo’lda ochgan holatda kirgizishingiz mumkin! Buning uchun, o’ng tarafdagi o’rtada turgan tugmalardan, 1-shlagbaumni ochish tugmasini bosishingiz kerak bo’ladi. 1-shlagbaum ochish tugmasi bosilsa, kirgizmoqchi bo’lgan avtomabil raqamini so’raydi.",
                        "/images/shlagbaum-buttons.png"
                ),
                createSectionWithSeparator(
                        "Siz bu yerdan moshina raqamini to’gri va aniq kiritishingiz kerak bo’ladi. Viloyat raqami, 3 ta nomer va 3 ta harfni hammasini to’liq yozishingiz kerak bo’ladi. Masalan, 20C587DA. Shundan keyin 1-shlagbaum ochiladi va moshina taroziga chiqishi mumkin bo’ladi.",
                        "/images/enter-avto-number.png"
                ),
                createSectionWithSeparator(
                        "Huddi shu moshina qaytib chiqib ketyotgan paytda xatolik berishi mumkin. Moshina kirgandan keyin, 5 daqiqadan so’ng chiqib ketishi mumkin. Kirgan zahoti 2-3 minut Ichida qayta chiqib keta olmaydi. 5 daqiqa kutish kerakligini va qachon kirishi mumkinligi haqida ekranda ma’lumot beriladi (kirish vaqti – 12:33 || chiqish vaqti – 12:38:47).",
                        "/images/enterance-after5.png"
                )
        );

        mainLayout.getChildren().addAll(
                createSectionWithSeparator(
                        "Moshina kirib chiqib ketganidan keyin hamma rasmlar ko’rinadi. Har birini ustidan bossangiz, rasmni kattalashtirib ko’rsatadi.",
                        "/images/moshina-rasm-hammasi.png"
                )
        );

        mainLayout.getChildren().addAll(
                createSectionWithSeparator(
                        "Programmaning bu funksiyasi – moshina chiqib ketayotgan paytda massani avtomatik tarzda olib jadvalga yozib qo’yadi",
                        "/images/massani-avtomatik.jpg"
                ),
                createSectionWithSeparator(
                        "Agar bu o’chirib qo’yilsa, moshina chiqib ketayotgan payt operatordan massani tasdiqlashini so’raydi. Agar u tasdiqlasa, 1-shlagbaum ochilib, moshina chiqib ketishi mumkin.",
                        "/images/massani-avtomatik-off.png"
                ),
                createSectionWithSeparator(
                        "Tasdiqlash – olib chiqib ketilayotgan yukni massasini tasdiqlaydi va 1-shlagbaumni ochadi.\n" +
                                "Bekor qilish – olib chiqib ketilayotgan yuk massasi qabul qilinmaydi va orqadagi 2-shlagbaum qayta ochiladi va moshina orqadan chiqib ketishi kerak. \n" +
                                "Qayta o’lchash – yukni qayta o’lchaydi. Agar, olib chiqib ketilayotgan yuk massasi ko’proq yoki kamroq bo’lganda, bu funksiya ishlatiladi. Agar yuk keragidan ortiq ko’p bo’lsa, shu tarozini ustida, moshina hech qayerga yurmagan holatda, ortiqcha yuk to’kiladi. Agar yuk massasi kam bo’lsa, qo’shimcha yuk yuklanadi. Yukni qayta o’lchash tugmasi bosilganda, yangi yuk massasi ekranga chiqadi va tasdiqlansa 1-shlagbaum ochiladi va moshina chiqib ketishi mumkin bo’ladi.\n",
                        "/images/massani-tasdiqlash.png"
                )
        );

        // Wrap the content in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);

        // Close button
        Button closeButton = new Button("Yopish");
        closeButton.setOnAction(event -> instructionStage.close());
        closeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 20;");
        closeButton.setCursor(Cursor.HAND); // Change cursor to hand on hover

        VBox rootLayout = new VBox(scrollPane, closeButton);
        rootLayout.setSpacing(10);
        rootLayout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(rootLayout, 600, 700);
        instructionStage.setScene(scene);
        instructionStage.show();
    }

    private VBox createRowWithImages(String[] imagePaths, String description) {
        VBox rowContainer = new VBox(5);
        rowContainer.setAlignment(Pos.CENTER);

        HBox imageRow = new HBox(10);
        imageRow.setAlignment(Pos.CENTER);

        for (String path : imagePaths) {
            imageRow.getChildren().add(createClickableImage(path, 150, 150));
        }

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 16px;");

        rowContainer.getChildren().addAll(imageRow, descriptionLabel);
        return rowContainer;
    }

    private VBox createSectionWithSeparator(String description, String imagePath) {
        ImageView imageView = createClickableImage(imagePath, 300, 200); // Set uniform size
        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 16px;"); // Bigger font size
        descriptionLabel.setCursor(Cursor.TEXT);
        VBox sectionLayout = new VBox(5, imageView, descriptionLabel, new Separator());
        sectionLayout.setAlignment(Pos.CENTER);
        return sectionLayout;
    }

    private ImageView createClickableImage(String imagePath, double width, double height) {
        ImageView imageView = new ImageView(new Image(imagePath));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false); // Ensure uniform size
        imageView.setCursor(javafx.scene.Cursor.HAND); // Change cursor to hand on hover

        imageView.setOnMouseClicked(event -> openImageInPopup(imagePath));
        return imageView;
    }

    private void openImageInPopup(String imagePath) {
        Stage imagePopupStage = new Stage();
        imagePopupStage.initModality(Modality.APPLICATION_MODAL);
        imagePopupStage.setTitle("Tasvirni ko'rish");

        ImageView imageView = new ImageView(new Image(imagePath));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800); // Set a large size for preview
        imageView.setFitHeight(600);

        VBox layout = new VBox(imageView);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #ffffff;");

        Scene scene = new Scene(layout, 820, 620);
        imagePopupStage.setScene(scene);
        imagePopupStage.show();
    }


    private void downloadInstructionFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Yo'riqnoma faylini saqlang");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word documents", "*.docx"));

        // Default fayl nomini o'rnatish
        fileChooser.setInitialFileName("Yoriqnoma.docx");

        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try (InputStream inputStream = getClass().getResourceAsStream("/files/Yoriqnoma.docx")) {
                if (inputStream != null) {
                    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Fayl yuklab olindi!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Fayl topilmadi!");
                    alert.showAndWait();
                }
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Fayl saqlanmadi: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }


    @FXML
    public void OpenTab() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Tarozi Sozlamalari");

        // Form Elements
        Label massTimeLabel = new Label("Kirish Tarozi massasini aniqlash vaqti (ms):");
        TextField massTimeField = new TextField(SCALE_TIMEOUT.toString());

        Label portLabel = new Label("Porti:");
        TextField portField = new TextField(SCALE_PORT);

        Line line = new Line(50, 50, 465, 50);
        line.setStroke(Color.BLUE); // chiziq rangi
        line.setStrokeWidth(2);

        Label massTimeLabelExit = new Label("Chiqish Tarozi massasini aniqlash vaqti (ms):");
        TextField massTimeFieldExit = new TextField(SCALE_TIMEOUT.toString());

        Label portLabelExit = new Label("Porti:");
        TextField portFieldExit = new TextField(SCALE_PORT);

        // Buttons
        Button saveButton = new Button("Saqlash");
        Button cancelButton = new Button("Bekor qilish");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    massTimeField.getText().equals(SCALE_TIMEOUT.toString()) &&
                            portField.getText().equals(SCALE_PORT) &&
                            massTimeFieldExit.getText().equals(EXIT_TIMEOUT.toString()) &&
                            portFieldExit.getText().equals(SCALE_EXIT_PORT)
            );
        };

        massTimeField.textProperty().addListener(changeListener);
        portField.textProperty().addListener(changeListener);

        massTimeFieldExit.textProperty().addListener(changeListener);
        portFieldExit.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
            SCALE_TIMEOUT = Integer.parseInt(massTimeField.getText());
            SCALE_PORT = portField.getText();
            configurations.setScaleTimeout(Integer.parseInt(massTimeField.getText()));
            configurations.setScalePort(portField.getText());

            EXIT_TIMEOUT = Integer.parseInt(massTimeFieldExit.getText());
            SCALE_EXIT_PORT = portFieldExit.getText();
            configurations.setExitTimeout(Integer.parseInt(massTimeFieldExit.getText()));
            configurations.setScaleExitPort(portFieldExit.getText());
            configUtilsService.saveConfig(configurations);
            // Add save logic here
            popupStage.close();
        });

        cancelButton.setOnAction(event -> popupStage.close());

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(saveButton, cancelButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(5,
                massTimeLabel, massTimeField,
                portLabel, portField,
                line,
                massTimeLabelExit, massTimeFieldExit,
                portLabelExit, portFieldExit,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 450, 370);
        popupStage.setScene(popupScene);
        popupStage.show();
    }
}
