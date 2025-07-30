package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandRepositoryImpl implements BrandJpaRepository{
    private final BrandJpaRepository brandJpaRepository;


    /**
     * 브랜드코드로 브랜드 정보 조회
     * @param brandCode
     * @return
     */
    @Override
    public Optional<Brand> findByBrandCode(String brandCode) {
        return brandJpaRepository.findByBrandCode(brandCode);
    }

    /**
     * 브랜드 저장
     * @param brand
     * @return
     */
    @Override
    public Brand save(Brand brand) {
        return brandJpaRepository.save(brand);
    }

    /**
     * 브랜드 삭제
     * @param brandCode
     */
    @Override
    public void deleteByBrandCode(String brandCode) {
        brandJpaRepository.deleteByBrandCode(brandCode);
    }
}
