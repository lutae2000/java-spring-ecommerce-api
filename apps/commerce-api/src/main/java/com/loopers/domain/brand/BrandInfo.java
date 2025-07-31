package com.loopers.domain.brand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BrandInfo {

    private String code;
    private String name;
    private String description;
    private String imgURL;
    private Boolean useYn;

    public static BrandInfo from(Brand brand){
        return new BrandInfo(
            brand.getCode(),
            brand.getName(),
            brand.getDescription(),
            brand.getImgURL(),
            brand.getUseYn()
        );
    }

    public record Create(String code, String name, String description, String imgURL, Boolean useYn){

        public static class CreateBuilder{
            private String code;
            private String name;
            private String description;
            private String imgURL;
            private Boolean useYn;
        }
    }
}
