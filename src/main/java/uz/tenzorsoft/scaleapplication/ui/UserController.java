package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;

import static uz.tenzorsoft.scaleapplication.domain.Instances.currentUser;

@Component
@RequiredArgsConstructor
public class UserController {

    @FXML
    private Text changePasswordText;

    @FXML
    private Button logoutButton;

    @FXML
    private TextField username;

    @FXML
    private TextField phoneNumber;

    @FXML
    public void initialize() {
        changePasswordText.setOnMouseClicked(event -> openPasswordChangeDialog());
    }

    public void loadUserMenu() {
        username.setText(currentUser.getUsername());
        phoneNumber.setText(currentUser.getPhoneNumber());
    }

    @FXML
    private void handleLogoutClick() {
        // Change button color to indicate it's been pressed
        logoutButton.setStyle("-fx-background-color: darkred; -fx-text-fill: white;");

        // Create confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Tizimdan chiqmoqchimisiz?", ButtonType.YES, ButtonType.NO);
        confirmDialog.initStyle(StageStyle.UTILITY); // Simple utility style

        // Style the dialog content
        confirmDialog.setHeaderText(null);
        confirmDialog.setTitle("Tasdiqlash");

        // Customize button styles and set hand cursor
        Button yesButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.YES);
        yesButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        yesButton.setCursor(Cursor.HAND);  // Set hand cursor for "Yes"

        Button noButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.NO);
        noButton.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        noButton.setCursor(Cursor.HAND);   // Set hand cursor for "No"

        // Handle dialog response
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                System.exit(0); // Exit application or add your logout logic here
            } else {
                // Reset button color if 'No' is chosen
                logoutButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            }
        });
    }

    private void openPasswordChangeDialog() {
        // Create a new dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        dialog.setTitle("Parolni almashtirish");

        // Step 1: Phone Number Input Field (auto-filled and non-editable)
        Label phoneLabel = new Label("Telefon nomerini kiriting:");
        TextField phoneInput = new TextField();
        phoneInput.setText(currentUser.getPhoneNumber());  // Auto-filled phone number (replace with dynamic data if needed)
        phoneInput.setEditable(false);  // Make it non-editable
        phoneInput.setPrefWidth(200);  // Adjusted width
        phoneInput.setMaxWidth(250);   // Set max width to prevent it from getting too wide

        Button sendSmsButton = new Button("SMS Yuborish");
        setupButtonPressEffect(sendSmsButton, "#578fea", "#426bb2");

        // Step 2: SMS Code Input Field (initially hidden)
        Label smsLabel = new Label("SMS kodini kiriting:");
        smsLabel.setVisible(false);
        TextField smsInput = new TextField();
        smsInput.setPromptText("SMS kod");
        smsInput.setVisible(false);
        smsInput.setPrefWidth(200);  // Adjusted width
        smsInput.setMaxWidth(250);   // Set max width to prevent it from getting too wide

        Button confirmSmsButton = new Button("Tasdiqlash");
        confirmSmsButton.setVisible(false);
        setupButtonPressEffect(confirmSmsButton, "#578fea", "#426bb2");

        // Step 3: New Password Fields (initially hidden)
        Label newPasswordLabel = new Label("Yangi parol:");
        newPasswordLabel.setVisible(false);
        PasswordField newPasswordInput = new PasswordField();
        newPasswordInput.setPromptText("Yangi parol");
        newPasswordInput.setVisible(false);
        newPasswordInput.setPrefWidth(200);  // Adjusted width
        newPasswordInput.setMaxWidth(250);   // Set max width to prevent it from getting too wide

        Label confirmPasswordLabel = new Label("Parolni qayta kiriting:");
        confirmPasswordLabel.setVisible(false);
        PasswordField confirmPasswordInput = new PasswordField();
        confirmPasswordInput.setPromptText("Parolni qayta kiriting");
        confirmPasswordInput.setVisible(false);
        confirmPasswordInput.setPrefWidth(200);  // Adjusted width
        confirmPasswordInput.setMaxWidth(250);   // Set max width to prevent it from getting too wide

        Button confirmPasswordButton = new Button("Tasdiqlash");
        confirmPasswordButton.setVisible(false);
        setupButtonPressEffect(confirmPasswordButton, "#578fea", "#426bb2");

        // Layout for dialog components
        VBox dialogVbox = new VBox(10);
        dialogVbox.setAlignment(Pos.CENTER);  // Center all elements
        dialogVbox.setPadding(new Insets(20)); // Add padding to prevent elements from sticking to the edges
        dialogVbox.getChildren().addAll(phoneLabel, phoneInput, sendSmsButton, smsLabel, smsInput, confirmSmsButton, newPasswordLabel, newPasswordInput, confirmPasswordLabel, confirmPasswordInput, confirmPasswordButton);

        Scene dialogScene = new Scene(dialogVbox, 300, 400);
        dialog.setScene(dialogScene);

        // Step 1: Send SMS Button Action
        sendSmsButton.setOnAction(e -> {
            if (phoneInput.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Telefon nomeri talab qilinadi.", ButtonType.OK).showAndWait();
                return;
            }

            // Simulate sending SMS with the default code "11111"
            String smsCode = "11111"; // Temporary code
            saveSmsCodeToDatabase(smsCode);

            smsLabel.setVisible(true);
            smsInput.setVisible(true);
            confirmSmsButton.setVisible(true);
        });

        // Step 2: Confirm SMS Button Action
        confirmSmsButton.setOnAction(e -> {
            String smsCode = smsInput.getText();

            // Check SMS code (default code "11111")
            if ("11111".equals(smsCode)) {  // Temporary code for testing
                newPasswordLabel.setVisible(true);
                newPasswordInput.setVisible(true);
                confirmPasswordLabel.setVisible(true);
                confirmPasswordInput.setVisible(true);
                confirmPasswordButton.setVisible(true);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "SMS kod noto‘g‘ri, qayta urinib ko‘ring.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        // Step 3: Confirm Password Button Action
        confirmPasswordButton.setOnAction(e -> {
            String newPassword = newPasswordInput.getText().trim();  // Trim whitespace
            String confirmPassword = confirmPasswordInput.getText().trim();  // Trim whitespace

            // Check if any password field is empty
            if (newPassword.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Yangi parol maydoni bo'sh bo'lmasligi kerak.", ButtonType.OK).showAndWait();
                return;
            }

            if (confirmPassword.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Parolni qayta kiriting maydoni bo'sh bo'lmasligi kerak.", ButtonType.OK).showAndWait();
                return;
            }

            // Validate that the passwords match
            if (newPassword.equals(confirmPassword)) {
                // Update password in the backend (store it in DB)
                System.out.println("Password successfully changed!");
                dialog.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Parollar bir-biriga to'g'ri kelmadi.", ButtonType.OK);
                alert.showAndWait();
            }
        });


        // Show the dialog
        dialog.showAndWait();
    }

    private void setupButtonPressEffect(Button button, String normalColor, String pressedColor) {
        button.setStyle("-fx-background-color: " + normalColor + "; -fx-text-fill: white; -fx-border-radius: 7; -fx-background-radius: 10; -fx-cursor: hand;"); // Add cursor change here

        button.setOnMousePressed(event -> button.setStyle("-fx-background-color: " + pressedColor + "; -fx-text-fill: white; -fx-border-radius: 7; -fx-background-radius: 10; -fx-cursor: hand;"));

        button.setOnMouseReleased(event -> button.setStyle("-fx-background-color: " + normalColor + "; -fx-text-fill: white; -fx-border-radius: 7; -fx-background-radius: 10; -fx-cursor: hand;"));
    }

    private void saveSmsCodeToDatabase(String smsCode) {
        // Temporary method to simulate storing the SMS code in the database
        System.out.println("SMS code stored in DB: " + smsCode);
    }

}
