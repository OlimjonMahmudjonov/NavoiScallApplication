package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")  // Bu to'g'ri - bu database jadval nomini belgilaydi
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserEntity extends BaseEntity {

    private String username;
    private String password;
    private String phoneNumber;
//    private Long scaleId;
    private Long externalScaleId;
    private Long internalScaleId;

}
