package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PointDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        public String loginId;
        public Long point;

    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        public String loginId;
        public Long point;

        public static Response from(PointInfo pointInfo){
            return new Response(pointInfo.getUserId(), pointInfo.getPoint());
        }
    }
}
