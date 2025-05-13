package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ProductResponses {
    private Long id;
    private String name;
    private Long scaleId;
}
