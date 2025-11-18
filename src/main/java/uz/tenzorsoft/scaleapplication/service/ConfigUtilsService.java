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
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;

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
        config.setControllerIp("192.168.7.244");
        config.setControllerPort(Modbus.DEFAULT_PORT);
        config.setControllerConnectTimeout(2000);
        config.setCloseGate1Timeout(8000);
        config.setCloseGate2Timeout(8000);
        config.setKppcloseGate1Timeout(8000);
        config.setKppcloseGate2Timeout(8000);
        config.setKpppinInIn(KPP_OPEN_GATE_EXIT_1);
        config.setKpppinInOut(KPP_CLOSE_GATE_EXIT_1);
        config.setKpppinOutIn(KPP_OPEN_GATE_EXIT_2);
        config.setKpppinOutOut(KPP_CLOSE_GATE_EXIT_2);
        config.setPinInIn(RASP_OPEN_GATE_1);
        config.setPinInOut(RASP_CLOSE_GATE_1);
        config.setPinInIn2(RASP_OPEN_GATE_2);
        config.setPinInOut2(RASP_CLOSE_GATE_2);
        config.setCloseGateExit1Timeout(8000);
        config.setCloseGateExit2Timeout(8000);

        config.setPinOutIn(RASP_OPEN_GATE_EXIT_1);
        config.setPinOutOut(RASP_CLOSE_GATE_EXIT_1);
        config.setPinOutIn2(RASP_OPEN_GATE_EXIT_2);
        config.setPinOutOut2(RASP_CLOSE_GATE_EXIT_2);

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
        config.setCamera1("192.168.1.64");
        config.setCamera2("192.168.1.65");
        config.setCamera3("192.168.1.66");
        config.setCamera4("192.168.1.67");
        config.setCameraRezerv1("");
        config.setCameraRezerv2("");
        config.setRaspberryUsing(0);
        config.setSensorIn1(RASP_SENSOR_1);
        config.setSensorIn2(RASP_SENSOR_2);
        config.setSensorIn3(RASP_SENSOR_3);
        config.setSensorOut1(RASP_SENSOR_EXIT_1);
        config.setSensorOut2(RASP_SENSOR_EXIT_2);
        config.setSensorOut3(RASP_SENSOR_EXIT_3);
        return config;
    }

    private void applyConfigurations(Configurations config, Configurations defaultConfig) {
        Settings.CONTROLLER_IP = config.getControllerIp() == null ? defaultConfig.getControllerIp() : config.getControllerIp();
        Settings.CONTROLLER_PORT = config.getControllerPort() == null ? defaultConfig.getControllerPort() : config.getControllerPort();
        Settings.CONTROLLER_CONNECT_TIMEOUT = config.getControllerConnectTimeout() == null ? defaultConfig.getControllerConnectTimeout() : config.getControllerConnectTimeout();
        Settings.CLOSE_GATE1_TIMEOUT = config.getCloseGate1Timeout() == null ? defaultConfig.getCloseGate1Timeout() : config.getCloseGate1Timeout();
        Settings.CLOSE_GATE2_TIMEOUT = config.getCloseGate2Timeout() == null ? defaultConfig.getCloseGate2Timeout() : config.getCloseGate2Timeout();
        Settings.PIN_IN_IN = config.getPinInIn() == null ? defaultConfig.getPinInIn() : config.getPinInIn();
        Settings.PIN_IN_OUT = config.getPinInOut() == null ? defaultConfig.getPinInOut() : config.getPinInOut();
        Settings.PIN_IN_IN2 = config.getPinInIn2() == null ? defaultConfig.getPinInIn2() : config.getPinInIn2();
        Settings.PIN_IN_OUT2 = config.getPinInOut2() == null ? defaultConfig.getPinInOut2() : config.getPinInOut2();

        Settings.KPP_CLOSE_GATE1_TIMEOUT = config.getCloseGate1Timeout() == null ? defaultConfig.getCloseGate1Timeout() : config.getCloseGate1Timeout();
        Settings.KPP_CLOSE_GATE2_TIMEOUT = config.getCloseGate2Timeout() == null ? defaultConfig.getCloseGate2Timeout() : config.getCloseGate2Timeout();

        Settings.CLOSE_GATE1_EXIT_TIMEOUT = config.getCloseGate1Timeout() == null ? defaultConfig.getCloseGate1Timeout() : config.getCloseGate1Timeout();
        Settings.CLOSE_GATE2_EXIT_TIMEOUT = config.getCloseGate2Timeout() == null ? defaultConfig.getCloseGate2Timeout() : config.getCloseGate2Timeout();

        Settings.PIN_OUT_IN = config.getPinOutIn() == null ? defaultConfig.getPinOutIn() : config.getPinOutIn();
        Settings.PIN_OUT_OUT = config.getPinOutOut() == null ? defaultConfig.getPinOutOut() : config.getPinOutOut();
        Settings.PIN_OUT_IN2 = config.getPinOutIn2() == null ? defaultConfig.getPinOutIn2() : config.getPinOutIn2();
        Settings.PIN_OUT_OUT2 = config.getPinOutOut2() == null ? defaultConfig.getPinOutOut2() : config.getPinOutOut2();

        Settings.KPP_PIN_IN_IN = config.getKpppinInIn() == null ? defaultConfig.getKpppinInIn() : config.getKpppinInIn();
        Settings.KPP_PIN_IN_OUT = config.getKpppinInOut() == null ? defaultConfig.getKpppinInOut() : config.getKpppinInOut();
        Settings.KPP_EXIT_PIN_IN_IN = config.getKpppinOutIn() == null ? defaultConfig.getKpppinOutIn() : config.getKpppinOutIn();
        Settings.KPP_EXIT_PIN_IN_OUT = config.getKpppinOutOut() == null ? defaultConfig.getKpppinOutOut() : config.getKpppinOutOut();

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
