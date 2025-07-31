package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.swing.text.html.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    /**
     * Brand 조회
     */
    public BrandInfo findByBrandCode(String brandCode){
        Brand brand = brandRepository.findByBrandCode(brandCode)
            .orElseThrow(() -> new NoSuchElementException("브랜드가 존재하지 않습니다"));

        return BrandInfo.from(brand);
    }

    /**
     * Brand 생성(Upsert)
     */
    public BrandInfo createBrand(BrandCommand.Create command){
        Brand brand = brandRepository.save(command.toEntity());
        return BrandInfo.from(brand);
    }

    /**
     * Brand 삭제
     */
    public void deleteBrand(String brandCode){

        boolean notExists = brandRepository.findByBrandCode(brandCode).isEmpty();

        if(notExists){
            throw new CoreException(ErrorType.NOT_FOUND, "삭제하려는 브랜드 코드가 없습니다");
        }
        brandRepository.deleteByBrandCode(brandCode);
    }
}
