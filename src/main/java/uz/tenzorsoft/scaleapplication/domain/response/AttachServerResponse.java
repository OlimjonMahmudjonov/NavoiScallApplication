package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AttachServerResponse {
    private Long id;
    private String originalName;
    private String fileName;
    private Long size;
    private String type;
    private String contentType;
    private String path;
    private LocalDateTime createdAt;
}
