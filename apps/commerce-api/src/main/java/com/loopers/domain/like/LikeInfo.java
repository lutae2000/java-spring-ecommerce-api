package com.loopers.domain.like;

import com.loopers.domain.brand.Brand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;


public record LikeInfo(
    String productId,
    String userId,
    Boolean likeYn,
    Long likesCount
) {

}
