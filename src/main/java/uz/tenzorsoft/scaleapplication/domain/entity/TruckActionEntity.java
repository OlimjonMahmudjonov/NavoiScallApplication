package uz.tenzorsoft.scaleapplication.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import uz.tenzorsoft.scaleapplication.domain.enumerators.ActionStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name = "truck_actions")
public class TruckActionEntity extends BaseEntity {
    @ColumnDefault("0.0")
    private Double weight = 0.0;

    @Enumerated(EnumType.STRING)
    private TruckAction action = TruckAction.NO_ACTION;

    @Enumerated(EnumType.STRING)
    private ActionStatus actionStatus = ActionStatus.NEW;

    @ManyToOne
    private UserEntity onDuty;
    private Boolean isSentToMyCoal = false;

}
