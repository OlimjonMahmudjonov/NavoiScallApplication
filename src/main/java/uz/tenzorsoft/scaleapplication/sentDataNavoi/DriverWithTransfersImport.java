package uz.tenzorsoft.scaleapplication.sentDataNavoi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriverWithTransfersImport {
    private DriverImport driver;
    private List<TransferImport> transfers;


}
