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
public class TransferImport {
    private Long id;
    private String createdAt;
    private String currentStatus;
    private String productName;
    private Integer quantity;
    private List<String> statusChanges;
}
