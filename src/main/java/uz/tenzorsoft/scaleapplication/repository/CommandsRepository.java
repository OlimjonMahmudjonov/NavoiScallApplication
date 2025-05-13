package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.CommandsEntity;

import java.util.Optional;

@Repository
public interface CommandsRepository extends JpaRepository<CommandsEntity, Long> {
    Optional<CommandsEntity> findByScaleId(Long scaleId);
}