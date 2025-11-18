package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class AllSendResponse {

    private List<AttachmentDto> attachmentDto;

    private List<WebViewDto> webViewDto;
}
