package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

@Entity
@Table(name = "likes", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = {"productId", "userId"}))
@Builder
@NoArgsConstructor
@Getter
public class Like extends BaseEntity {

    @Id
    private String productId;
    private String userId;

    @Version
    private Long version;

    public Like(String productId, String userId, Long likesCount, Long version) {
        this.productId = productId;
        this.userId = userId;
        this.version = version;
        validProductId(productId);
        validUserId(userId);
    }

    public Like(String productId, String userId, Long likesCount) {
        this.productId = productId;
        this.userId = userId;
        validProductId(productId);
        validUserId(userId);
    }

    public Like(String productId, String userId) {
        this.productId = productId;
        this.userId = userId;
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
}
