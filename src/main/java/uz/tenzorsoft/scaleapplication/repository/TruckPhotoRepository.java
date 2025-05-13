package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TruckPhotoRepository extends JpaRepository<TruckPhotosEntity, Long> {

    List<TruckPhotosEntity> findByTruckPhotoOrderByCreatedAtDesc(AttachEntity attachEntity);

}
