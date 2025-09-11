package com.loopers.infrastructure.rank;

import com.loopers.domain.rank.RankingPage;
import com.loopers.domain.rank.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public RankingPage getRankings(
        @RequestParam(required = false) String date,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "1") int page
    ) {
        return rankingService.getDailyRanking(date, page, size);
    }
}
