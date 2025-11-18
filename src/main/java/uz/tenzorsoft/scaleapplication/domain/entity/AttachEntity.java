package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name = "attachments")
@ToString
public class AttachEntity extends BaseEntity {
    private String originalName;
    private String fileName;
    private Long size;
    private String type;
    private String contentType;
    private String path;
}
