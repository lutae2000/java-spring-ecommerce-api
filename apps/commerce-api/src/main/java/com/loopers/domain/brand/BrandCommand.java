package com.loopers.domain.brand;

import lombok.Builder;

public class BrandCommand {
    private String brandCode;
    private String brandName;
    private String brandDesc;
    private String brandImg;
    private Boolean useYn;

    @Builder
    public record Create(String brandCode, String brandName, String brandDesc, String brandImg, Boolean useYn){
        public Brand toEntity(){
            return new Brand(brandCode, brandName, brandDesc, brandImg, useYn);
        }
    }
}
