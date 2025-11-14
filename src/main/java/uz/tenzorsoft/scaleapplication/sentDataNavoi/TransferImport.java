package uz.tenzorsoft.scaleapplication.sentDataNavoi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)// fildlarda mos  emaslik  xatosi  kelmasligi uchun  qo`yildi.
public class TransferImport {
    private Long id;
    private String createdAt;
    private String productName;
    private Integer quantity;
    private String currentStatus;
    private List<String> statusChanges;//  statsularni ichdan  qidirish  uchun   kerak
}
