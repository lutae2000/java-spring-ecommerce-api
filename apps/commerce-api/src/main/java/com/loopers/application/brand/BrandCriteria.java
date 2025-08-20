package com.loopers.application.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandCriteria {
    @NotBlank(message = "브랜드 코드는 필수입니다")
    private String code;

    @NotBlank(message = "브랜드 이름은 필수입니다")
    private String name;

    private String description;

    private String imgURL;

    @NotNull(message = "사용 여부는 필수입니다")
    private Boolean useYn;
}
