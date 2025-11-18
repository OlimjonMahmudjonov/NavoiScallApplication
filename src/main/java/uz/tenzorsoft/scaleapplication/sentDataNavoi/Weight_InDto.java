package uz.tenzorsoft.scaleapplication.sentDataNavoi;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Weight_InDto {
    private Long navoiyAzotTransferId;
    private Double tara;
    private LocalDateTime taraTime;
    private Long localId;
    private Long scaleId;
}
