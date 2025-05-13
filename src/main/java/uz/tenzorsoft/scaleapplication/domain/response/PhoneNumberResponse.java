package uz.tenzorsoft.scaleapplication.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Игнорируем лишние поля
public class PhoneNumberResponse {
    private String message;
    private String status;
}
