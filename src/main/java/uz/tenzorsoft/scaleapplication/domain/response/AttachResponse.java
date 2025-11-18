package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class AttachResponse {
    private Long id;
    private String originalName;
    private String fileName;
    private Long size;
    private String type;
    private String contentType;
    private String path;
    private LocalDateTime createdAt;
}
