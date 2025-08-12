package com.loopers.domain.point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PointInfo {

    private Long id;
    private Long point;
    private String userId;

    public PointInfo(Long point, String loginId) {
        this.point = point;
        this.userId = loginId;
    }

    public static PointInfo from(Point Point){
        return PointInfo.builder()
            .point(Point.getPoint() == null ? 0L : Point.getPoint())
            .userId(Point.getUserId())
            .build();
    }

}
