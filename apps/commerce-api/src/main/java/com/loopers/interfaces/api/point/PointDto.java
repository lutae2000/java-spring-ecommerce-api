package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointEntity;
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

        public PointEntity toPointEntity(){
            return PointEntity.builder()
                .loginId(this.loginId)
                .point(this.point)
                .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        public String loginId;
        public Long point;

        public static Response from(String loginId, Long point){
            return new Response(loginId, point);
        }
    }
}
