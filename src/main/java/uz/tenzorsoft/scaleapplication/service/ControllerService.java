package uz.tenzorsoft.scaleapplication.service;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
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

        address = InetAddress.getByName(CONTROLLER_IP);
        connection = new TCPMasterConnection(address);
        connection.setTimeout(3000);
        connection.setPort(CONTROLLER_PORT);
        connection.connect();
        isConnected = true;
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


    public boolean closeGate1() throws ModbusException {
        if (!isConnected) {
            throw new ModbusException("Controllerga ulanmagan");
        }
        writeCoil(COIL_GREEN_LIGHT_1, false);
        writeCoil(COIL_OPEN_GATE_1, false);
        writeCoil(COIL_CLOSE_GATE_1, true);
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
        if (isConnected) {
            return readCoil(coilAddress) == 1;
        }
        return false;
    }

    public boolean checkConnection(String ipAddress) throws IOException {
        InetAddress address = InetAddress.getByName(ipAddress);
        return address.isReachable(1000);
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
            // Process readResponse, for example:
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
            scalePort.closePort();
        }
    }
}
