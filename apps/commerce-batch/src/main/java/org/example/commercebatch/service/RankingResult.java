package org.example.commercebatch.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RankingResult {
    private final boolean success;
    private final int processedCount;
    private final String errorMessage;

    public static RankingResult success(int processedCount) {
        return new RankingResult(true, processedCount, null);
    }

    public static RankingResult failure(String errorMessage) {
        return new RankingResult(false, 0, errorMessage);
    }
}
