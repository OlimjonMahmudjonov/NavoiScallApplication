package uz.tenzorsoft.scaleapplication.service;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.pi4j.Pi4J;
import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.ui.MainController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isAvailableToConnect;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnected;
import static uz.tenzorsoft.scaleapplication.domain.Settings.CONTROLLER_IP;
import static uz.tenzorsoft.scaleapplication.domain.Settings.CONTROLLER_PORT;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;

@Service
@RequiredArgsConstructor
public class ControllerService {

    public void connect() throws Exception {
//        if (!isAvailableToConnect) return;

        if (CONTROLLER_IP == null){
            System.out.println("Controller ip is nullllllllllllllllllllllllllllllllllllllllllllllllllllll");
        }else {
            System.out.println("Controller ip is not nullllllllllllllllllllllllllllllllllllllllllllllllllllll");
        }
        if (CONTROLLER_PORT == null){
            System.out.println("Controller port is nullllllllllllllllllllllllllllllllllllllllllllllllllll");
        }else {
            System.out.println("Controller port is not nullllllllllllllllllllllllllllllllllllllllllllllllllll");
        }
        address = InetAddress.getByName(CONTROLLER_IP);
        connection = new TCPMasterConnection(address);
        connection.setTimeout(3000);
        connection.setPort(CONTROLLER_PORT);
        connection.connect();
        isConnected = true;
        System.out.println("CONTROLLERGA ULANILDIIIII");
        scalePort.openPort();
    }

    public boolean openGate1() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Not connected to controller");
        }
        writeCoil(COIL_GREEN_LIGHT_1, true);
        writeCoil(COIL_OPEN_GATE_1, true);
        writeCoil(COIL_CLOSE_GATE_1, false);
        return true;
    }
    public boolean kppopenGate1() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Not connected to controller");
        }
        writeCoil(KPP_OPEN_GATE_EXIT_1, true);
        writeCoil(KPP_OPEN_GATE_EXIT_1, false);
        return true;
    }
    public boolean kppopenGate2() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Not connected to controller");
        }
        writeCoil(KPP_OPEN_GATE_EXIT_2, true);
        writeCoil(KPP_OPEN_GATE_EXIT_2, false);
        return true;
    }

    public boolean openExitGate1() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Not connected to controller");
        }
        writeCoil(COIL_GREEN_LIGHT_EXIT_1, true);
        writeCoil(COIL_OPEN_GATE_EXIT_1, true);
        writeCoil(COIL_CLOSE_GATE_EXIT_1, false);
        return true;
    }

    public boolean openGate1(int truckPosition) throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        ScaleSystem.truckPosition = truckPosition;
        return openGate1();
    }

    public boolean kppopenGate1(int truckPosition) throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        ScaleSystem.truckPosition = truckPosition;
        return openGate1();
    }

    public boolean closeGate1() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        writeCoil(COIL_GREEN_LIGHT_1, false);
        writeCoil(COIL_OPEN_GATE_1, false);
        writeCoil(COIL_CLOSE_GATE_1, true);
        return true;
    }
    public boolean kppcloseGate1() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
//        writeCoil(COIL_GREEN_LIGHT_1, false);
        writeCoil(KPP_OPEN_GATE_EXIT_1, false);
        writeCoil(KPP_CLOSE_GATE_EXIT_1, true);
        return true;
    }
    public boolean kppcloseGate2() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
//        writeCoil(COIL_GREEN_LIGHT_1, false);
        writeCoil(KPP_OPEN_GATE_EXIT_2, false);
        writeCoil(KPP_CLOSE_GATE_EXIT_2, true);
        return true;
    }

    public boolean closeExitGate1() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        writeCoil(COIL_GREEN_LIGHT_EXIT_1, false);
        writeCoil(COIL_OPEN_GATE_EXIT_1, false);
        writeCoil(COIL_CLOSE_GATE_EXIT_1, true);
        return true;
    }


    public boolean openGate2() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        writeCoil(COIL_GREEN_LIGHT_2, true);
        writeCoil(COIL_OPEN_GATE_2, true);
        writeCoil(COIL_CLOSE_GATE_2, false);
        return true;
    }

    public boolean openExitGate2() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        writeCoil(COIL_GREEN_LIGHT_EXIT_2, true);
        writeCoil(COIL_OPEN_GATE_EXIT_2, true);
        writeCoil(COIL_CLOSE_GATE_EXIT_2, false);
        return true;
    }

    public boolean openGate2(int truckPosition) throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        ScaleSystem.truckPosition = truckPosition;
        openGate2();
        return true;
    }

    public boolean openExitGate1(int truckPosition) throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        ScaleSystem.truckExitPosition = truckPosition;
        openExitGate1();
        return true;
    }

    public boolean openExitGate2(int truckPosition) throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        truckExitPosition = truckPosition;
        openExitGate2();
        return true;
    }

    public boolean closeGate2() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        writeCoil(COIL_GREEN_LIGHT_2, false);
        writeCoil(COIL_OPEN_GATE_2, false);
        writeCoil(COIL_CLOSE_GATE_2, true);
        return true;
    }

    public boolean closeExitGate2() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        writeCoil(COIL_GREEN_LIGHT_EXIT_2, false);
        writeCoil(COIL_OPEN_GATE_EXIT_2, false);
        writeCoil(COIL_CLOSE_GATE_EXIT_2, true);
        return true;
    }

    public boolean checkConnection(Integer coilAddress) throws Exception {
        if (connection == null){
            if (CONTROLLER_IP == null){
                System.out.println("Controller ip is null");
            }else {
                System.out.println("Controller ip is not null");
            }
            if (CONTROLLER_PORT == null){
                System.out.println("Controller port is null");
            }else {
                System.out.println("Controller port is not null");
            }
            address = InetAddress.getByName(CONTROLLER_IP);
            connection = new TCPMasterConnection(address);
            connection.setTimeout(3000);
            connection.setPort(CONTROLLER_PORT);
            connection.connect();
            isConnected = true;
            System.out.println("CONTROLLERGA ULANILDI");
            scalePort.openPort();
        }
        if (isConnected) {
            return readCoil(coilAddress) == 1;
        }
        System.out.println("Controllerga ulanib bo`lmadi");
        return false;
    }

    public boolean checkConnectionUsingGPIO(Integer coilAddress) throws Exception {
        if (isConnected) {
            return readGPIO(coilAddress) == 1;
        }
        return false;
    }

    private int readGPIO(int pinNumber) {
        var pi4j = Pi4J.newAutoContext();
        var gpio = pi4j.din().create(pinNumber);
        int result = gpio.isHigh() ? 1 : 0;
        pi4j.shutdown();
        return result;
    }

    public boolean checkConnection(String ipAddress) throws IOException {
        if (ipAddress != null && !ipAddress.equals("") && !ipAddress.isEmpty()) {
            InetAddress address = InetAddress.getByName(ipAddress);
            return address.isReachable(1000);
        } else return false;
    }


    public boolean checkInternetConnection(String ipAddress) throws IOException {
        try (Socket socket = new Socket()) {
            // 8.8.8.8 - Google DNS serveri, 53 - DNS porti
            socket.connect(new InetSocketAddress(ipAddress, 53), 2000);
            return true; // Internet bor
        } catch (Exception e) {
            return false; // Internet yo'q
        }
    }

    private void writeCoil(Integer coil, boolean state) throws ModbusException {
        WriteCoilRequest request = new WriteCoilRequest(coil, state);
        if (transaction == null) transaction = new ModbusTCPTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        ModbusResponse response = transaction.getResponse();
        if (response instanceof ExceptionResponse) {
            throw new ModbusException("Modbus exception response");
        }
    }

//    private int readCoil(Integer coilAddress) throws Exception {
//        System.out.println("=== DEBUG readCoil START ===");
//        System.out.println("Coil Address: " + coilAddress);
//
//        // Check connection first
//        if (connection == null) {
//            System.err.println("ERROR: connection is null!");
//            return -1;
//        }
//
//        if (!connection.isConnected()) {
//            System.err.println("ERROR: connection is not connected!");
//            System.out.println("Attempting to reconnect...");
//            try {
//                connect(); // Your connect method
//            } catch (Exception e) {
//                System.err.println("Reconnect failed: " + e.getMessage());
//                return -1;
//            }
//        }
//
//        System.out.println("Connection status: " + connection.isConnected());
//
//        try {
//            // Create request
//            ReadCoilsRequest request = new ReadCoilsRequest(coilAddress, 1);
//            System.out.println("Created ReadCoilsRequest for address: " + coilAddress);
//
//            // Create new transaction each time (recommended practice)
//            transaction = new ModbusTCPTransaction(connection);
//            transaction.setRequest(request);
//
//            transaction.execute();
//
//            ModbusResponse response = transaction.getResponse();
//
//            if (response == null) {
//                System.err.println("ERROR: No response received from transaction for coil: " + coilAddress);
//                return -1;
//            }
//
//            if (response instanceof ExceptionResponse exceptionResponse) {
//                System.err.println("ERROR: Modbus Exception Response!");
//                System.err.println("Exception Code: " + exceptionResponse.getExceptionCode());
//                System.err.println("Exception Message: " + exceptionResponse.getMessage());
//                return -1;
//            }
//
//            if (response instanceof ReadCoilsResponse readResponse) {
//                System.out.println("SUCCESS: Got ReadCoilsResponse");
//
//                // Check if we have coils
//                if (readResponse.getCoils() == null) {
//                    System.err.println("ERROR: Coils in response are null!");
//                    return -1;
//                }
//
//                System.out.println("Number of coils in response: " + readResponse.getBitCount());
//
//                boolean coilState = readResponse.getCoils().getBit(0);
//                System.out.println("Coil " + coilAddress + " state: " + coilState);
//                System.out.println("=== DEBUG readCoil END (SUCCESS) ===");
//
//                return coilState ? 1 : 0;
//            } else {
//                System.err.println("ERROR: Unexpected response type: " + response.getClass().getName());
//                System.err.println("Response toString: " + response.toString());
//                return -1;
//            }
//
//        } catch (Exception e) {
//            System.err.println("EXCEPTION in readCoil: " + e.getClass().getSimpleName());
//            System.err.println("Exception message: " + e.getMessage());
//            e.printStackTrace();
//            throw e;
//        }
//    }

    private int readCoil(Integer coilAddress) throws Exception {
        ReadCoilsRequest request = new ReadCoilsRequest(coilAddress, 1);
        if (transaction == null) transaction = new ModbusTCPTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        ModbusResponse response = transaction.getResponse();

        if (response == null) {
            System.err.println("No response received from transaction.");
            return -1;
        } else if (response instanceof ExceptionResponse) {
            System.err.println("Received an exception response: " + ((ExceptionResponse) response).getExceptionCode());
            return -1;
        } else if (response instanceof ReadCoilsResponse readResponse) {
//             Process readResponse, for example:
            System.out.println(coilAddress + "YANGILANDIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");
            return readResponse.getCoils().getBit(0) ? 1 : 0;  // Example: return 1 if coil is true, 0 if false
        } else {
            System.out.println("Unexpected response type: " + response.getClass().getName());
            return -1;
        }

    }

    public static int readCoil1(Integer coilAddress) throws Exception {
        ReadCoilsRequest request = new ReadCoilsRequest(coilAddress, 1);
        if (transaction == null) transaction = new ModbusTCPTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        ModbusResponse response = transaction.getResponse();

        if (response == null) {
            System.err.println("No response received from transaction.");
            return -1;
        } else if (response instanceof ExceptionResponse) {
            System.err.println("Received an exception response: " + ((ExceptionResponse) response).getExceptionCode());
            return -1;
        } else if (response instanceof ReadCoilsResponse readResponse) {
//             Process readResponse, for example:
            System.out.println(coilAddress + "YANGILANDIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");
            return readResponse.getCoils().getBit(0) ? 1 : 0;  // Example: return 1 if coil is true, 0 if false
        } else {
            System.out.println("Unexpected response type: " + response.getClass().getName());
            return -1;
        }

    }

    public void disconnect() {
        if (connection != null && connection.isConnected()) {
            connection.close();
            isConnected = false;
            if (scalePort != null)
                scalePort.closePort();
        }
    }
}
