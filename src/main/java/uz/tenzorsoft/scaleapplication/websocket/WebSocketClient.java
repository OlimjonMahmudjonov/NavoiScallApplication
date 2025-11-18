package uz.tenzorsoft.scaleapplication.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled; // Make sure this import is present
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.request.CommandsRequest;
import uz.tenzorsoft.scaleapplication.domain.request.ShlagbaunDto;
import uz.tenzorsoft.scaleapplication.service.CommandsService;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.ShlagbaunService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime; // For logging with timestamp
import java.util.concurrent.atomic.AtomicBoolean; // For managing connection state

@Component
@ClientEndpoint
public class WebSocketClient {

    @Autowired
    private CommandsService commandsService;

    @Autowired
    private ShlagbaunService shlagbaunService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogService logService;

    private Session session;
    private final AtomicBoolean isConnecting = new AtomicBoolean(false); // To prevent multiple connection attempts concurrently

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("[" + LocalDateTime.now() + "] Serverga ulanildi (WebSocketClient). Sessiya ID: " + session.getId());
        this.session = session;
        isConnecting.set(false); // Successfully connected, reset connecting flag
        // You might want to send initial messages upon successful connection here if needed
        // For example:
        // sendMessage("gate1");
        // sendMessage("gate2");
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            System.out.println("[" + LocalDateTime.now() + "] Serverdan kelgan xabar: " + message);
            if (message.startsWith("CommandsDto")) {
                System.out.println("CommandsDto qayta ishlanmoqda...");
                CommandsRequest commands = parseCommandsRequest(message);
                commandsService.saveOrUpdateCommands(commands);
            } else if (message.startsWith("ShlagbaunDto")) {
                System.out.println("ShlagbaunDto qayta ishlanmoqda...");
                ShlagbaunDto shlagbaun = parseShlagbaunDto(message);
                shlagbaunService.handleShlagbaunCommand(shlagbaun);
            } else {
                System.out.println("Noma'lum formatdagi xabar: " + message);
            }
        } catch (Exception e) {
            if (logService != null) {
                logService.save(new LogEntity(5L, Instances.truckNumber, "Xabar qayta ishlashda xatolik: " + e.getMessage()));
            }
            System.err.println("[" + LocalDateTime.now() + "] Xabar qayta ishlashda xatolik: " + e.getMessage());
            System.err.println("Kelgan xom xabar: " + message);
        }
    }

    private CommandsRequest parseCommandsRequest(String message) {
        String jsonValue = convertToStringJson(message, "CommandsDto");
        CommandsRequest commands = new CommandsRequest();
        try {
            commands = objectMapper.readValue(jsonValue, CommandsRequest.class);
        } catch (JsonProcessingException e) {
            if (logService != null) {
                logService.save(new LogEntity(5L, Instances.truckNumber, "CommandsRequest parse xatoligi: " + e.getMessage()));
            }
            System.err.println("[" + LocalDateTime.now() + "] CommandsRequest parse xatoligi: " + e.getMessage());
        }
        return commands;
    }

    private ShlagbaunDto parseShlagbaunDto(String message) {
        String jsonValue = convertToStringJson(message, "ShlagbaunDto");
        ShlagbaunDto shlagbaun = new ShlagbaunDto();
        try {
            shlagbaun = objectMapper.readValue(jsonValue, ShlagbaunDto.class);
        } catch (JsonProcessingException e) {
            if (logService != null) {
                logService.save(new LogEntity(5L, Instances.truckNumber, "ShlagbaunDto parse xatoligi: " + e.getMessage()));
            }
            System.err.println("[" + LocalDateTime.now() + "] ShlagbaunDto parse xatoligi: " + e.getMessage());
        }
        return shlagbaun;
    }

    private String convertToStringJson(String dtoString, String dtoName) {
        String json = dtoString.substring(dtoName.length());
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }
        String[] pairs = json.split(",\\s*");
        StringBuilder jsonBuilder = new StringBuilder("{");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                jsonBuilder.append("\"").append(key).append("\":");
                if (value.equals("true") || value.equals("false") || value.matches("-?\\d+(\\.\\d+)?")) {
                    jsonBuilder.append(value);
                } else {
                    if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                        value = "\"" + value.substring(1, value.length() - 1).replace("\"", "\\\"") + "\""; // Escape inner quotes
                    } else {
                        value = "\"" + value.replace("\"", "\\\"") + "\""; // Escape inner quotes
                    }
                    jsonBuilder.append(value);
                }
                if (i < pairs.length - 1) {
                    jsonBuilder.append(",");
                }
            }
        }
        jsonBuilder.append("}");
        // System.out.println("Konvertatsiya qilingan JSON: " + jsonBuilder.toString()); // Can be verbose
        return jsonBuilder.toString();
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("[" + LocalDateTime.now() + "] Aloqa uzildi: " + reason.getReasonPhrase() + ". Sessiya ID: " + (session != null ? session.getId() : "N/A"));
        this.session = null; // Mark session as closed
        // Reconnection will be handled by the scheduled task
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[" + LocalDateTime.now() + "] Xatolik yuz berdi: " + throwable.getMessage() + ". Sessiya ID: " + (session != null ? session.getId() : "N/A"));
        if (logService != null) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "WebSocket xatoligi: " + throwable.getMessage()));
        }
        // Even on error, if the session is still open, it might be closed shortly after.
        // If the session is closed by the error, onClose will be called.
        // If the error doesn't close the session, the connection might still be problematic.
        // Marking session as null here might be too aggressive, let onClose handle it.
        // Reconnection will be handled by the scheduled task.
    }

    public synchronized void connect(String uri) {
        if (this.session != null && this.session.isOpen()) {
            System.out.println("[" + LocalDateTime.now() + "] WebSocket allaqachon ulangan.");
            isConnecting.set(false);
            return;
        }

        if (isConnecting.get()) {
            System.out.println("[" + LocalDateTime.now() + "] Hozirda ulanishga harakat qilinmoqda, yangi urinish o'tkazib yuborildi.");
            return;
        }

        isConnecting.set(true);
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            System.out.println("[" + LocalDateTime.now() + "] WebSocket serveriga ulanmoqda: " + uri);
            container.connectToServer(this, new URI(uri));
            // onOpen will set isConnecting to false upon success
        } catch (DeploymentException e) {
            System.err.println("[" + LocalDateTime.now() + "] WebSocket serveriga ulanishda xatolik (DeploymentException): " + uri + " - " + e.getMessage());
            if (logService != null) logService.save(new LogEntity(5L, Instances.truckNumber, "DeploymentException: " + e.getMessage()));
            this.session = null; // Ensure session is null on failure
            isConnecting.set(false);
        } catch (IOException e) {
            System.err.println("[" + LocalDateTime.now() + "] WebSocket serveriga ulanishda xatolik (IOException): " + e.getMessage());
            if (logService != null) logService.save(new LogEntity(5L, Instances.truckNumber, "IOException (connect): " + e.getMessage()));
            this.session = null;
            isConnecting.set(false);
        } catch (URISyntaxException e) {
            System.err.println("[" + LocalDateTime.now() + "] Noto'g'ri WebSocket URI: " + uri + " - " + e.getMessage());
            if (logService != null) logService.save(new LogEntity(5L, Instances.truckNumber, "URISyntaxException: " + e.getMessage()));
            this.session = null;
            isConnecting.set(false);
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now() + "] WebSocket serveriga ulanishda kutilmagan xatolik: " + e.getMessage());
            if (logService != null) logService.save(new LogEntity(5L, Instances.truckNumber, "Kutilmagan ulanish xatoligi: " + e.getMessage()));
            this.session = null;
            isConnecting.set(false);
        }
    }

    public void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            // System.out.println("[" + LocalDateTime.now() + "] Serverga yuborilayotgan xabar: " + message); // Can be verbose
            session.getAsyncRemote().sendText(message);
        } else {
            System.err.println("[" + LocalDateTime.now() + "] Xabar yuborib bo'lmadi ('" + message + "'). Sessiya ochiq emas.");
            if (logService != null) {
                logService.save(new LogEntity(5L, Instances.truckNumber, "Xabar yuborib bo'lmadi. Sessiya ochiq emas."));
            }
            // Do not try to connect directly from here, let the scheduled task handle it.
        }
    }

    @Scheduled(fixedRate = 5000) // Har 5 sekundda
    public void checkConnectionAndReconnect() {
        if (this.session == null || !this.session.isOpen()) {
            if (!isConnecting.get()) { // Only attempt to connect if not already trying
                System.out.println("[" + LocalDateTime.now() + "] WebSocket ulanmagan. Qayta ulanishga harakat qilinmoqda...");
                connect(Instances.WEBSOCKET_URL);
            } else {
                System.out.println("[" + LocalDateTime.now() + "] WebSocket ulanmagan, lekin ulanish jarayoni davom etmoqda.");
            }
        } else {
            // System.out.println("[" + LocalDateTime.now() + "] WebSocket ulangan va ochiq. Sessiya ID: " + this.session.getId());
            // Optionally send a PING or a keep-alive message if the server supports/requires it
            // For example: session.getAsyncRemote().sendPing(ByteBuffer.wrap("keepalive".getBytes()));
        }
    }

    // Your sendPeriodicMessage if needed, but ensure it checks session status
//    @Scheduled(fixedRate = 15000) // Example: send a status/gate check every 15 seconds
    public void sendPeriodicGateStatus() {
        if (session != null && session.isOpen()) {
            System.out.println("[" + LocalDateTime.now() + "] Davriy 'gate' xabarlari yuborilmoqda.");
            sendMessage("gate1"); // Example message
            sendMessage("gate2"); // Example message
        } else {
            System.out.println("[" + LocalDateTime.now() + "] Davriy xabar yuborib bo'lmadi, WebSocket ulanmagan.");
        }
    }
}