package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.response.UserResponse;
import uz.tenzorsoft.scaleapplication.service.CloudService;
import uz.tenzorsoft.scaleapplication.service.ConfigUtilsService;
import uz.tenzorsoft.scaleapplication.service.UserService;

import static uz.tenzorsoft.scaleapplication.domain.Settings.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.PIN_IN_IN;
import static uz.tenzorsoft.scaleapplication.domain.Settings.PIN_IN_IN2;
import static uz.tenzorsoft.scaleapplication.domain.Settings.PIN_IN_OUT;
import static uz.tenzorsoft.scaleapplication.domain.Settings.PIN_IN_OUT2;
import static uz.tenzorsoft.scaleapplication.domain.Settings.PIN_OUT_IN;
import static uz.tenzorsoft.scaleapplication.domain.Settings.PIN_OUT_IN2;
import static uz.tenzorsoft.scaleapplication.domain.Settings.PIN_OUT_OUT;
import static uz.tenzorsoft.scaleapplication.domain.Settings.PIN_OUT_OUT2;
import static uz.tenzorsoft.scaleapplication.domain.Settings.SENSOR_OUT1;
import static uz.tenzorsoft.scaleapplication.domain.Settings.SENSOR_OUT2;
import static uz.tenzorsoft.scaleapplication.domain.Settings.SENSOR_OUT3;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.RASP_CLOSE_GATE_EXIT_2;

@Component
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final MainController mainController;
    private final CloudService cloudService;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String phoneNumber = phoneNumberField.getText();
        String password = passwordField.getText();

        UserResponse userResponse = userService.validateUser(phoneNumber, password);

        if (userResponse != null) {
            Stage currentStage = (Stage) phoneNumberField.getScene().getWindow();
            currentStage.close();
            mainController.load();
        } else {
            if (cloudService.login(phoneNumber, password)) {
                showSmsVerificationPopup(phoneNumber, password);
            } else {
                showAlert(Alert.AlertType.ERROR, "Tizimga kirishda xatolik yuz berdi", "Telefon raqami yoki parol noto‘g‘ri.");
            }
        }
    }

    @FXML
    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
                handleLogin();
                break;
            default:
                break;
        }
    }


    private void showSmsVerificationPopup(String phoneNumber, String password) {
        Stage smsDialog = new Stage();
        smsDialog.initModality(Modality.APPLICATION_MODAL);
        smsDialog.setTitle("SMS tasdiqlash");

        Label smsCodeLabel = new Label("Telefoningizga yuborilgan SMS kodini kiriting:");
        TextField smsInput = new TextField();
        smsInput.setPromptText("SMS kod");

        Button confirmButton = new Button("Tasdiqlash");
        confirmButton.setDefaultButton(true);

        VBox vbox = new VBox(10, smsCodeLabel, smsInput, confirmButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        confirmButton.setOnMouseClicked(event -> {
            String smsCode = smsInput.getText();
            String verify = cloudService.verify(phoneNumber, password, smsCode);
            if (!verify.isEmpty() && !verify.equals("INCORRECT_PASSWORD")) {
                userService.create(phoneNumber, password);
                mainController.load();
                smsDialog.close();
                Stage currentStage = (Stage) phoneNumberField.getScene().getWindow();
                currentStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Tekshirish amalga oshmadi", "Noto'g'ri SMS kodi. Iltimos, qayta urinib koʻring.");
            }
        });

        smsDialog.setScene(new Scene(vbox, 300, 200));
        smsDialog.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) phoneNumberField.getScene().getWindow();
        stage.close();
    }
}
