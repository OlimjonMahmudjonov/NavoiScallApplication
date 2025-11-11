package uz.tenzorsoft.scaleapplication.sentDataNavoi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Weight_OutDto {
    private Long navoiyAzotTransferId;
    private Double neto;
    private LocalDateTime netoTime;
    private Long localId;
    private Long scaleId;
}
