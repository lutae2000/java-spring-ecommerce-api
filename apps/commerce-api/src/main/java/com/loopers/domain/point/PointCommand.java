package com.loopers.domain.point;

public class PointCommand {

    private String userId;
    private Long point;

    public record Create(String userId, Long point) {
        public Point toPoint() {
            return Point.builder()
                .userId(this.userId)
                .point(this.point)
                .build();
        }
    }

}
