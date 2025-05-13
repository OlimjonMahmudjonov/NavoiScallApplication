package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByPhoneNumberAndPassword(String phoneNumber, String password);

    List<UserEntity> findTop10ByIsSentToCloud(boolean isSent);

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
}
