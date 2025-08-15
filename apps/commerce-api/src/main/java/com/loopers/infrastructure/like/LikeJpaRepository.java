package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeJpaRepository extends JpaRepository<Like, Long> {

    @Query("select l from Like l where l.userId = :userId and l.productId = :productId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Like> findByUserIdAndProductId(@Param("userId") String userId, @Param("productId") String productId);

    @Modifying
    @Query("delete from Like l where l.userId = :userId and l.productId = :productId")
    void deleteByUserIdAndProductId(@Param("userId") String userId, @Param("productId") String productId);

    @Query("select l from Like l where l.productId = :productId")
    Optional<Like> likeByProductId(@Param("productId") String productId);

    @Query("select count(l) > 0 from Like l where l.userId = :userId and l.productId = :productId")
    Boolean existsByUserIdAndProductId(@Param("userId") String userId, @Param("productId") String productId);

    /**
     * Optimistic locking - find by userId and productId without lock
     */
    @Query("select l from Like l where l.userId = :userId and l.productId = :productId")
    Optional<Like> findByUserIdAndProductIdOptimistic(@Param("userId") String userId, @Param("productId") String productId);
} 