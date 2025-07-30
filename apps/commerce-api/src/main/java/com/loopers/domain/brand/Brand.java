package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.utils.StringUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;


@Entity
@Table(name = "brand")
@NoArgsConstructor
@Getter
@Setter
public class Brand extends BaseEntity {
    private String code;
    private String name;
    private String description;
    private String imgURL;
    private Boolean useYn;

    @Builder
    public Brand(String code, String name, String description, String imgURL, boolean useYn) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.imgURL = imgURL;
        this.useYn = useYn;
        validName(name);
        validDescription(description);
    }

    private void validName(String name){
        if(StringUtils.isEmpty(name)){
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 이름은 필수입니다");
        }
    }

    private void validDescription(String description){
        if(StringUtils.isEmpty(description)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 설명은 필수입니다");
        }
    }
}
