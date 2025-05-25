package uz.tenzorsoft.scaleapplication.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Scale", // Optional: Qo'shimcha ma'lumot berish uchun
                version = "2.0", // OpenAPI versiyasi noto'g'ri emas, faqat ma'lumot sifatida
                description = "API Documentation"
        ),
        servers = {
                @Server(url = "https://omborim.uz/", description = "Prod Server"),
                @Server(url = "http://localhost:3333", description = "Local Server"),
                @Server(url = "http://192.168.68.115:3333", description = "IP Local Server"),
        }
)
public class SwaggerConfig {
}
