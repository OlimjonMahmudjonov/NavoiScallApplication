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
import uz.tenzorsoft.scaleapplication.service.raspberry.GpioControl;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.RASP_CLOSE_GATE_EXIT_2;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.RASP_CLOSE_GATE_2;

@Component
@RequiredArgsConstructor
public class ConnectionsController implements BaseController {

    private final ExecutorService executors;
    private final ControllerService controllerService;
    private final GpioControl gpioControl;
    private final LogService logService;

    @FXML
    private ImageView camera1, camera2, /*camera3,*/ gate1, gate2, sensor1, sensor2, sensor3,
            cameraExit1, cameraExit2, /* cameraExit3,*/ gateExit1, gateExit2,
            sensorExit1, sensorExit2, sensorExit3;

    public void initialize() {
        if (sensor1 != null) {
            camera1.setImage(redLight);
            camera2.setImage(redLight);
            // camera3.setImage(redLight); // <-- 3-kamera vaqtincha o‘chirilgan
            sensor1.setImage(redLight);
            sensor2.setImage(redLight);
            sensor3.setImage(redLight);
            gate1.setImage(redLight);
            gate2.setImage(redLight);
        }
        if (sensorExit1 != null) {
            cameraExit1.setImage(redLight);
            cameraExit2.setImage(redLight);
          //  cameraExit3.setImage(redLight);
            sensorExit1.setImage(redLight);
            sensorExit2.setImage(redLight);
            sensorExit3.setImage(redLight);
            gateExit1.setImage(redLight);
            gateExit2.setImage(redLight);
        }
    }

    public void updateConnections() {
        executors.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!isTesting && isRaspberryUsing) {
                        gate1Connection = controllerService.checkConnection(RASP_CLOSE_GATE_1);
                        gate2Connection = controllerService.checkConnection(RASP_CLOSE_GATE_2);

                        sensor1Connection = controllerService.checkConnection(RASP_SENSOR_1);
                        sensor2Connection = controllerService.checkConnection(RASP_SENSOR_2);
                        sensor3Connection = controllerService.checkConnection(RASP_SENSOR_3);

                        gateExit1Connection = controllerService.checkConnection(RASP_CLOSE_GATE_EXIT_1);
                        gateExit2Connection = controllerService.checkConnection(RASP_CLOSE_GATE_EXIT_2);
                        sensorExit1Connection = controllerService.checkConnectionUsingGPIO(RASP_SENSOR_EXIT_1);
                        sensorExit2Connection = controllerService.checkConnectionUsingGPIO(RASP_SENSOR_EXIT_2);
                        sensorExit3Connection = controllerService.checkConnectionUsingGPIO(RASP_SENSOR_EXIT_3);
                    }

                    camera1Connection = controllerService.checkConnection(CAMERA_1);
                    System.out.println("camera1Connection->>: " + camera1Connection);
                    camera2Connection = controllerService.checkConnection(CAMERA_2);
                    System.out.println("camera2Connection->>: " + camera2Connection);
                    camera3Connection = controllerService.checkConnection(CAMERA_3);
                    camera4Connection = controllerService.checkConnection(CAMERA_4);
                    System.out.println("camera4Connection->>: " + camera4Connection);
                    cameraRezerv1Connection = controllerService.checkConnection(CAMERA_REZERV1);
                    cameraRezerv2Connection = controllerService.checkConnection(CAMERA_REZERV2);
                     System.out.println("camera3Connection->>: " + camera3Connection);
                    isConnectedToInternet = controllerService.checkInternetConnection(GOOGLE_DNS);

                    sensor1.setImage(sensor1Connection ? greenLight : redLight);
                    sensor2.setImage(sensor2Connection ? greenLight : redLight);
                    sensor3.setImage(sensor3Connection ? greenLight : redLight);

                    sensorExit1.setImage(sensorExit1Connection ? greenLight : redLight);
                    sensorExit2.setImage(sensorExit2Connection ? greenLight : redLight);
                    sensorExit3.setImage(sensorExit3Connection ? greenLight : redLight);

                    camera1.setImage(camera1Connection ? greenLight : redLight);
                    camera2.setImage(camera2Connection ? greenLight : redLight);
                    // camera3.setImage(cameraRezerv1Connection ? greenLight : redLight);
                    cameraExit1.setImage(camera4Connection ? greenLight : redLight);
                    cameraExit2.setImage(cameraRezerv1Connection ? greenLight : redLight);
                 //   cameraExit3.setImage(cameraRezerv2Connection ? greenLight : redLight);

                    gate1.setImage(gate1Connection ? greenLight : redLight);
                    gate2.setImage(gate2Connection ? greenLight : redLight);

                    gateExit1.setImage(gateExit1Connection ? greenLight : redLight);
                    gateExit2.setImage(gateExit2Connection ? greenLight : redLight);

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logService.save(new LogEntity(5L, Instances.truckNumber,
                            "00030: (" + getClass().getName() + ") " + e.getMessage()));
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, Instances.truckNumber,
                            "00031: (" + getClass().getName() + ") " + e.getMessage()));
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

                    sensorExit1.setImage(sensorExit1Connection ? greenLight : redLight);
                    sensorExit2.setImage(sensorExit2Connection ? greenLight : redLight);
                    sensorExit3.setImage(sensorExit3Connection ? greenLight : redLight);

                    camera1.setImage(camera1Connection ? greenLight : redLight);
                    camera2.setImage(camera2Connection ? greenLight : redLight);
                    // camera3.setImage(cameraRezerv1Connection ? greenLight : redLight); // <-- 3-kamera o‘chirilgan
                    cameraExit1.setImage(camera4Connection ? greenLight : redLight);
                    cameraExit2.setImage(cameraRezerv1Connection ? greenLight : redLight);
                  //  cameraExit3.setImage(cameraRezerv2Connection ? greenLight : redLight);

                    gate1.setImage(gate1Connection ? greenLight : redLight);
                    gate2.setImage(gate2Connection ? greenLight : redLight);

                    gateExit1.setImage(gateExit1Connection ? greenLight : redLight);
                    gateExit2.setImage(gateExit2Connection ? greenLight : redLight);

                    Thread.sleep(500);
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, Instances.truckNumber, e.getMessage()));
                    logService.save(new LogEntity(5L, Instances.truckExitNumber, e.getMessage()));
                    e.printStackTrace();
                }
            }
        });
    }
}
