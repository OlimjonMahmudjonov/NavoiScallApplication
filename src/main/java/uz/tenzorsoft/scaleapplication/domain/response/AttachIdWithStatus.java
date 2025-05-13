package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AttachIdWithStatus {
    private Long id;
    private AttachStatus status;
}
