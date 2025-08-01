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
    private Long likesCount;

    public Like(String productId, String userId, Boolean likeYn, Long likesCount) {
        this.productId = productId;
        this.userId = userId;
        this.likeYn = likeYn;
        this.likesCount = likesCount;
    }

    public Like(String productId, String userId, Boolean likeYn) {
        this.productId = productId;
        this.userId = userId;
        this.likeYn = likeYn;
    }

}
