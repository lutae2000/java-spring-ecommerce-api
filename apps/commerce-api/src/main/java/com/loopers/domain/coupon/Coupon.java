package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Builder
@Getter
@NoArgsConstructor
@Entity
@Table(name = "coupon")
public class Coupon extends BaseEntity {
    private String orderNo;
    private String eventId;
    private String couponNo;
    private String userId;
    private String couponName;
    private Boolean useYn;
    private DiscountType discountType;
    private BigDecimal discountRate;
    private BigDecimal discountRateLimitPrice;
    private BigDecimal discountAmount;
    private BigDecimal orderAmount;

    public Coupon(String orderNo, String eventId, String couponNo, String userId, String couponName,
        Boolean useYn, DiscountType discountType, BigDecimal discountRate,
        BigDecimal discountRateLimitPrice, BigDecimal discountAmount, BigDecimal orderAmount) {
        this.orderNo = orderNo;
        this.eventId = eventId;
        this.couponNo = couponNo;
        this.userId = userId;
        this.couponName = couponName;
        this.useYn = useYn;
        this.discountType = discountType;
        this.discountRate = discountRate;
        this.discountRateLimitPrice = discountRateLimitPrice;
        this.discountAmount = discountAmount;
        this.orderAmount = orderAmount;
        if(discountType == DiscountType.AMOUNT_DISCOUNT){
            validDiscountAmount();
        }
        if(discountType == DiscountType.RATIO_DISCOUNT){
            validDiscountRate();
        }
    }

    public void validEventId(){
        if(StringUtils.isEmpty(eventId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "행사 ID는 필수입니다");
        }
    }

    public void validDiscount(){
        if((ObjectUtils.isEmpty(discountRate) && discountType.equals( DiscountType.RATIO_DISCOUNT))
            || (ObjectUtils.isEmpty(discountAmount) && discountType.equals( DiscountType.AMOUNT_DISCOUNT) ) ){
            throw new CoreException(ErrorType.BAD_REQUEST, "할인율 혹은 할인금액은 필수입니다");
        }
    }

    public void useYnValid(){
        if(ObjectUtils.isEmpty(useYn)){
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 사용여부는 필수값입니다");
        }
    }

    public void validDiscountRate(){
        if (discountType == DiscountType.RATIO_DISCOUNT) {
            if(ObjectUtils.isEmpty(discountRate)) {
                throw new CoreException(ErrorType.BAD_REQUEST, "할인율은 필수값 입니다");
            }
            if(discountRate.compareTo(BigDecimal.ZERO) <= 0 || discountRate.compareTo(BigDecimal.ONE) >= 0 ){
                throw new CoreException(ErrorType.BAD_REQUEST, "할인율은 0보다 크고 1보다 작아야 합니다");
            }
            if(ObjectUtils.isEmpty(discountRateLimitPrice)){
                throw new CoreException(ErrorType.BAD_REQUEST, "할인율 상한금액은 필수입니다");
            }
        };
    }

    public void validDiscountAmount(){
        if(ObjectUtils.isNotEmpty(discountType) && discountType.equals( DiscountType.AMOUNT_DISCOUNT) ){
            if(ObjectUtils.isEmpty(discountAmount)|| discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "할인 금액은 필수값 입니다");
            }
        }
    }

    // 쿠폰 할인금액 계산
    public BigDecimal calculateDiscount(BigDecimal orderAmount){
        switch (discountType){
            case RATIO_DISCOUNT -> {return discountRatePrice(orderAmount);}
            case AMOUNT_DISCOUNT -> {return discountAmountPrice(orderAmount);}
            default -> {return BigDecimal.ZERO;}
        }
    }

    //정률 할인 금액 계산
    private BigDecimal discountRatePrice(BigDecimal orderAmount){
        if(discountRateLimitPrice == null || discountRateLimitPrice.compareTo(BigDecimal.ZERO) <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "정률 할인금액이 비정상입니다");
        }
        BigDecimal rawDiscount = orderAmount.multiply(discountRate);
        if(discountRateLimitPrice != null && rawDiscount.compareTo(discountRateLimitPrice) > 0){
            return discountRateLimitPrice;
        }
        return rawDiscount;
    }

    //정액 할인 금액 계산
    private BigDecimal discountAmountPrice(BigDecimal orderAmount){
        if(discountAmount == null)
            throw new CoreException(ErrorType.BAD_REQUEST, "정액 할인 금액이 없습니다");
        if(discountAmount.equals(BigDecimal.ZERO))
            throw new CoreException(ErrorType.BAD_REQUEST, "정액 할인 금액은 0원이 될수 없습니다");
        return orderAmount.min(discountAmount);
    }
}
