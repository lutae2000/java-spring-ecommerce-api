package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import java.util.Optional;

public interface BrandRepository {
    Optional<Brand> findByBrandCode(String brandCode);
    Brand save(Brand brand);
    void deleteByBrandCode(String brandCode);
}
