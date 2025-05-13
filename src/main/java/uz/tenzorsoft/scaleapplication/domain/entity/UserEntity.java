package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "users")
public class UserEntity extends BaseEntity {

    private String username;
    private String password;
    private String phoneNumber;
//    private Long scaleId;
    private Long externalScaleId;
    private Long internalScaleId;

}
