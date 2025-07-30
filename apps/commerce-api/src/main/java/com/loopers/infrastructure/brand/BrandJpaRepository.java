package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import java.util.Optional;

public interface BrandJpaRepository extends BrandRepository<Brand, Long> {
    Optional<Brand> findByBrandCode(String brandCode);
    Brand save(Brand brand);
    void deleteByBrandCode(String brandCode);
}
