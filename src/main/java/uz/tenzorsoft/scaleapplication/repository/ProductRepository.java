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

    ProductsEntity findByName(String name);

    Optional<ProductsEntity> findByNameIgnoreCase(String trim);

    // Yangi methodlar - fallback logic uchun
    Optional<ProductsEntity> findFirstByOrderByCreatedAtDesc();  // Oxirgi product
    Optional<ProductsEntity> findFirstByOrderByCreatedAtAsc();   // Birinchi product

    // Yoki ID bo'yicha eng katta/kichik
    Optional<ProductsEntity> findFirstByOrderByIdDesc();  // Oxirgi ID
    Optional<ProductsEntity> findFirstByOrderByIdAsc();   // Birinchi ID
}
