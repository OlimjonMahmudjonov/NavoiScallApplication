package uz.tenzorsoft.scaleapplication.sentDataNavoi;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshToken {

    private final RestTemplate restTemplate;

    @Value("${spring.url}")
    private String baseUrl;

    @Value("${spring.username}")
    private String login;

    @Value("${spring.password}")
    private String password;

    private String refreshTokenUrl;
    private String newToken;
    private long tokenExpireTime;
    private final Object lock = new Object();

    @PostConstruct
    public void init() {
        this.refreshTokenUrl = baseUrl + "/auth/login";
        log.info("Token URL o‘rnatildi: {}", refreshTokenUrl);
        refreshToken(); // dastur boshida
    }

    public String getNewToken() {
        synchronized (lock) {
            if (newToken == null || System.currentTimeMillis() >= tokenExpireTime) {
                refreshToken();
            }
            if (newToken == null) {
                throw new IllegalStateException("Token autentifikatsiyasi muvaffaqiyatsiz");
            }
            return newToken;
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void autoRefresh() {
        synchronized (lock) {
            if (newToken != null && System.currentTimeMillis() + 60000 >= tokenExpireTime) {
                log.info("Token muddati tugamoqda, yangilanmoqda...");
                refreshToken();
            }
        }
    }

    private void refreshToken() {
        synchronized (lock) {
            Map<String, String> body = new HashMap<>();
            body.put("username", login);
            body.put("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        refreshTokenUrl, HttpMethod.POST, request, Map.class
                );

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    throw new RuntimeException("HTTP xato: " + response.getStatusCode());
                }

                Map<String, Object> respBody = response.getBody();
                String token = (String) respBody.get("token");
                Long expiresIn = respBody.containsKey("expiresIn") ? ((Number) respBody.get("expiresIn")).longValue() : 3600L;

                if (token == null || token.isEmpty()) {
                    throw new RuntimeException("Token javobda yo'q");
                }

                this.newToken = token;
                this.tokenExpireTime = System.currentTimeMillis() + (expiresIn * 1000);

                log.info("Yangi token olindi ({} soniya): {}", expiresIn, token.substring(0, Math.min(10, token.length())) + "...");

            } catch (Exception e) {
                log.error("Token olishda xatolik: {}", e.getMessage(), e);
                this.newToken = null; // xavfsizlik
                throw new RuntimeException("Token autentifikatsiyasi muvaffaqiyatsiz", e);
            }
        }
    }
}
