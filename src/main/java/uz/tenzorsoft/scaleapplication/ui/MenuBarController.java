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
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.repository.TruckActionRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;
import uz.tenzorsoft.scaleapplication.service.ConfigUtilsService;
import uz.tenzorsoft.scaleapplication.service.ExcelService;
import uz.tenzorsoft.scaleapplication.service.PrintCheck;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static uz.tenzorsoft.scaleapplication.domain.Instances.configurations;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isRaspberryUsing;
import static uz.tenzorsoft.scaleapplication.domain.Settings.*;

@Component
@RequiredArgsConstructor
public class MenuBarController implements BaseController {

    private final ConfigUtilsService configUtilsService;
    private final PrintCheck printCheck;
    private final TruckRepository truckRepository;
    private final TruckActionRepository truckActionRepository;
    private final ControlPane controlPane;
    @Autowired
    private TableController tableController;

    private Node incoms() {
        AnchorPane camera = showCameraPopup();
        Node node = showShlagbaumPopup();
        Node node1 = showTaroziPopup();
        return new AnchorPane(new VBox(5, camera, node, node1));
    }

    private Node outs() {
        AnchorPane camera = showCameraPopupOut();
        Node node = showShlagbaumPopupOut();
        Node node1 = showTaroziPopupOut();
        return new AnchorPane(new VBox(5, camera, node, node1));
    }

    @FXML
    private void settingsTabs() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Sozlamalar");

        TabPane tabPane = new TabPane();

        Button saveButton = new Button("Saqlash");
        Button cancelButton = new Button("Bekor qilish");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        Tab inCamera = new Tab("Kirish");
//        showCameraPopup();
        inCamera.setContent(incoms());
        inCamera.setClosable(false);

        Tab outCamera = new Tab("Chiqish");
        outCamera.setContent(outs());
        outCamera.setClosable(false);

//        Tab inGate = new Tab("Kirish shlagbaum");
//        inGate.setContent(showShlagbaumPopup());
//        inGate.setClosable(false);
//
//        Tab outGate = new Tab("Chiqish shlagbaum");
//        outGate.setContent(showShlagbaumPopupOut());
//        outGate.setClosable(false);
//
//        Tab inScale = new Tab("Kiruvchi tarozi");
//        inScale.setContent(showTaroziPopup());
//        inScale.setClosable(false);
//
//        Tab outScale = new Tab("Chiquvchi tarozi");
        Tab printer = new Tab("Printer");
        printer.setContent(showPrinterPopup());
        printer.setClosable(false);
        Tab controller = new Tab("Controller");
        controller.setContent(showControllerPopup());
        controller.setClosable(false);

        tabPane.getTabs().add(inCamera);
        tabPane.getTabs().add(outCamera);
//        tabPane.getTabs().add(inGate);
//        tabPane.getTabs().add(outGate);
//        tabPane.getTabs().add(inScale);
//        tabPane.getTabs().add(outScale);
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
    private void onDatabaseMenuSelected() {
        showDatabasePopup();
    }

    @FXML
    private void onCameraMenuSelected() {
        showCameraPopup();
    }

    @FXML
    private void onControllerMenuSelected() {
        showControllerPopup();
    }

    @FXML
    private void onShlagbaumMenuSelected() {
        showShlagbaumPopup();
    }

    @FXML
    private void onTaroziMenuSelected() {
        showTaroziPopup();
    }

    @FXML
    private void onExportDataMenuSelected() {
        showExportData();
    }

    @FXML
    private void onExitTimeMenuSelected() {
        showExitTimePopup();
    }

    @FXML
    private void onPrinterMenuSelected() {
        showPrinterPopup();
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
        VBox layout = new VBox(10, nameLabel, nameField, loginLabel, loginField, passwordLabel, passwordBox, buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 350, 300);
        popupStage.setScene(popupScene);
        popupStage.setResizable(false);
        popupStage.show();
    }


    private AnchorPane showCameraPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Kirish Kamerasi
        Label camera = new Label("Camera");

        Label camera1Label = new Label("1-kamera kirish IP manzili:");
        TextField camera1Field = new TextField(CAMERA_1);
        camera1Field.setPrefWidth(80);

        Label rezervCam = new Label("Rezerv kamera:");
        TextField cameraRezervField = new TextField();
        cameraRezervField.setPrefWidth(80);

        // Yuk Kamerasi
        Label camera2Label = new Label("2-kamera yuk IP manzili:");
        TextField camera2Field = new TextField(CAMERA_2);
        camera2Field.setPrefWidth(80);

//        saveButton.setDisable(true); // Disabled by default
//
//        // Enable Save button on any change in text fields
//        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
//            saveButton.setDisable(
//                    camera1Field.getText().equals(CAMERA_1) &&
//                            camera2Field.getText().equals(CAMERA_2)
//            );
//        };

//        camera1Field.textProperty().addListener(changeListener);
//        camera2Field.textProperty().addListener(changeListener);

//        saveButton.setOnAction(event -> {
//            CAMERA_1 = camera1Field.getText();
//            CAMERA_2 = camera2Field.getText();
//            configurations.setCamera1(camera1Field.getText());
//            configurations.setCamera2(camera2Field.getText());
//            configUtilsService.saveConfig(configurations);
//            // Add save logic for all three cameras here
//            popupStage.close();
//        });

//        cancelButton.setOnAction(event -> popupStage.close());
//
//        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
//        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");
//
//        HBox buttonBox = new HBox(saveButton, cancelButton);
//        buttonBox.setSpacing(10);
//        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox v = new VBox(10, rezervCam, cameraRezervField);
        // Layout
        VBox h = new VBox(10, camera,
                new HBox(20,
                        new VBox(10,
                                new VBox(10,
                                        camera1Label, camera1Field),
                                new VBox(10,
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

    private AnchorPane showCameraPopupOut() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Chiqish Kamerasi
        Label camera3Label = new Label("1-kamera kirish IP manzili:");
        Label camera = new Label("Camera");

        TextField camera3Field = new TextField(CAMERA_3);
        camera3Field.setPrefWidth(80);

        Label camera4Label = new Label("2-kamera yuk IP manzili:");
        TextField camera4Field = new TextField(CAMERA_4);
        camera4Field.setPrefWidth(80);

        Label rezervCam = new Label("Rezerv kamera:");
        TextField cameraRezervField = new TextField();
        cameraRezervField.setPrefWidth(80);

        // Buttons
//        Button saveButton = new Button("Saqlash");
//        Button cancelButton = new Button("Bekor qilish");

//        saveButton.setDisable(true); // Disabled by default
//
//        // Enable Save button on any change in text fields
//        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
//            saveButton.setDisable(
//                    camera3Field.getText().equals(CAMERA_3) &&
//                            camera4Field.getText().equals(CAMERA_4)
//            );
//        };

//        camera3Field.textProperty().addListener(changeListener);
//        camera4Field.textProperty().addListener(changeListener);

//        saveButton.setOnAction(event -> {
//            CAMERA_3 = camera3Field.getText();
//            CAMERA_4 = camera4Field.getText();
//            configurations.setCamera3(camera3Field.getText());
//            configurations.setCamera4(camera4Field.getText());
//            configUtilsService.saveConfig(configurations);
//            // Add save logic for all three cameras here
//            popupStage.close();
//        });
//
//        cancelButton.setOnAction(event -> popupStage.close());
//
//        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
//        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

//        HBox buttonBox = new HBox(saveButton, cancelButton);
//        buttonBox.setSpacing(10);
//        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox v = new VBox(10, rezervCam, cameraRezervField);
        // Layout
        VBox h = new VBox(10, camera,
                new HBox(20,
                        new VBox(10,
                                new VBox(10,
                                        camera3Label, camera3Field),
                                new VBox(10,
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


/*
    private void showControllerPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Controller Sozlamalari");

        // Form Elements
        Label controllerAddressLabel = new Label("Controller addresi:");
        TextField controllerAddressField = new TextField(CONTROLLER_IP);

        Label portLabel = new Label("Porti:");
        TextField portField = new TextField(CONTROLLER_PORT.toString());

        Label timeoutLabel = new Label("Time out (ms):");
        TextField timeoutField = new TextField(CONTROLLER_CONNECT_TIMEOUT.toString());

        // Buttons
        Button saveButton = new Button("Saqlash");
        Button cancelButton = new Button("Bekor qilish");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    controllerAddressField.getText().equals(CONTROLLER_IP) &&
                            portField.getText().equals(CONTROLLER_PORT.toString()) &&
                            timeoutField.getText().equals("" + CONTROLLER_CONNECT_TIMEOUT)
            );
        };


        controllerAddressField.textProperty().addListener(changeListener);
        portField.textProperty().addListener(changeListener);
        timeoutField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
            CONTROLLER_IP = controllerAddressField.getText();
            CONTROLLER_PORT = Integer.parseInt(portField.getText());
            CONTROLLER_CONNECT_TIMEOUT = Integer.parseInt(timeoutField.getText());
            configurations.setControllerIp(controllerAddressField.getText());
            configurations.setControllerPort(Integer.parseInt(portField.getText()));
            configurations.setControllerConnectTimeout(Integer.parseInt(timeoutField.getText()));
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
        VBox layout = new VBox(10,
                controllerAddressLabel, controllerAddressField,
                portLabel, portField,
                timeoutLabel, timeoutField,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 400, 300);
        popupStage.setScene(popupScene);
        popupStage.show();
    }
*/

    private Node showControllerPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Controller Sozlamalari");

        Label controllerAddressLabel = new Label("Controller addresi:");
        TextField controllerAddressField = new TextField(CONTROLLER_IP);

        Label portLabel = new Label("Porti:");
        TextField portField = new TextField(CONTROLLER_PORT.toString());

        Label timeoutLabel = new Label("Time out (ms):");
        TextField timeoutField = new TextField(CONTROLLER_CONNECT_TIMEOUT.toString());

        Label toggleSwitchLabel = new Label("Raspberrydan foylanilyapti:");
        ToggleSwitch toggleSwitch = new ToggleSwitch();

        // Buttons
//        Button saveButton = new Button("Saqlash");
//        Button cancelButton = new Button("Bekor qilish");

//        saveButton.setDisable(true); // Disabled by default
//
//        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
//            saveButton.setDisable(
//                    toggleSwitch.isSelected() &&
//                            controllerAddressField.getText().equals(CONTROLLER_IP) &&
//                            portField.getText().equals(CONTROLLER_PORT.toString()) &&
//                            timeoutField.getText().equals("" + CONTROLLER_CONNECT_TIMEOUT)
//            );
//        };

//        controllerAddressField.textProperty().addListener(changeListener);
//        portField.textProperty().addListener(changeListener);
//        timeoutField.textProperty().addListener(changeListener);
//
//        if (IS_RASPBERRY_USING) {
//            toggleSwitch.setSelected(true);
//            controllerAddressField.setDisable(true);
//            portField.setDisable(true);
//            timeoutField.setDisable(true);
//        }

//        toggleSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
//            boolean isControllerUsed = newValue;
//            controllerAddressField.setDisable(isControllerUsed);
//            portField.setDisable(isControllerUsed);
//            timeoutField.setDisable(isControllerUsed);
//            saveButton.setDisable(false);
//        });

//        saveButton.setOnAction(event -> {
//            if (!toggleSwitch.isSelected()) {
//                CONTROLLER_IP = controllerAddressField.getText();
//                CONTROLLER_PORT = Integer.parseInt(portField.getText());
//                CONTROLLER_CONNECT_TIMEOUT = Integer.parseInt(timeoutField.getText());
//                configurations.setControllerIp(controllerAddressField.getText());
//                configurations.setControllerPort(Integer.parseInt(portField.getText()));
//                configurations.setControllerConnectTimeout(Integer.parseInt(timeoutField.getText()));
//            } else {
//                isRaspberryUsing = true;
//                IS_RASPBERRY_USING = true;
//            }
//            configurations.setRaspberryUsing(toggleSwitch.isSelected() ? 1 : 0);
//            configUtilsService.saveConfig(configurations);
//            popupStage.close();
//        });
//
//        cancelButton.setOnAction(event -> popupStage.close());
//
//        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
//        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

//        HBox buttonBox = new HBox(saveButton, cancelButton);
//        buttonBox.setSpacing(10);
//        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(10,
                toggleSwitchLabel, toggleSwitch,
                controllerAddressLabel, controllerAddressField,
                portLabel, portField,
                timeoutLabel, timeoutField);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

//        Scene popupScene = new Scene(layout, 400, 350); // Adjusted height for the additional ToggleSwitch
//        popupStage.setScene(popupScene);
//        popupStage.show();
        return layout;
    }


    private Node showShlagbaumPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Form Elements
        Label firstShlagbaumLabel = new Label("1-shlagbaum yopilish vaqti (ms):");
        Label shlakbaum = new Label("Shlagbaum");
        TextField firstShlagbaumField = new TextField("" + CLOSE_GATE1_TIMEOUT);
        firstShlagbaumField.setPrefWidth(80);

        Label secondShlagbaumLabel = new Label("2-shlagbaum yopilish vaqti (ms):");
        TextField secondShlagbaumField = new TextField("" + CLOSE_GATE2_TIMEOUT);
        secondShlagbaumField.setPrefWidth(80);

        Label pinLabel = new Label("Pin");
        TextField pin = new TextField();
        pin.setPrefWidth(40);

        Label pinLabel2 = new Label("Pin");
        TextField pin2 = new TextField();
        pin2.setPrefWidth(40);

        secondShlagbaumField.setPrefWidth(80);

//        Label firstShlagbaumLabelExit = new Label("1-shlagbaum yopilish vaqti (ms):");
//        TextField firstShlagbaumFieldExit = new TextField("" + CLOSE_GATE1_EXIT_TIMEOUT);
//
//        Label secondShlagbaumLabelExit = new Label("2-shlagbaum yopilish vaqti (ms):");
//        TextField secondShlagbaumFieldExit = new TextField("" + CLOSE_GATE2_EXIT_TIMEOUT);

        // Buttons
//        Button saveButton = new Button("Saqlash");
//        Button cancelButton = new Button("Bekor qilish");
//
//        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
//        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
//            saveButton.setDisable(
//                    firstShlagbaumField.getText().equals("" + CLOSE_GATE1_TIMEOUT) &&
//                            secondShlagbaumField.getText().equals("" + CLOSE_GATE2_TIMEOUT)
////                            firstShlagbaumFieldExit.getText().equals("" + CLOSE_GATE1_EXIT_TIMEOUT) &&
////                            secondShlagbaumFieldExit.getText().equals("" + CLOSE_GATE2_EXIT_TIMEOUT)
//            );
//        };

//        firstShlagbaumField.textProperty().addListener(changeListener);
//        secondShlagbaumField.textProperty().addListener(changeListener);

//        firstShlagbaumFieldExit.textProperty().addListener(changeListener);
//        secondShlagbaumFieldExit.textProperty().addListener(changeListener);

//        saveButton.setOnAction(event -> {
//            CLOSE_GATE1_TIMEOUT = Integer.parseInt(firstShlagbaumField.getText());
//            CLOSE_GATE2_TIMEOUT = Integer.parseInt(secondShlagbaumField.getText());
//            configurations.setCloseGate1Timeout(Integer.parseInt(firstShlagbaumField.getText()));
//            configurations.setCloseGate2Timeout(Integer.parseInt(secondShlagbaumField.getText()));
//
////            CLOSE_GATE1_EXIT_TIMEOUT = Integer.parseInt(firstShlagbaumFieldExit.getText());
////            CLOSE_GATE2_EXIT_TIMEOUT = Integer.parseInt(secondShlagbaumFieldExit.getText());
////            configurations.setCloseGateExit1Timeout(Integer.parseInt(firstShlagbaumFieldExit.getText()));
////            configurations.setCloseGateExit2Timeout(Integer.parseInt(secondShlagbaumFieldExit.getText()));
//            configUtilsService.saveConfig(configurations);
//            // Add save logic here
//            popupStage.close();
//        });

//        cancelButton.setOnAction(event -> popupStage.close());
//
//        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
//        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");
//
//        HBox buttonBox = new HBox(saveButton, cancelButton);
//        buttonBox.setSpacing(10);
//        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox v = new VBox(10, shlakbaum, new VBox(10,
                new HBox(20, new VBox(firstShlagbaumLabel, firstShlagbaumField), new VBox(pinLabel, pin)),
                new HBox(20, new VBox(secondShlagbaumLabel, secondShlagbaumField), new VBox(pinLabel2, pin2))
        ));
        v.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(
                anchorSet(v,
                        0., 0., 0., 0.));
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }

    private Node showShlagbaumPopupOut() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Form Elements
//        Label firstShlagbaumLabel = new Label("1-shlagbaum yopilish vaqti (ms):");
//        TextField firstShlagbaumField = new TextField("" + CLOSE_GATE1_TIMEOUT);
//
//        Label secondShlagbaumLabel = new Label("2-shlagbaum yopilish vaqti (ms):");
//        TextField secondShlagbaumField = new TextField("" + CLOSE_GATE2_TIMEOUT);

        Label firstShlagbaumLabelExit = new Label("1-shlagbaum yopilish vaqti (ms):");
        Label shlagbaum = new Label("Shlagbaum");
        TextField firstShlagbaumFieldExit = new TextField("" + CLOSE_GATE1_EXIT_TIMEOUT);
        firstShlagbaumFieldExit.setPrefWidth(80);

        Label secondShlagbaumLabelExit = new Label("2-shlagbaum yopilish vaqti (ms):");
        TextField secondShlagbaumFieldExit = new TextField("" + CLOSE_GATE2_EXIT_TIMEOUT);
        secondShlagbaumFieldExit.setPrefWidth(80);

        Label pinLabel = new Label("Pin");
        TextField pin = new TextField();
        pin.setPrefWidth(40);

        Label pinLabel2 = new Label("Pin");
        TextField pin2 = new TextField();
        pin2.setPrefWidth(40);

        // Buttons
//        Button saveButton = new Button("Saqlash");
//        Button cancelButton = new Button("Bekor qilish");
//
//        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
//        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
//            saveButton.setDisable(
//                    firstShlagbaumField.getText().equals("" + CLOSE_GATE1_TIMEOUT) &&
//                            secondShlagbaumField.getText().equals("" + CLOSE_GATE2_TIMEOUT)
////                            firstShlagbaumFieldExit.getText().equals("" + CLOSE_GATE1_EXIT_TIMEOUT) &&
////                            secondShlagbaumFieldExit.getText().equals("" + CLOSE_GATE2_EXIT_TIMEOUT)
//            );
//        };

//        firstShlagbaumField.textProperty().addListener(changeListener);
//        secondShlagbaumField.textProperty().addListener(changeListener);

//        firstShlagbaumFieldExit.textProperty().addListener(changeListener);
//        secondShlagbaumFieldExit.textProperty().addListener(changeListener);

//        saveButton.setOnAction(event -> {
//            CLOSE_GATE1_TIMEOUT = Integer.parseInt(firstShlagbaumField.getText());
//            CLOSE_GATE2_TIMEOUT = Integer.parseInt(secondShlagbaumField.getText());
//            configurations.setCloseGate1Timeout(Integer.parseInt(firstShlagbaumField.getText()));
//            configurations.setCloseGate2Timeout(Integer.parseInt(secondShlagbaumField.getText()));
//
////            CLOSE_GATE1_EXIT_TIMEOUT = Integer.parseInt(firstShlagbaumFieldExit.getText());
////            CLOSE_GATE2_EXIT_TIMEOUT = Integer.parseInt(secondShlagbaumFieldExit.getText());
////            configurations.setCloseGateExit1Timeout(Integer.parseInt(firstShlagbaumFieldExit.getText()));
////            configurations.setCloseGateExit2Timeout(Integer.parseInt(secondShlagbaumFieldExit.getText()));
//            configUtilsService.saveConfig(configurations);
//            // Add save logic here
//            popupStage.close();
//        });

//        cancelButton.setOnAction(event -> popupStage.close());
//
//        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
//        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");
//
//        HBox buttonBox = new HBox(saveButton, cancelButton);
//        buttonBox.setSpacing(10);
//        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout

        VBox v = new VBox(10, shlagbaum, new VBox(10,
                new HBox(20, new VBox(firstShlagbaumLabelExit, firstShlagbaumFieldExit), new VBox(pinLabel, pin)),
                new HBox(20, new VBox(secondShlagbaumLabelExit, secondShlagbaumFieldExit), new VBox(pinLabel2, pin2))
        ));
        v.setAlignment(Pos.CENTER);
        AnchorPane layout = new AnchorPane(
                anchorSet(v,
                        0., 0., 0., 0.));
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return layout;
    }


    private Node showTaroziPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
//        popupStage.setTitle("Tarozi Sozlamalari");

        // Form Elements
//        Label exitTimeLabel = new Label("Mashinaning qaytib chiqish vaqti (ms):");
//        TextField exitTimeField = new TextField(EXIT_TIMEOUT.toString());

        // Form Elements
        Label massTimeLabel = new Label("Vazn aniqlash vaqti (ms):");
        Label tarozi = new Label("Tarozi");
        TextField massTimeField = new TextField(SCALE_TIMEOUT.toString());
        massTimeField.setPrefWidth(80);

        Label portLabel = new Label("Porti:");
        TextField portField = new TextField(SCALE_PORT);
        portField.setPrefWidth(80);
//        Label massTimeLabelExit = new Label("Chiqish Tarozi massasini aniqlash vaqti (ms):");
//        TextField massTimeFieldExit = new TextField(SCALE_TIMEOUT.toString());
//
//        Label portLabelExit = new Label("Porti:");
//        TextField portFieldExit = new TextField(SCALE_PORT);

        // Buttons
//        Button saveButton = new Button("Saqlash");
//        Button cancelButton = new Button("Bekor qilish");

//        saveButton.setDisable(true); // Disabled by default
//
//        // Enable Save button on any change in text fields
//        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
//            saveButton.setDisable(
//                    massTimeField.getText().equals(SCALE_TIMEOUT.toString()) &&
//                            portField.getText().equals(SCALE_PORT) &&
//                            massTimeFieldExit.getText().equals(EXIT_TIMEOUT.toString()) &&
//                            portFieldExit.getText().equals(SCALE_EXIT_PORT)
//            );
//        };

//        massTimeField.textProperty().addListener(changeListener);
//        portField.textProperty().addListener(changeListener);
//
//        massTimeFieldExit.textProperty().addListener(changeListener);
//        portFieldExit.textProperty().addListener(changeListener);
//
//        saveButton.setOnAction(event -> {
//            SCALE_TIMEOUT = Integer.parseInt(massTimeField.getText());
//            SCALE_PORT = portField.getText();
//            configurations.setScaleTimeout(Integer.parseInt(massTimeField.getText()));
//            configurations.setScalePort(portField.getText());
//
//            EXIT_TIMEOUT = Integer.parseInt(massTimeFieldExit.getText());
//            SCALE_EXIT_PORT = portFieldExit.getText();
//            configurations.setExitTimeout(Integer.parseInt(massTimeFieldExit.getText()));
//            configurations.setScaleExitPort(portFieldExit.getText());
//            configUtilsService.saveConfig(configurations);
//            // Add save logic here
//            popupStage.close();
//        });

//        cancelButton.setOnAction(event -> popupStage.close());
//
//        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
//        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");
//
//        HBox buttonBox = new HBox(saveButton, cancelButton);
//        buttonBox.setSpacing(10);
//        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox v = new VBox(10, tarozi, new HBox(20,
                new VBox(10,
                        massTimeLabel, massTimeField
                ),
                new VBox(10,
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
    private Node showTaroziPopupOut() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
//        popupStage.setTitle("Tarozi Sozlamalari");

        // Form Elements
//        Label exitTimeLabel = new Label("Mashinaning qaytib chiqish vaqti (ms):");
//        TextField exitTimeField = new TextField(EXIT_TIMEOUT.toString());

        // Form Elements
        Label massTimeLabel = new Label("Vazn aniqlash vaqti (ms):");
        Label tarozi = new Label("Tarozi");

        TextField massTimeField = new TextField(EXIT_TIMEOUT.toString());
        massTimeField.setPrefWidth(80);

        Label portLabel = new Label("Porti:");
        TextField portField = new TextField(SCALE_EXIT_PORT);
        portField.setPrefWidth(80);
//        Label massTimeLabelExit = new Label("Chiqish Tarozi massasini aniqlash vaqti (ms):");
//        TextField massTimeFieldExit = new TextField(SCALE_TIMEOUT.toString());
//
//        Label portLabelExit = new Label("Porti:");
//        TextField portFieldExit = new TextField(SCALE_PORT);

        // Buttons
//        Button saveButton = new Button("Saqlash");
//        Button cancelButton = new Button("Bekor qilish");

//        saveButton.setDisable(true); // Disabled by default
//
//        // Enable Save button on any change in text fields
//        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
//            saveButton.setDisable(
//                    massTimeField.getText().equals(SCALE_TIMEOUT.toString()) &&
//                            portField.getText().equals(SCALE_PORT) &&
//                            massTimeFieldExit.getText().equals(EXIT_TIMEOUT.toString()) &&
//                            portFieldExit.getText().equals(SCALE_EXIT_PORT)
//            );
//        };

//        massTimeField.textProperty().addListener(changeListener);
//        portField.textProperty().addListener(changeListener);
//
//        massTimeFieldExit.textProperty().addListener(changeListener);
//        portFieldExit.textProperty().addListener(changeListener);
//
//        saveButton.setOnAction(event -> {
//            SCALE_TIMEOUT = Integer.parseInt(massTimeField.getText());
//            SCALE_PORT = portField.getText();
//            configurations.setScaleTimeout(Integer.parseInt(massTimeField.getText()));
//            configurations.setScalePort(portField.getText());
//
//            EXIT_TIMEOUT = Integer.parseInt(massTimeFieldExit.getText());
//            SCALE_EXIT_PORT = portFieldExit.getText();
//            configurations.setExitTimeout(Integer.parseInt(massTimeFieldExit.getText()));
//            configurations.setScaleExitPort(portFieldExit.getText());
//            configUtilsService.saveConfig(configurations);
//            // Add save logic here
//            popupStage.close();
//        });

//        cancelButton.setOnAction(event -> popupStage.close());
//
//        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
//        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");
//
//        HBox buttonBox = new HBox(saveButton, cancelButton);
//        buttonBox.setSpacing(10);
//        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox v = new VBox(10, tarozi, new HBox(20,
                new VBox(10,
                        massTimeLabel, massTimeField
                ),
                new VBox(10,
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
        VBox layout = new VBox(10,
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
        VBox layout = new VBox(10,
                exitTimeLabel, exitTimeField,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 350, 200);
        popupStage.setScene(popupScene);
        popupStage.show();
    }


    private Node showPrinterPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Printer Sozlamalari");

        // Form Elements
        Label printerNameLabel = new Label("Printer nomi:");
        TextField printerNameField = new TextField(PRINTER_NAME);

        // Buttons
        Button testButton = new Button("Test");
//        Button saveButton = new Button("Saqlash");
//        Button cancelButton = new Button("Bekor qilish");

//        saveButton.setDisable(true); // Disabled by default
//
//        // Enable Save button when the text field value changes
//        printerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
//            saveButton.setDisable(printerNameField.getText().equals(PRINTER_NAME));
//        });

        // Test button action
        testButton.setOnAction(event -> {
            printCheck.testCheckRecipient();
        });

//        saveButton.setOnAction(event -> {
//            PRINTER_NAME = printerNameField.getText();
//            configurations.setPrinterName(printerNameField.getText());
//            configUtilsService.saveConfig(configurations);
//            // Add save logic here
//            popupStage.close();
//        });
//
//        cancelButton.setOnAction(event -> popupStage.close());

        testButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 15;");
//        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
//        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(testButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Layout
        VBox layout = new VBox(10,
                printerNameLabel, printerNameField,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

//        Scene popupScene = new Scene(layout, 400, 200);
//        popupStage.setScene(popupScene);
//        popupStage.show();
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
        VBox layout = new VBox(10, infoLabel, closeButton);
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
        VBox rowContainer = new VBox(10);
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
        VBox sectionLayout = new VBox(10, imageView, descriptionLabel, new Separator());
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
//        Label exitTimeLabel = new Label("Mashinaning qaytib chiqish vaqti (ms):");
//        TextField exitTimeField = new TextField(EXIT_TIMEOUT.toString());

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
        VBox layout = new VBox(10,
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
