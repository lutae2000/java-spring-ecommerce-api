package com.loopers.domain.point;

public class PointCommand {

    private String userId;
    private Long point;

    public record Create(String userId, Long point) {
        public PointEntity toPointEntity() {
            return PointEntity.builder()
                .userId(this.userId)
                .point(this.point)
                .build();
        }
    }

}
