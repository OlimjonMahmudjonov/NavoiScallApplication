package uz.tenzorsoft.scaleapplication.domain.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ShlagbaunDto {

    private Long id;        // Serverdagi ShlagbaunDto identifikatori (buyruq ID si sifatida ishlatilishi mumkin)
    private Boolean status; // Shlagbaumni ochish (true) yoki yopish (false) holati
    private Integer number; // Shlagbaum raqami (masalan, 1-shlagbaum, 2-shlagbaum)
    private Long localid;

}
