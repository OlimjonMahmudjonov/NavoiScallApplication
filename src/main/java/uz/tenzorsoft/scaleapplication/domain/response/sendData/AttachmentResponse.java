package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponse extends BaseResponse {

    private Long truckId;

    private String originName;
    private Long size;
    private String type;
    private String contentType;
    private String localPath;
    private String cloudPath;
    private AttachStatus attachStatus;
    private LocalDateTime createdDate;

    private byte[] bytes;
}
