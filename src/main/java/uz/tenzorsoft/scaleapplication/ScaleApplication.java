package uz.tenzorsoft.scaleapplication;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.service.raspberry.GpioControl;
import uz.tenzorsoft.scaleapplication.websocket.WebSocketClient;

@SpringBootApplication
@EnableScheduling
public class ScaleApplication extends Application {

    private ConfigurableApplicationContext context;

    private Parent rootNode;

    // No-arg constructor (required by JavaFX Application)
    public ScaleApplication() {
    }

    public static void main(String[] args) {
        Application.launch(ScaleApplication.class, args);
    }

    @Override
    public void init() throws Exception {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ScaleApplication.class);
        context = builder.run(getParameters().getRaw().toArray(new String[0]));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/index.fxml"));
        loader.setControllerFactory(context::getBean);
        loader.setLocation(getClass().getResource("/fxml/index.fxml"));
        rootNode = loader.load();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/index.fxml"));
        loader.setControllerFactory(context::getBean);
        rootNode = loader.load();
        primaryStage.setTitle("Scale Application");
        primaryStage.setScene(new Scene(rootNode));
        primaryStage.setResizable(false);
        primaryStage.show();
        WebSocketClient webSocketClient = context.getBean(WebSocketClient.class);
         try {
             webSocketClient.connect(Instances.WEBSOCKET_URL);
//              Boshlang'ich xabarlarni yuborish, agar kerak bo'lsa
              webSocketClient.sendMessage("gate1");
              webSocketClient.sendMessage("gate2");// Yoki "gate"
         } catch (Exception e) {
            System.err.println("WebSocket serveriga ulanishda xatolik (start): " + e.getMessage());
         }
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Dastur to‘xtayapti...");

        if (context != null) {
            GpioControl gpioControl = context.getBean(GpioControl.class);
            if (gpioControl != null) {
                gpioControl.shutdownCompletely();
            }
            context.close();
        }
        System.exit(0);
    }
}
