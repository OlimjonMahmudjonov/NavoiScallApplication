package uz.tenzorsoft.scaleapplication.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public interface BaseController {

    default void showAlert(Alert.AlertType alertType, String headerText, String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(alertType);
                alert.setTitle(headerText);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            }
        });
    }

}
