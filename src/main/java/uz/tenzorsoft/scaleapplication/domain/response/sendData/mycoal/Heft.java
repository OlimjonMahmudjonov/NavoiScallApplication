package uz.tenzorsoft.scaleapplication.domain.response.sendData.mycoal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Heft {
    private Double brutto;
    private Double tara;
    private Double netto_arrival;
    private Double netto_cost;
}
