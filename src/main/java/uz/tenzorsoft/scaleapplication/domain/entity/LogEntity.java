package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "logs")
public class LogEntity extends BaseEntity {
    private Long scaleId;
    private String truckNumber;
    @Column(length = 1024)
    private String event;

}
