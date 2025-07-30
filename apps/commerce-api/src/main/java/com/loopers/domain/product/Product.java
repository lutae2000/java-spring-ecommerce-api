package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "product")
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Product extends BaseEntity {

    private String code;
    private String name;
    private BigDecimal price;
    private int quantity;
    private String imgURL;
    private String description;
    @Transient
    private Brand brand;
    private String category1;
    private String category2;
    private String category3;
    private boolean useYn;
    private Long like;

    public Product(String code, String name, BigDecimal price, int quantity, String imgURL, String description, Brand brand, String category1, String category2, String category3, boolean useYn, Long like) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imgURL = imgURL;
        this.description = description;
        this.brand = brand;
        this.category1 = category1;
        this.category2 = category2;
        this.category3 = category3;
        this.useYn = useYn;
        this.like = like;
        validName(name);
        validPrice(price);
        validQuantity(quantity);
    }

    private void validName(String name){
        if(StringUtils.isEmpty(name)){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 이름은 필수입니다");
        }
    }

    private void validPrice(BigDecimal price){
        if(price == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 가격은 필수입니다");
        }

        if(price.compareTo(BigDecimal.ZERO) < 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 가격은 0보다 커야합니다");
        }
    }

    private void validQuantity(int quantity){
        if(price == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 수량은 필수입니다");
        }

        if(price.compareTo(BigDecimal.ZERO) < 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 수량은 0보다 커야합니다");
        }
    }
}
