package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CheckCommandsDto {
    private Long id;
    private String comment;
    private boolean finished;
}
