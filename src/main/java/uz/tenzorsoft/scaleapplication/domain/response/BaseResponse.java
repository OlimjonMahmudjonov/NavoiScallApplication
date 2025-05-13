package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class BaseResponse {
    private Long id;
    private LocalDateTime createdAt;
    private Long idOnServer;
}
