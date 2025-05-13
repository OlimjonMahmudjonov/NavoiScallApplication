package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity(name = "products")
public class ProductsEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @Column(columnDefinition = "boolean default true")
    private Boolean isSelected = true;

    private Long scaleId;

    @CreationTimestamp
    private LocalDateTime created;

}
