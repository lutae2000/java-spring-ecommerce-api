package com.loopers.domain.point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PointInfo {

    private Long id;
    private Long point;
    private String loginId;

    public PointInfo(Long point, String loginId) {
        this.point = 0L;
        this.loginId = loginId;
    }

    public static PointInfo from(PointEntity pointEntity){
        return PointInfo.builder()
            .point(pointEntity.getPoint())
            .loginId(pointEntity.getLoginId())
            .build();
    }

}
