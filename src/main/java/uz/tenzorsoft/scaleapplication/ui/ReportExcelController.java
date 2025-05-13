package uz.tenzorsoft.scaleapplication.ui;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class ReportExcelController {

    @Getter
    private boolean confirmed = false;

    @FXML
    private Button confirmButton; // Define the button

    @FXML
    private Button cancelButton; // Define the button

    @FXML
    private void initialize() {
        confirmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 20;");
        cancelButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 20;");
    }


    @FXML
    private void confirm() {
        confirmed = true;
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void cancel() {
        confirmed = false;
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

}

