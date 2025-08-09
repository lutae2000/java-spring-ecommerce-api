package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJPARepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.code = :productId")
    Product findProductByCodeForUpdate(@Param("productId") String productId);

    Product save(Product product);
}
