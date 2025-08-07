package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Builder
@Getter
@NoArgsConstructor
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
    private BigDecimal discountAmountLimitPrice;
    private BigDecimal discountAmount;

    public Coupon(String orderNo, String eventId, String couponNo, String userId, String couponName, Boolean useYn,
        DiscountType discountType, BigDecimal discountRate, BigDecimal discountRateLimitPrice, BigDecimal discountAmountLimitPrice,
        BigDecimal discountAmount) {
        this.orderNo = orderNo;
        this.eventId = eventId;
        this.couponNo = couponNo;
        this.userId = userId;
        this.couponName = couponName;
        this.useYn = useYn;
        this.discountType = discountType;
        this.discountRate = discountRate;
        this.discountRateLimitPrice = discountRateLimitPrice;
        this.discountAmountLimitPrice = discountAmountLimitPrice;
        this.discountAmount = discountAmount;
        validEventId();
        validDiscount();
        useYnValid();
        validDiscountRate();
        validDiscountAmount();
    }

    public void validEventId(){
        if(StringUtils.isEmpty(eventId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "행사 ID는 필수입니다");
        }
    }

    public void validDiscount(){
        if(ObjectUtils.isEmpty(discountRate) | ObjectUtils.isEmpty(discountAmount)){
            throw new CoreException(ErrorType.BAD_REQUEST, "할인율 혹은 할인금액은 필수입니다");
        }
    }

    public void useYnValid(){
        if(ObjectUtils.isEmpty(useYn)){
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 사용여부는 필수값입니다");
        }
    }

    public void validDiscountRate(){

        if(ObjectUtils.isEmpty(discountRate)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인율은 필수값 입니다");
        }
        if(discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(BigDecimal.ONE) > 0 ){
            throw new CoreException(ErrorType.BAD_REQUEST, "할인율은 0보다 크고 1보다 작아야 합니다");
        }
        if(ObjectUtils.isEmpty(discountRateLimitPrice)){
            throw new CoreException(ErrorType.BAD_REQUEST, "할인율 상한금액은 필수입니다");
        }

    }

    public void validDiscountAmount(){
        if(ObjectUtils.isEmpty(discountAmount) || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 금액은 필수값 입니다");
        }
        if(ObjectUtils.isEmpty(discountAmountLimitPrice)){
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 상한금액은 필수입니다");
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
            return BigDecimal.ZERO;
        }
        BigDecimal rawDiscount = orderAmount.multiply(discountRate);
        if(discountRateLimitPrice != null && rawDiscount.compareTo(discountRateLimitPrice) > 0){
            return discountRateLimitPrice;
        }
        return rawDiscount;
    }

    //정액 할인 금액 계산
    private BigDecimal discountAmountPrice(BigDecimal orderAmount){
        if(discountAmountLimitPrice == null) return BigDecimal.ZERO;
        return orderAmount.min(discountAmountLimitPrice);
    }
}
