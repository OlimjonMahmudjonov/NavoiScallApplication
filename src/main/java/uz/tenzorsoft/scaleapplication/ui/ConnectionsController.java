package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.service.ControllerService;
import uz.tenzorsoft.scaleapplication.service.LogService;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;


@Component
@RequiredArgsConstructor
public class ConnectionsController implements BaseController {

    private final ExecutorService executors;
    private final ControllerService controllerService;
    private final LogService logService;
    @FXML
    private ImageView controller, camera1, camera2, camera3, gate1, gate2, sensor1, sensor2, sensor3;

    public void initialize() {
        controller.setImage(redLight);
        camera1.setImage(redLight);
        camera2.setImage(redLight);
        camera3.setImage(redLight);
        sensor1.setImage(redLight);
        sensor2.setImage(redLight);
        sensor3.setImage(redLight);
        gate1.setImage(redLight);
        gate2.setImage(redLight);
    }

    public void updateConnections() {
        executors.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!isTesting &&  !isRaspberryUsing) {
                        gate1Connection = controllerService.checkConnection(COIL_CLOSE_GATE_1);
                        gate2Connection = controllerService.checkConnection(COIL_CLOSE_GATE_2);
                        sensor1Connection = controllerService.checkConnection(COIL_SENSOR_1);
                        sensor2Connection = controllerService.checkConnection(COIL_SENSOR_2);
                        sensor3Connection = controllerService.checkConnection(COIL_SENSOR_3);
                    }
                    camera1Connection = controllerService.checkConnection(CAMERA_1);
//                    System.out.println("camera1Connection: " + camera1Connection);
                    camera2Connection = controllerService.checkConnection(CAMERA_2);
//                    System.out.println("camera2Connection: " + camera2Connection);
                    camera3Connection = controllerService.checkConnection(CAMERA_3);
//                    System.out.println("camera3Connection: " + camera3Connection);
                    isConnectedToInternet = controllerService.checkInternetConnection(GOOGLE_DNS);
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
                    logService.save(new LogEntity(5L, Instances.truckNumber, "00030: (" + getClass().getName() + ") " + e.getMessage()));
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, Instances.truckNumber, "00031: (" + getClass().getName() + ") " + e.getMessage()));
                    //showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    public void showConnections() {
        executors.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    sensor1.setImage(sensor1Connection ? greenLight : redLight);
                    sensor2.setImage(sensor2Connection ? greenLight : redLight);
                    sensor3.setImage(sensor3Connection ? greenLight : redLight);

                    camera1.setImage(camera1Connection ? greenLight : redLight);
                    camera2.setImage(camera2Connection ? greenLight : redLight);
                    camera3.setImage(camera3Connection ? greenLight : redLight);

                    controller.setImage(isConnected ? greenLight : redLight);
                    gate1.setImage(gate1Connection ? greenLight : redLight);
                    gate2.setImage(gate2Connection ? greenLight : redLight);
                    Thread.sleep(500);
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, Instances.truckNumber, e.getMessage()));
                    e.printStackTrace();
                }
            }
        });
    }


}
