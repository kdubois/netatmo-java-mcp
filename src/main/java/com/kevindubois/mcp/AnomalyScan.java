package com.kevindubois.mcp;

import java.util.List;

public record AnomalyScan(
    String deviceId,
    String beginDate,
    String endDate,
    int dataPoints,
    List<Finding> findings,
    List<String> recommendations
) {

    public record Finding(
        String type,
        String severity,
        String detail
    ) {
    }
}
