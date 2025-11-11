package uz.tenzorsoft.scaleapplication.sentDataNavoi;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
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

    @Value("${spring.basic-auth.login}")
    private String login;

    @Value("${spring.basic-auth.password}")
    private String password;

    private String refreshTokenUrl;
    private String newToken;
    private long tokenExpireTime;

    @PostConstruct
    public void init() {
        this.refreshTokenUrl = baseUrl + "/Auth/login";
        log.info(" Token URL o‘rnatildi: {}", refreshTokenUrl);
        refreshToken(); // dastur ishga tushganda bir marta token olish
    }

    /**
     * Tokenni olish — agar mavjud bo‘lmasa yoki muddati tugagan bo‘lsa, yangidan oladi.
     */
    public synchronized String getNewToken() {
        if (newToken == null || System.currentTimeMillis() >= tokenExpireTime) {
            refreshToken();
        }
        return newToken;
    }

    /**
     * Token muddati tugashidan 1 daqiqa oldin avtomatik yangilanishi uchun
     */
    @Scheduled(fixedDelay = 60000) // har 60 sekundda tekshiriladi
    public void autoRefresh() {
        if (newToken != null && System.currentTimeMillis() + 60000 >= tokenExpireTime) {
            log.info(" Token muddati tugash arafasida, yangilanmoqda...");
            refreshToken();
        }
    }

    /**
     * Yangi token olish metodi
     */
    @Synchronized
    private void refreshToken() {
        Map<String, String> body = new HashMap<>();
        body.put("Username", login);
        body.put("Password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    refreshTokenUrl, HttpMethod.POST, request, Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().get("token") != null) {

                this.newToken = response.getBody().get("token").toString();
                this.tokenExpireTime = System.currentTimeMillis() + (3600 * 1000); // 1 soat

                log.info(" Yangi token olindi: {}", newToken.substring(0, Math.min(10, newToken.length())) + "...");
            } else {
                throw new RuntimeException(" Token yangilanmadi: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Token olishda xatolik: {}", e.getMessage());
        }
    }
}
