package com.loopers.application.brand;

import com.loopers.domain.brand.BrandInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {
    private String code;
    private String name;
    private String description;
    private String imgURL;
    private Boolean useYn;

    public static BrandResponse from(BrandInfo brandInfo) {
        return BrandResponse.builder()
            .code(brandInfo.getCode())
            .name(brandInfo.getName())
            .description(brandInfo.getDescription())
            .imgURL(brandInfo.getImgURL())
            .useYn(brandInfo.getUseYn())
            .build();
    }
}
