package uz.tenzorsoft.scaleapplication.domain.response.sendData.mycoal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ProductResponse {
    private String code;
    private String name;
    private Long id;
}
