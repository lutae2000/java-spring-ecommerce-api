package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
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

    @Builder
    public Like(String productId, String userId, Boolean likeYn) {
        this.productId = productIdValid(productId);
        this.userId = userId;
        this.likeYn = likeYn;
    }

    private String productIdValid(String productId){
        if(StringUtils.isEmpty(productId)){
            return StringUtil.generateCode(7);
        }
        return productId;
    }

}
