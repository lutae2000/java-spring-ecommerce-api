package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.SortBy;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJPARepository extends JpaRepository<Product, Long> {

    /**
     * 물품조회(비관락)
     * @param productId
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.code = :productId")
    Product findProductByCodeForUpdate(@Param("productId") String productId);

    /**
     * 물품 저장
     * @param product
     * @return
     */
    Product save(Product product);

    /**
     * 물품 상세
     * @param code
     * @return
     */
    @Query("select p from Product p where p.code = :code")
    Product findByCode(@Param("code") String code);

}
