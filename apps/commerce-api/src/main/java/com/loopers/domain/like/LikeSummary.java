package com.loopers.domain.like;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "like_summary")
public class LikeSummary {

    @Id
    private String productId;

    @Column(nullable = false)
    private Long likesCount;

    public LikeSummary(String productId, Long likesCount) {
        this.productId = productId;
        this.likesCount = likesCount;
        increaseLikesCount();
        decreaseLikesCount();
    }

    public void increaseLikesCount(){
        if(this.likesCount == Long.MAX_VALUE){  //오버플로우 방지
            this.likesCount = Long.MAX_VALUE;
        } else {
            this.likesCount++;
        }
    }

    public void decreaseLikesCount(){
        if(this.likesCount > 0)
            this.likesCount--;
    }
}
