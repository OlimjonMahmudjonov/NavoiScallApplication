package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class WebViewDto {

    private Long localId;

    private Long serverId;

    private Long scaleId;

    private String carNumber;

    private LocalDateTime enteredAt;

    private Double enterWeight;

    private LocalDateTime exitedAt;

    private Double exitWeight;

    private String responsiblePerson;

    private String exitResponsiblePerson;
}
