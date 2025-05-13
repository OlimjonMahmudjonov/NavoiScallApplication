package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "statuses")
public class StatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean controller = false;
    private boolean gate1 = false;
    private boolean gate2 = false;
    private boolean camera1 = false;
    private boolean camera2 = false;
    private boolean camera3 = false;
    private boolean sensor1 = false;
    private boolean sensor2 = false;
    private boolean sensor3 = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
