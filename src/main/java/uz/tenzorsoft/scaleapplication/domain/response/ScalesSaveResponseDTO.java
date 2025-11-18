package uz.tenzorsoft.scaleapplication.domain.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScalesSaveResponseDTO {
    private Long id;
    private UUID scaleReadingId;
    private boolean status;
}
