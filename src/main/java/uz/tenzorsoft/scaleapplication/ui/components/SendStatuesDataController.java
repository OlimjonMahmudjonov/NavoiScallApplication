package uz.tenzorsoft.scaleapplication.ui.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.StatusEntity;
import uz.tenzorsoft.scaleapplication.repository.StatusRepository;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.sendData.SendDataService;

import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;

@Component
@RequiredArgsConstructor
public class SendStatuesDataController {


    private final ExecutorService executors;
    private final SendDataService sendDataService;
    private final StatusRepository statusRepository;
    private final LogService logService;
    private StatusEntity lastStatuses = new StatusEntity();

    public void startSending() {
        executors.execute(() -> {
            while (true) {
                try {
                    if (lastStatuses.isController() != isConnected
                            || lastStatuses.isGate1() != gate1Connection
                            || lastStatuses.isGate2() != gate2Connection

                            || lastStatuses.isSensor1() != sensor1Connection
                            || lastStatuses.isSensor2() != sensor2Connection
                            || lastStatuses.isSensor3() != sensor3Connection

                            || lastStatuses.isGateExit1() != gateExit1Connection
                            || lastStatuses.isGateExit2() != gateExit2Connection

                            || lastStatuses.isSensorExit1() != sensorExit1Connection
                            || lastStatuses.isSensorExit2() != sensorExit2Connection
                            || lastStatuses.isSensorExit3() != sensorExit3Connection

                            || lastStatuses.isCamera1() != camera1Connection
                            || lastStatuses.isCamera2() != camera2Connection
                            || lastStatuses.isCamera3() != camera3Connection
                    ) {
                        StatusEntity status = new StatusEntity();
                        lastStatuses.setController(isConnected);
                        status.setController(isConnected);

                        lastStatuses.setGate1(gate1Connection);
                        lastStatuses.setGate2(gate2Connection);

                        lastStatuses.setGateExit1(gateExit1Connection);
                        lastStatuses.setGateExit2(gateExit2Connection);

                        status.setGate1(gate1Connection);
                        status.setGate2(gate2Connection);

                        status.setGateExit1(gateExit1Connection);
                        status.setGateExit2(gateExit2Connection);

                        lastStatuses.setCamera1(camera1Connection);
                        lastStatuses.setCamera2(camera2Connection);
                        lastStatuses.setCamera3(camera3Connection);

                        status.setCamera1(camera1Connection);
                        status.setCamera2(camera2Connection);
                        status.setCamera3(camera3Connection);

                        lastStatuses.setSensor1(sensor1Connection);
                        lastStatuses.setSensor2(sensor2Connection);
                        lastStatuses.setSensor3(sensor3Connection);

                        status.setSensor1(sensor1Connection);
                        status.setSensor2(sensor2Connection);
                        status.setSensor3(sensor3Connection);

                        lastStatuses.setSensorExit1(sensorExit1Connection);
                        lastStatuses.setSensorExit2(sensorExit2Connection);
                        lastStatuses.setSensorExit3(sensorExit3Connection);

                        status.setSensorExit1(sensorExit1Connection);
                        status.setSensorExit2(sensorExit2Connection);
                        status.setSensorExit3(sensorExit3Connection);

                        statusRepository.save(status);

//                        sendDataService.sendStatuses();
                    }

                    Thread.sleep(500);
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, truckNumber, "00041: (" + getClass().getName() + ") " + e.getMessage()));
                    e.printStackTrace();
                }
            }
        });
    }
}
