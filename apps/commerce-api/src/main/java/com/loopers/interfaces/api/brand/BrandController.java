package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandCreateRequest;
import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandResponse;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Slf4j
public class BrandController {
    private final BrandFacade brandFacade;

    /**
     * 브랜드 생성 API
     */
    @PostMapping
    public ApiResponse<BrandResponse> createBrand(
        @RequestBody BrandCreateRequest request) {
        log.info("브랜드 생성 요청: {}", request);

            var brandInfo = brandFacade.createBrand(request);
            var response = BrandResponse.from(brandInfo);

        return ApiResponse.success(response);
    }

    /**
     * 브랜드 조회 API
     */
/*    @GetMapping("/{brandCode}")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrand(@PathVariable String brandCode) {
        log.info("브랜드 조회 요청: {}", brandCode);

        try {
            var brandInfo = brandFacade.getBrand(brandCode);
            var response = BrandResponse.from(brandInfo);

            return ResponseEntity.ok(ApiResponse.success(response, "브랜드 조회 성공"));

        } catch (Exception e) {
            log.error("브랜드 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }*/

    /**
     * 브랜드 삭제 API
     */
    @DeleteMapping("/{brandCode}")
    public void deleteBrand(@PathVariable String brandCode) {
        log.info("브랜드 삭제 요청: {}", brandCode);
            brandFacade.deleteBrand(brandCode);
            log.info("브랜드 삭제 성공: {}", brandCode);
    }
}
