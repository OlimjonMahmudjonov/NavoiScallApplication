package uz.tenzorsoft.scaleapplication.sentDataNavoi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriverImport {
    private Long id;
    private String name;
    private String transportNumber;
    private String transportModel;
    private String phone;
    private String enterDate;
}
