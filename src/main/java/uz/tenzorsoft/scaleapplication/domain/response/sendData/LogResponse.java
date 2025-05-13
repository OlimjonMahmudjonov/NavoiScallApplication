package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LogResponse {

    private List<LogEntity> dtoList;

}
