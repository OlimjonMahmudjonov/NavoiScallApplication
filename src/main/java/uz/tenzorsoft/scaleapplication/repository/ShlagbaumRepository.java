package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.ShlagbaumEntity;

import java.util.Optional;

@Repository
public interface ShlagbaumRepository extends JpaRepository<ShlagbaumEntity, Long> {
    Optional<ShlagbaumEntity> findByCommandId(Long commandId);
}
