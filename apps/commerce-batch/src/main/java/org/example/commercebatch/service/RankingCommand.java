package org.example.commercebatch.service;

import lombok.Getter;


import java.time.LocalDate;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RankingCommand {
    private final LocalDate targetDate;
    private final RankingType type;

    public static RankingCommand createWeekly(LocalDate targetDate) {
        return new RankingCommand(targetDate, RankingType.WEEKLY);
    }

    public static RankingCommand createMonthly(LocalDate targetDate) {
        return new RankingCommand(targetDate, RankingType.MONTHLY);
    }

    public enum RankingType {
        WEEKLY, MONTHLY
    }
}
