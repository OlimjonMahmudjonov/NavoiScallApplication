package uz.tenzorsoft.scaleapplication.service;

import javafx.animation.KeyValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.ProductsEntity;
import uz.tenzorsoft.scaleapplication.repository.ProductRepository;

import java.util.List;
import java.util.Map;

import static uz.tenzorsoft.scaleapplication.domain.Instances.currentUser;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<String> getAllProducts() {
        return productRepository.findAllByIsDeletedFalse().stream().map(ProductsEntity::getName).toList();
    }

    public ProductsEntity saveProduct(String productName) {
        ProductsEntity entity = new ProductsEntity();
        entity.setName(productName);
        entity.setScaleId(Instances.currentUser.getInternalScaleId());
        return productRepository.save(entity);
    }

//    public String getSelectedProduct() {
//        ProductsEntity product = productRepository.findByIsSelectedTrue().orElse(null);
//        if (product == null) {
//            List<ProductsEntity> list = productRepository.findByIsDeletedFalseOrderByCreatedDesc();
//            if (!list.isEmpty()) {
//                product = list.get(0);
//            }else{
//                throw new RuntimeException("Product not found");
//            }
//        }
//        return product.getName();
//    }

    public ProductsEntity getSelectedProduct() {
        return productRepository.findFirstByIsSelectedTrueAndIsDeletedFalse();
    }

    public List<ProductsEntity> getNotSentProducts() {
        return productRepository.findTop10ByIdOnServer(null);
    }


    public void dataSent(List<ProductsEntity> notSentProducts, Map<String, Long> productMap) {
        if (productMap == null || productMap.isEmpty()) {
            return;
        }
        notSentProducts.forEach(product -> {
            product.setIsSentToCloud(true);
            System.out.println("product = " + product);
            product.setIdOnServer(productMap.get(product.getId().toString()));
            productRepository.save(product);
        });
    }

    @Transactional
    public void updateSelectedProduct(String selectedProductName) {
        // Unselect all products
        List<ProductsEntity> allProducts = productRepository.findAll();
        for (ProductsEntity product : allProducts) {
            product.setIsSelected(product.getName().equals(selectedProductName));
        }
        // Save updated products
        productRepository.saveAll(allProducts);
    }

}
