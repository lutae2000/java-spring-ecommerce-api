package org.example.commercebatch.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commercebatch.service.RankingBatchService;
import org.example.commercebatch.service.RankingCommand;
import org.example.commercebatch.service.RankingResult;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WeeklyRankingJob {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RankingBatchService rankingBatchService;

    @Bean
    public Job weeklyRankingProcessJob() {
        return new JobBuilder("weeklyRankingProcessJob", jobRepository)
            .start(weeklyRankingProcessStep())
            .build();
    }

    @Bean
    @JobScope
    public Step weeklyRankingProcessStep() {
        return new StepBuilder("weeklyRankingProcessStep", jobRepository)
            .tasklet(weeklyRankingTasklet(null), transactionManager)
            .build();
    }

    @Bean
    @JobScope
    public Tasklet weeklyRankingTasklet(@Value("#{jobParameters['targetDate']}") String targetDate) {
        return (contribution, chunkContext) -> {
            log.info("주간 랭킹 배치 Tasklet 실행 - targetDate: {}", targetDate);

            LocalDate target = LocalDate.parse(targetDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            RankingCommand command = RankingCommand.createWeekly(target);

            RankingResult result = rankingBatchService.processWeeklyRanking(command);

            if (result.isSuccess()) {
                log.info("주간 랭킹 배치 완료 - 처리 건수: {}", result.getProcessedCount());
                return RepeatStatus.FINISHED;
            } else {
                log.error("주간 랭킹 배치 실패: {}", result.getErrorMessage());
                throw new RuntimeException("주간 랭킹 배치 실패: " + result.getErrorMessage());
            }
        };
    }
}
