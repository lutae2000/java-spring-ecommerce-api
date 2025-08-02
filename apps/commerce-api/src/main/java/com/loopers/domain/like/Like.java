package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.utils.StringUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "likes")
@Builder
@NoArgsConstructor
@Getter
public class Like extends BaseEntity {

    private String productId;
    private String userId;
    private Boolean likeYn;
    private Long likesCount;

    public Like(String productId, String userId, Boolean likeYn, Long likesCount) {
        this.productId = productId;
        this.userId = userId;
        this.likeYn = likeYn;
        this.likesCount = ObjectUtils.isEmpty(likesCount) ? 0L : likesCount;
        validLikeYn(likeYn);
        validProductId(productId);
        validUserId(userId);
    }

    public Like(String productId, String userId, Boolean likeYn) {
        this.productId = productId;
        this.userId = userId;
        this.likeYn = likeYn;
        validLikeYn(likeYn);
        validProductId(productId);
        validUserId(userId);
    }

    public void validProductId(String productId){
        if(StringUtils.isEmpty(productId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "물품 코드는 필수값 입니다");
        }
    }

    public void validUserId(String userId){
        if(StringUtils.isEmpty(userId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "회원ID는 필수값 입니다");
        }
    }

    public void validLikeYn(Boolean likeYn){
        if(ObjectUtils.isEmpty(likeYn)){
            throw new CoreException(ErrorType.BAD_REQUEST, "좋아요 플래그는 필수값 입니다");
        }
    }
}
