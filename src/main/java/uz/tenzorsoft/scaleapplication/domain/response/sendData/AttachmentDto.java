package uz.tenzorsoft.scaleapplication.domain.response.sendData;


import lombok.*;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class AttachmentDto {

    private Long localId;

    private Long serverId;

    private String originName;

    private Long size;

    private String type;

    private String contentType;

    private AttachStatus attachStatus;

    private Long webViewServerId;

    private byte[] bytes;
}
