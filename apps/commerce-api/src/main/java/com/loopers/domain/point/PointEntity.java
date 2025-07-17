package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@Table(name = "point")

public class PointEntity {

    @Id
    private String loginId;

    private Long point;

    public PointEntity(String loginId, Long point) {
        if(point < 0 || point == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 마이너스 값이 될 수 없습니다");
        }
        this.point = point;
        this.loginId = loginId;
        validPoint(point);
    }

    private void validPoint(Long point){
        if(point < 0 || point == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 마이너스 값이 될 수 없습니다");
        }
    }
}
