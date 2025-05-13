package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.ProductsEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductsEntity, Long> {
    List<ProductsEntity> findAllByIsDeletedFalse();

    Optional<ProductsEntity> findByIsSelectedTrue();

    List<ProductsEntity> findByIsDeletedFalseOrderByCreatedDesc();

    ProductsEntity findFirstByIsSelectedTrueAndIsDeletedFalse();

    List<ProductsEntity> findTop10ByIdOnServer(Long id);
}
