package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Product extends BaseEntity {

    private String code;
    private String name;
    private BigDecimal price;
    private Long quantity;
    private String imgURL;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_code")
    private Brand brand;

    private String category1;
    private String category2;
    private String category3;
    private Boolean useYn;

    /**
     * ProductCommand로부터 Product 생성
     */
    public static Product from(ProductCommand productCommand) {
        validateProductCommand(productCommand);

        return Product.builder()
            .code(productCommand.code())
            .name(productCommand.name())
            .price(productCommand.price())
            .quantity(productCommand.quantity())
            .imgURL(productCommand.imgURL())
            .description(productCommand.description())
            .brand(null) // Brand는 나중에 설정
            .category1(productCommand.category1())
            .category2(productCommand.category2())
            .category3(productCommand.category3())
            .useYn(true)
            .build();
    }

    /**
     * ProductCommand와 Brand로부터 Product 생성
     */
    public static Product from(ProductCommand productCommand, Brand brand) {
        validateProductCommand(productCommand);
        validateBrand(brand);

        return Product.builder()
            .code(productCommand.code())
            .name(productCommand.name())
            .price(productCommand.price())
            .quantity(productCommand.quantity())
            .imgURL(productCommand.imgURL())
            .description(productCommand.description())
            .brand(brand)
            .category1(productCommand.category1())
            .category2(productCommand.category2())
            .category3(productCommand.category3())
            .useYn(true)
            .build();
    }

    /**
     * 기본 정보로 Product 생성
     */
    public static Product create(String code, String name, BigDecimal price, Long quantity,
        String imgURL, String description, String category1,
        String category2, String category3) {
        validateBasicInfo(code, name, price, quantity);

        return Product.builder()
            .code(code)
            .name(name)
            .price(price)
            .quantity(quantity)
            .imgURL(imgURL)
            .description(description)
            .brand(null)
            .category1(category1)
            .category2(category2)
            .category3(category3)
            .useYn(true)
            .build();
    }

    /**
     * Brand와 함께 Product 생성
     */
    public static Product createWithBrand(String code, String name, BigDecimal price, Long quantity,
        String imgURL, String description, Brand brand,
        String category1, String category2, String category3) {
        validateBasicInfo(code, name, price, quantity);
        validateBrand(brand);

        return Product.builder()
            .code(code)
            .name(name)
            .price(price)
            .quantity(quantity)
            .imgURL(imgURL)
            .description(description)
            .brand(brand)
            .category1(category1)
            .category2(category2)
            .category3(category3)
            .useYn(true)
            .build();
    }

    // ===== 검증 메소드들 =====

    private static void validateProductCommand(ProductCommand productCommand) {
        if (productCommand == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "ProductCommand는 null일 수 없습니다");
        }
        validateBasicInfo(
            productCommand.code(),
            productCommand.name(),
            productCommand.price(),
            productCommand.quantity()
        );
    }

    private static void validateBasicInfo(String code, String name, BigDecimal price, Long quantity) {
        validateCode(code);
        validateName(name);
        validatePrice(price);
        validateQuantity(quantity);
    }

    private static void validateCode(String code) {
        if (StringUtils.isEmpty(code)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 코드는 필수값입니다");
        }
    }

    private static void validateName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 이름은 필수값입니다");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 가격은 필수입니다");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 가격은 0보다 커야합니다");
        }
    }

    private static void validateQuantity(Long quantity) {
        if (quantity == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 수량은 필수입니다");
        }
        if (quantity < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 수량은 0보다 커야합니다");
        }
    }

    private static void validateBrand(Brand brand) {
        if (brand == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "Brand는 null일 수 없습니다");
        }
        if (StringUtils.isEmpty(brand.getCode())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "Brand 코드는 필수입니다");
        }
    }

    // ===== 비즈니스 메소드들 =====

    /**
     * Brand 설정
     */
    public void setBrand(Brand brand) {
        validateBrand(brand);
        this.brand = brand;
    }


    /**
     * 재고 차감
     */
    public void decreaseQuantity(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "차감할 수량은 0보다 커야합니다");
        }
        if (this.quantity < amount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다. 현재: " + this.quantity + ", 요청: " + amount);
        }
        this.quantity -= amount;
    }

}
