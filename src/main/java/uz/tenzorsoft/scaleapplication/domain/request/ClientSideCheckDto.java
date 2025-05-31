package uz.tenzorsoft.scaleapplication.domain.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientSideCheckDto {
    private Long id;
    private Boolean success;
    private String comment;
    private Integer number;     // Agar bu maydonlarni yubormoqchi bo'lmasangiz, null bo'lib qoladi
    private Long localId;      // Agar bu maydonlarni yubormoqchi bo'lmasangiz, null bo'lib qoladi
}
