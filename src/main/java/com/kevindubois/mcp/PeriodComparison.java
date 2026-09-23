package com.kevindubois.mcp;

import java.util.List;

public record PeriodComparison(
    PeriodResult period1,
    PeriodResult period2,
    Deltas deltas,
    String summary
) {

    public record PeriodResult(
        String label,
        List<DayReading> days,
        Double avgMin,
        Double avgMax,
        Double rangeMin,
        Double rangeMax
    ) {

        public record DayReading(
            String date,
            Double min,
            Double max
        ) {
        }
    }

    public record Deltas(
        Double avgMin,
        Double avgMax
    ) {
    }
}
