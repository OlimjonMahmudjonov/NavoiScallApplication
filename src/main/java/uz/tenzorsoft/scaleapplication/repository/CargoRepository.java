package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.tenzorsoft.scaleapplication.domain.entity.CargoEntity;

import java.util.List;
import java.util.Optional;

public interface CargoRepository extends JpaRepository<CargoEntity, Long> {
    List<CargoEntity> findTop10ByIsSentToCloud(boolean isSent);

    Optional<CargoEntity> findByScaleId(Long cargoId);

    Optional<CargoEntity> findByTruckId(Long truckId);
}
