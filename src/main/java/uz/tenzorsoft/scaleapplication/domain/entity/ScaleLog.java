package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Length;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name="scale_data_logs")
public class ScaleLog extends BaseEntity{
    @Column(columnDefinition = "text")
    private String data;
    @Column(length=1024)
    private String convertedData;
}
