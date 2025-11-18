
package uz.tenzorsoft.scaleapplication.ui.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.sendData.SendDataService;

import java.util.concurrent.*;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnectedToInternet;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isTesting;
import static uz.tenzorsoft.scaleapplication.domain.Settings.SCALE_WEB_ID;

@Component
@RequiredArgsConstructor
public class DataSendController {

    private final SendDataService sendDataService;
    private final LogService logService;
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);


    public void sendNotSentData() {
        System.out.println("Ma'lumot yuboradigan thread ishga tushmoqda .......");
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Internet bor yo'qligini teshkirmoqda ..");
            if (isConnectedToInternet) {
                System.out.println("Internetga ulangan");
                try {
                    if (SCALE_WEB_ID != 0){
                        System.out.println("Ma'lumotlarni tarozi webga jo'natadigan metod ishlayapti ...");
                        System.out.println("scale web id: " + SCALE_WEB_ID);
                        sendDataService.sendNotSentData();
                        System.out.println("Ma'lumotlar muvaffaqiyatli jo'natildi.");
                    }else {
                        System.out.println("Settingdan tarozi web id ni kiriting .......");
                    }
                } catch (Exception e) {
                    System.out.println("Ma'lumotlarni tarozi webga jo'natishda xatolik !!!!!!!!");
                    logService.save(new LogEntity(5L, Instances.truckNumber, "Error sendNotSentData(): " + e.getMessage()));
                }

                if (!isTesting) {
                    CompletableFuture.runAsync(() -> sendDataService.sendDataToMyCoal())
                            .exceptionally(e -> {
                                logService.save(new LogEntity(5L, Instances.truckNumber, "sendDataToMyCoal(): " + e.getMessage()));
                                return null;
                            });

                    CompletableFuture.runAsync(() -> sendDataService.sendLogsToServer())
                            .exceptionally(e -> {
                                logService.save(new LogEntity(5L, Instances.truckNumber, "sendLogsToServer(): " + e.getMessage()));
                                return null;
                            });

                    CompletableFuture.runAsync(() -> sendDataService.sendProductsToServer())
                            .exceptionally(e -> {
                                logService.save(new LogEntity(5L, Instances.truckNumber, "sendProductsToServer(): " + e.getMessage()));
                                return null;
                            });
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}