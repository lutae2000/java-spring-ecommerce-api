package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;

    /**
     * Brand 조회
     */
    public Optional<Brand> findByBrandCode(String brandCode){
        return brandRepository.findByBrandCode(brandCode);
    }

    /**
     * Brand 생성(Upsert)
     */
    public Brand createBrand(Brand brand){

        return brandRepository.save(brand);
    }

    /**
     * Brand 삭제
     */
    public void deleteBrand(String brandCode){

        if(findByBrandCode(brandCode).isPresent()){
            throw new CoreException(ErrorType.NOT_FOUND, "삭제하려는 브랜드 코드가 없습니다");
        }
        brandRepository.deleteByBrandCode(brandCode);
    }
}
