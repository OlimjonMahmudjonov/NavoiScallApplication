package uz.tenzorsoft.scaleapplication.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TruckRequest {
    private String truckNumber;

}
