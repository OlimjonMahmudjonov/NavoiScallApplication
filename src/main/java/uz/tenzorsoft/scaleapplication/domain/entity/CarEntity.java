package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class CarEntity extends BaseEntity {

    private String model;
    private String number;
    private Long owner_pnfl;
    private LocalDateTime time;
    private Boolean status;
}
