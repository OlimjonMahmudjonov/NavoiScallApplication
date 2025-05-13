package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachRepository extends JpaRepository<AttachEntity, Long> {

    @Query(value = "SELECT * FROM attachments WHERE is_sent_to_cloud = false and created_at <= '2025-01-31' ", nativeQuery = true)
    List<AttachEntity> findByIsSentToCloud(boolean isSent);

    Optional<AttachEntity> findByFileName(String fileName);

    List<AttachEntity> findAllById(Long id);
}
