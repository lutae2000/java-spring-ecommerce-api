-- ProductMetrics 테이블 DDL
-- metrics_date 컬럼명으로 통일 (에러 메시지에 따라)

CREATE TABLE IF NOT EXISTS product_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(100) NOT NULL,
    metrics_date DATE NOT NULL,
    likes_count BIGINT NOT NULL DEFAULT 0,
    sales_count BIGINT NOT NULL DEFAULT 0,
    sales_amount BIGINT NOT NULL DEFAULT 0,
    page_views BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_product_metrics_date (product_id, metrics_date),
    INDEX idx_product_id (product_id),
    INDEX idx_metrics_date (metrics_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 기존 테이블이 있고 컬럼명이 metric_date라면 아래 ALTER 문 실행
-- ALTER TABLE product_metrics CHANGE COLUMN metric_date metrics_date DATE NOT NULL;
