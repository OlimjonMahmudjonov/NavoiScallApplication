package uz.tenzorsoft.scaleapplication.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ScaleController {

    private final ButtonController buttonController;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @FXML
    private TextField scaleWeigh, scaleExitWeight;


    public void initialize() {
        if (scaleWeigh != null) {
            scaleWeigh.setDisable(true);
            scaleWeigh.setText("000 kg");
            Runtime.getRuntime().addShutdownHook(new Thread(buttonController::closePort));
        }
        if (scaleExitWeight != null) {
            scaleExitWeight.setDisable(true);
            scaleExitWeight.setText("000 kg");
            Runtime.getRuntime().addShutdownHook(new Thread(buttonController::closePort2)); // Portni tozalash
        }

    }


    public void showScale() {
        scheduler.scheduleAtFixedRate(() -> {
            double weigh = buttonController.getTruckWeigh();
            double exitWeigh = buttonController.getTruckExitWeigh();
            Platform.runLater(() -> {
                String displayText = weigh > 0 ? weigh + " kg" : "0.0 kg";
                scaleWeigh.setText(displayText);

                String displayExitText = exitWeigh > 0 ? exitWeigh + " kg" : "0.0 kg";
                scaleExitWeight.setText(displayExitText);
            });
        }, 0, 500, TimeUnit.MILLISECONDS);
    }
}
