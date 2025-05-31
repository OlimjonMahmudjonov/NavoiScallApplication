package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class ShlagbaumEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    private Long id;        // Serverdagi ShlagbaunDto identifikatori (buyruq ID si sifatida ishlatilishi mumkin)
    private Boolean status; // Shlagbaumni ochish (true) yoki yopish (false) holati
    private Integer number; // Shlagbaum raqami (masalan, 1-shlagbaum, 2-shlagbaum)
    private Long serverid;
    private Long localid;
    private Long commandId;
}
