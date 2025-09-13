package com.loopers.interfaces.api.rank;


import com.loopers.domain.rank.RankingResponseDto;
import com.loopers.domain.rank.RankingService;
import com.loopers.interfaces.api.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ranks")
@RequiredArgsConstructor
public class RankController {
    private final RankingService rankingService;

    @GetMapping
    public ApiResponse<List<RankingResponseDto>> getRank(
        @RequestParam(value = "date") String date,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        List<RankingResponseDto> list = rankingService.getRanking(date, size, page);
        return ApiResponse.success(list);
    }
}
