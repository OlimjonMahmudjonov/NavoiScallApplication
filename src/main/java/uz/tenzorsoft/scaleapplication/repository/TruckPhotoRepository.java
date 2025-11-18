package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface TruckPhotoRepository extends JpaRepository<TruckPhotosEntity, Long> {

    List<TruckPhotosEntity> findByTruckPhotoOrderByCreatedAtDesc(AttachEntity attachEntity);

    @Query("SELECT p FROM truck_photos p WHERE p.attachStatus = :status AND p.truck.id = :truckId ORDER BY p.createdAt DESC")
    List<TruckPhotosEntity> findByAttachStatusAndTruckId(@Param("status") AttachStatus status, @Param("truckId") Long truckId);

    Optional<Object> findFirstByTruckOrderByIdDesc(TruckEntity truck);
}
