package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name = "trucks")
public class TruckEntity extends BaseEntity {
    private String truckNumber;

    private String originalTruckNumber;

    @ManyToOne
    private ProductsEntity products;

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private List<TruckActionEntity> truckActions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TruckPhotosEntity> truckPhotos = new ArrayList<>();
    private LocalDateTime nextEntranceTime = LocalDateTime.now();

    private Boolean isFinished = false;

    private Boolean isSentToMyCoal = false;

}
