package com.loopers.domain.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.product.ProductPageResult;
import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.product.event.StockOutKafkaEvent;
import com.loopers.domain.rank.event.RankingEventPublisher;
import com.loopers.infrastructure.event.KafkaEventPublisher;
import com.loopers.infrastructure.kafka.PartitionKeyStrategy;
import com.loopers.message.KafkaEventMessage;
import com.loopers.message.KafkaTopics;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final PartitionKeyStrategy partitionKeyStrategy;
    private final ObjectMapper objectMapper;
    private RankingEventPublisher rankingEventPublisher;

    /**
     * product 생성(upsert)
     */
    @Transactional
    public void createProduct(ProductCommand productCommand){
        Product product = ProductCommand.toProduct(productCommand);
        productRepository.save(product);

        // 캐시 무효화
        productRepository.invalidateProductCache(product.getCode());
    }

    /**
     * 물품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductInfo findProduct(String productId){
        // 상품 조회 이벤트 발행
        rankingEventPublisher.publishViewEvent(productId);

        Product product = productRepository.findProductWithCache(productId, Duration.ofMinutes(1));

        if(ObjectUtils.isEmpty(product)){
            throw new CoreException(ErrorType.NOT_FOUND, "검색하려는 물품이 없습니다");
        }

        return ProductInfo.from(product);
    }


    /**
     * 주문 상품 처리
     * @param productId
     * @param quantity
     */
    @Transactional
    public void updateStock(String productId, Long quantity, OrderStatus orderStatus){
        ProductInfo productInfo = findProduct(productId);

        if(ObjectUtils.isEmpty(productInfo)){
            throw new CoreException(ErrorType.NOT_FOUND, "주문하려는 물품코드가 없습니다");
        }
        
        // 재고 부족 체크 및 이벤트 발행
        if(orderStatus.equals(OrderStatus.ORDER_PLACED) && productInfo.getQuantity() <= quantity){
            // 재고 부족 이벤트 발행 (캐시 무효화를 위해)
            try {
                publishStockOutEvent(productId, productInfo.getQuantity(), quantity);
            } catch (JsonProcessingException e) {
                log.error("재고 부족 이벤트 발행 실패 - ProductId: {}, Error: {}", productId, e.getMessage());
            }
            
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다");
        }
        
        productRepository.updateProduct(productId, quantity, orderStatus);
        
        // 재고 업데이트 후 재고가 0이 된 경우에도 이벤트 발행
        if(orderStatus.equals(OrderStatus.ORDER_PLACED)) {
            long remainingStock = productInfo.getQuantity() - quantity;
            if(remainingStock == 0) {
                try {
                    publishStockOutEvent(productId, 0L, 0L);
                } catch (JsonProcessingException e) {
                    log.error("재고 소진 이벤트 발행 실패 - ProductId: {}, Error: {}", productId, e.getMessage());
                }
            }
        }
    }

    /**
     * 재고 부족 이벤트를 Kafka로 직접 발행
     */
    private void publishStockOutEvent(String productId, Long currentStock, Long requestedQuantity) throws JsonProcessingException {
        String reason = (currentStock == 0) ? "STOCK_EMPTY" : "INSUFFICIENT_STOCK";
        
        log.info("재고 부족 이벤트 발행 - ProductId: {}, Reason: {}, CurrentStock: {}, RequestedQuantity: {}", 
                productId, reason, currentStock, requestedQuantity);

        // StockOutKafkaEvent 생성
        StockOutKafkaEvent kafkaEvent = StockOutKafkaEvent.builder()
            .productId(productId)
            .reason(reason)
            .currentStock(currentStock)
            .requestedQuantity(requestedQuantity)
            .occurredAt(LocalDateTime.now())
            .build();

        // KafkaEventMessage 생성
        KafkaEventMessage<Object> message = KafkaEventMessage.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("STOCK_OUT")
            .aggregateId(productId)
            .timestamp(LocalDateTime.now())
            .version(1)
            .payload(objectMapper.writeValueAsString(kafkaEvent))
            .build();

        // 파티션 키 생성 (상품별 순서 보장)
        String partitionKey = partitionKeyStrategy.getCatalogEventPartitionKey(productId);
        kafkaEventPublisher.publishEvent(KafkaTopics.CATALOG_EVENT, partitionKey, message);
        
        log.info("재고 부족 이벤트 발행 완료 - ProductId: {}", productId);
    }

    /**
     * 브랜드로 상품 리스트 조회
     */
    @Transactional(readOnly = true)
    public ProductPageResult findProductListByBrandCode(String brandCode, SortBy sortBy, Pageable pageable) {
        return productRepository.findProductListByBrandCodeWithCache(
            brandCode, sortBy, pageable, Duration.ofMinutes(1)
        );
    }
}
