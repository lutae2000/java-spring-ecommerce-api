package com.loopers.application.brand;

import com.loopers.domain.brand.BrandCommand;
import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandFacade {
    private final BrandService brandService;

    /**
     * 브랜드 생성
     */
    public BrandInfo createBrand(BrandCreateRequest request) {
        BrandCommand.Create command = BrandCommand.Create.builder()
            .brandCode(request.getCode())
            .brandName(request.getName())
            .brandDesc(request.getDescription())
            .brandImg(request.getImgURL())
            .useYn(request.getUseYn())
            .build();

        return brandService.createBrand(command);
    }

    /**
     * 브랜드 조회
     */
    public BrandInfo getBrand(String brandCode) {
        return brandService.findByBrandCode(brandCode);
    }

    /**
     * 브랜드 삭제
     */
    public void deleteBrand(String brandCode) {
        brandService.deleteBrand(brandCode);
    }
}
