package uz.tenzorsoft.scaleapplication.sentDataNavoi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageSendDTO {
    private Long localId;
    private Long navoiyAzotTransferId;
    private String cameraName;
    private String pictureUrl;
}

