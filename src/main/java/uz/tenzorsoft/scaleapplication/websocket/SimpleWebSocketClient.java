package uz.tenzorsoft.scaleapplication.websocket;

import jakarta.websocket.*;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.net.URI;
import java.util.Scanner;

@ClientEndpoint
public class SimpleWebSocketClient {

    private Session userSession = null;

    public SimpleWebSocketClient(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (DeploymentException | IOException e) {
            System.err.println("Ulanishda xatolik: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("Serverga ulanildi: " + userSession.getId());
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("Serverdan uzildi: " + userSession.getId() + ", Sabab: " + reason.getReasonPhrase());
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Serverdan kelgan xabar: " + message);
        // Bu yerda server yuborgan ShlagbaunDto.toString() qiymati keladi
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Xatolik yuz berdi: " + throwable.getMessage());
        throwable.printStackTrace();
    }

    @Scheduled(fixedRate = 100)
    public void sendMessage(String message) {
        if (this.userSession != null && this.userSession.isOpen()) {
            try {
                this.userSession.getBasicRemote().sendText(message);
                System.out.println("Serverga yuborilgan xabar: " + message);
            } catch (IOException e) {
                System.err.println("Xabar yuborishda xatolik: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("WebSocket ulanishi mavjud emas yoki yopiq.");
        }
    }

    public boolean isConnected() {
        return this.userSession != null && this.userSession.isOpen();
    }

    public void closeConnection() {
        try {
            if (this.userSession != null && this.userSession.isOpen()) {
                this.userSession.close();
            }
        } catch (IOException e) {
            System.err.println("Ulanishni yopishda xatolik: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // WebSocket server manzili (Spring Boot ilovangiz ishlayotgan manzil va endpoint)
        // Masalan, agar serveringiz localhost:8080 da va WebSocket endpoint /ws bo'lsa:
//        String serverUriStr = "ws://localhost:8080/ws"; // BU YERNI O'ZGARTIRING!

        // Agar serveringiz boshqa IP va portda bo'lsa, shunga moslang.
        // Masalan, oldingi kodingizda "ws://192.168.68.134:8880/ws" ishlatilgan edi.
         String serverUriStr = "ws://192.168.68.134:8880/ws";

        try {
            URI serverUri = URI.create(serverUriStr);
            SimpleWebSocketClient client = new SimpleWebSocketClient(serverUri);

            // Ulanishni kutish (oddiy usul)
            int retries = 15;
            while (!client.isConnected() && retries > 0) {
                System.out.println("Serverga ulanish kutilmoqda...");
                Thread.sleep(1000); // 1 soniya kutish
                retries--;
            }

            if (!client.isConnected()) {
                System.err.println("Serverga ulanib bo'lmadi. Dastur to'xtatilmoqda.");
                return;
            }

//             Serverga test xabar yuborish
            client.sendMessage("gate");
            Thread.sleep(500); // Javob kelishini kutish

//            client.sendMessage("/openGate2");
//            Thread.sleep(500);

            // Foydalanuvchidan xabar kiritishni so'rash
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nServerga xabar yuborish uchun matn kiriting (chiqish uchun 'exit' deb yozing):");
            String input;
            while (true) {
                input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                if (!input.trim().isEmpty()) {
                    client.sendMessage(input);
                }
            }

            // Ulanishni yopish
            client.closeConnection();
            System.out.println("Dastur tugadi.");

        } catch (Exception e) {
            System.err.println("Klient ishga tushirishda umumiy xatolik: " + e.getMessage());
            e.printStackTrace();
        }
    }
}