package uz.tenzorsoft.scaleapplication.service;

import com.ghgande.j2mod.modbus.Modbus;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.Configurations;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.Settings;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import static uz.tenzorsoft.scaleapplication.domain.Instances.configurations;
import static uz.tenzorsoft.scaleapplication.domain.Instances.directory;

@Service
public class ConfigUtilsService {

    private static final String CONFIG_FILE_PATH = "/bin/settings.conf";
    private final LogService logService;

    public ConfigUtilsService(LogService logService) {
        this.logService = logService;
    }

    public void saveConfig(Configurations config) {
        try {
            String pathName = directory + CONFIG_FILE_PATH;
            // Create the directory if it does not exist
            Path path = Paths.get(pathName).getParent();
            System.out.println("Path: " + pathName);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // Serialize and encode the config
            String encodedConfig = Base64.getEncoder().encodeToString(serialize(config));
            Files.writeString(Paths.get(pathName), encodedConfig);
            System.out.println("Configuration saved successfully.");

        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00012: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public void loadConfigurations() {
        try {
            String pathName = directory + CONFIG_FILE_PATH;
            Path path = Paths.get(pathName);
            System.out.println("Path: " + pathName);
            configurations = createDefaultConfigurations();

            if (Files.exists(path)) {
                String encodedConfig = Files.readString(path);
                byte[] decodedConfig = Base64.getDecoder().decode(encodedConfig);
                Configurations config = deserialize(decodedConfig);

                // Set values from config to Settings
                applyConfigurations(config, configurations);

            } else {
                // Set default values in Settings and create new Configurations
                applyConfigurations(configurations, configurations);

                // Save the default configuration to a new file
                saveConfig(configurations);
                System.out.println("Default configuration file created at " + CONFIG_FILE_PATH);
            }
        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00048: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private Configurations createDefaultConfigurations() {
        Configurations config = new Configurations();
        config.setControllerIp("192.168.1.5");
        config.setControllerPort(Modbus.DEFAULT_PORT);
        config.setControllerConnectTimeout(2000);
        config.setCloseGate1Timeout(8000);
        config.setCloseGate2Timeout(8000);
        config.setPinIn(2);
        config.setPinIn2(3);
        config.setCloseGateExit1Timeout(8000);
        config.setCloseGateExit2Timeout(8000);
        config.setPinOut(4);
        config.setPinOut2(5);
        config.setScaleTimeout(3000);
        config.setScalePort("COM3");
        config.setScaleExitPort("COM2");
        config.setExitTimeout(3000); // 3 minut
        config.setMycoalScaleId(0L); // 3 minut
        config.setScaleWebId(0L); // 3 minut
        config.setPrinterName("XP 80C");
        config.setDatabaseName("postgres");
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.setCamera1("192.168.7.63");
        config.setCamera2("192.168.7.65");
        config.setCamera3("192.168.7.64");
        config.setCamera4("192.168.7.66");
        config.setCameraRezerv1("");
        config.setCameraRezerv2("");
        config.setRaspberryUsing(0);
        config.setSensorIn1(17);
        config.setSensorIn2(27);
        config.setSensorIn3(22);
        config.setSensorOut1(14);
        config.setSensorOut2(15);
        config.setSensorOut3(18);
        return config;
    }

    private void applyConfigurations(Configurations config, Configurations defaultConfig) {
        Settings.CONTROLLER_IP = config.getControllerIp() == null ? defaultConfig.getControllerIp() : config.getControllerIp();
        Settings.CONTROLLER_PORT = config.getControllerPort() == null ? defaultConfig.getControllerPort() : config.getControllerPort();
        Settings.CONTROLLER_CONNECT_TIMEOUT = config.getControllerConnectTimeout() == null ? defaultConfig.getControllerConnectTimeout() : config.getControllerConnectTimeout();
        Settings.CLOSE_GATE1_TIMEOUT = config.getCloseGate1Timeout() == null ? defaultConfig.getCloseGate1Timeout() : config.getCloseGate1Timeout();
        Settings.CLOSE_GATE2_TIMEOUT = config.getCloseGate2Timeout() == null ? defaultConfig.getCloseGate2Timeout() : config.getCloseGate2Timeout();
        Settings.PIN_IN = config.getPinIn() == null ? defaultConfig.getPinIn() : config.getPinIn();
        Settings.PIN_IN2 = config.getPinIn2() == null ? defaultConfig.getPinIn2() : config.getPinIn2();

        Settings.CLOSE_GATE1_EXIT_TIMEOUT = config.getCloseGate1Timeout() == null ? defaultConfig.getCloseGate1Timeout() : config.getCloseGate1Timeout();
        Settings.CLOSE_GATE2_EXIT_TIMEOUT = config.getCloseGate2Timeout() == null ? defaultConfig.getCloseGate2Timeout() : config.getCloseGate2Timeout();
        Settings.PIN_OUT = config.getPinIn() == null ? defaultConfig.getPinOut() : config.getPinOut();
        Settings.PIN_OUT2 = config.getPinIn2() == null ? defaultConfig.getPinOut2() : config.getPinOut2();

        Settings.SCALE_TIMEOUT = config.getScaleTimeout() == null ? defaultConfig.getScaleTimeout() : config.getScaleTimeout();
        Settings.SCALE_PORT = config.getScalePort() == null ? defaultConfig.getScalePort() : config.getScalePort();
        Settings.SCALE_EXIT_PORT = config.getScaleExitPort() == null ? defaultConfig.getScaleExitPort() : config.getScaleExitPort();
        Settings.EXIT_TIMEOUT = config.getExitTimeout() == null ? defaultConfig.getExitTimeout() : config.getExitTimeout();
        Settings.PRINTER_NAME = config.getPrinterName() == null ? defaultConfig.getPrinterName() : config.getPrinterName();
        Settings.DATABASE_NAME = config.getDatabaseName() == null ? defaultConfig.getDatabaseName() : config.getDatabaseName();
        Settings.USERNAME = config.getUsername() == null ? defaultConfig.getUsername() : config.getUsername();
        Settings.PASSWORD = config.getPassword() == null ? defaultConfig.getPassword() : config.getPassword();
        Settings.CAMERA_1 = config.getCamera1() == null ? defaultConfig.getCamera1() : config.getCamera1();
        Settings.CAMERA_2 = config.getCamera2() == null ? defaultConfig.getCamera2() : config.getCamera2();
        Settings.CAMERA_3 = config.getCamera3() == null ? defaultConfig.getCamera3() : config.getCamera3();
        Settings.CAMERA_4 = config.getCamera4() == null ? defaultConfig.getCamera4() : config.getCamera4();
        Settings.CAMERA_REZERV1 = config.getCameraRezerv1() == null ? defaultConfig.getCameraRezerv1() : config.getCameraRezerv1();
        Settings.CAMERA_REZERV2 = config.getCameraRezerv2() == null ? defaultConfig.getCameraRezerv2() : config.getCameraRezerv2();
        Settings.MYCOAL_SCALE_ID = config.getMycoalScaleId() == null ? defaultConfig.getMycoalScaleId() : config.getMycoalScaleId();
        Settings.SCALE_WEB_ID = config.getScaleWebId() == null ? defaultConfig.getScaleWebId() : config.getScaleWebId();
        Settings.IS_RASPBERRY_USING = config.getRaspberryUsing() == 1;

        Settings.SENSOR_IN1 = config.getSensorIn1() == null ? defaultConfig.getSensorIn1() : config.getSensorIn1();
        Settings.SENSOR_IN2 = config.getSensorIn2() == null ? defaultConfig.getSensorIn2() : config.getSensorIn2();
        Settings.SENSOR_IN3 = config.getSensorIn3() == null ? defaultConfig.getSensorIn3() : config.getSensorIn3();

        Settings.SENSOR_OUT1 = config.getSensorOut1() == null ? defaultConfig.getSensorOut1() : config.getSensorOut1();
        Settings.SENSOR_OUT2 = config.getSensorOut2() == null ? defaultConfig.getSensorOut2() : config.getSensorOut2();
        Settings.SENSOR_OUT3 = config.getSensorOut3() == null ? defaultConfig.getSensorOut3() : config.getSensorOut3();
        Instances.isRaspberryUsing = config.getRaspberryUsing() == 1;
    }

    private Configurations deserialize(byte[] data) throws Exception {
        try (var bis = new ByteArrayInputStream(data); var in = new ObjectInputStream(bis)) {
            Configurations configurations1 = (Configurations) in.readObject();
            System.out.println(configurations1);
            return configurations1;
        }
    }

    private byte[] serialize(Configurations config) throws Exception {
        try (var bos = new java.io.ByteArrayOutputStream(); var out = new ObjectOutputStream(bos)) {
            out.writeObject(config);
            return bos.toByteArray();
        }
    }
}
