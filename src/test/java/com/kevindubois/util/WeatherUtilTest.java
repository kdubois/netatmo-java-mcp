package com.kevindubois.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.kevindubois.dto.NetatmoHistoricalDataResponse.NetatmoMeasurementData;

class WeatherUtilTest {

    @Test
    void testGetCurrentTimestamp() {
        // Test that getCurrentTimestamp returns a value close to the current time
        long now = Instant.now().getEpochSecond();
        long result = WeatherUtil.getCurrentTimestamp();
        assertTrue(Math.abs(now - result) < 2, "Timestamp should be within 2 seconds of current time");
    }
    
    @Test
    void testGetTimestampDaysAgo() {
        // Test that getTimestampDaysAgo returns the correct timestamp
        long now = Instant.now().getEpochSecond();
        long daysAgo7 = WeatherUtil.getTimestampDaysAgo(7);
        long expected = Instant.now().minus(7, ChronoUnit.DAYS).getEpochSecond();
        assertTrue(Math.abs(expected - daysAgo7) < 2, "Timestamp should be 7 days ago");
    }
    
    @Test
    void testFormatTimestamp() {
        // Test formatting a known timestamp
        long timestamp = 1628097600; // 2021-08-04 16:00:00 UTC
        String formatted = WeatherUtil.formatTimestamp(timestamp, "yyyy-MM-dd HH:mm");
        // Note: The actual result may vary based on timezone, so we'll just check that it contains the date
        assertTrue(formatted.contains("2021-08-04"), "Formatted date should contain the correct date");
    }
    
    @Test
    void testParseTimestamp() {
        // Test parsing a date string
        String dateStr = "2021-08-04";
        Long timestamp = WeatherUtil.parseTimestamp(dateStr, "yyyy-MM-dd");
        assertNotNull(timestamp);
        assertEquals("2021-08-04 00:00", WeatherUtil.formatTimestamp(timestamp, "yyyy-MM-dd HH:mm"));
        
        // Test invalid date format
        Long invalidTimestamp = WeatherUtil.parseTimestamp("invalid-date", "yyyy-MM-dd");
        assertNull(invalidTimestamp);
    }
    
    @Test
    void testProcessDataPoints() {
        // Create test data
        NetatmoMeasurementData parsedData = new NetatmoMeasurementData(
            1628097600L, // beginTime
            3600, // stepTime (1 hour)
            List.of(
                List.of(22.5, 45, 1013.2), // Temperature, Humidity, Pressure
                List.of(23.1, 46, 1013.0)
            )
        );
        
        // Create outdoor data points
        List<List<Object>> outdoorDataPoints = new ArrayList<>();
        outdoorDataPoints.add(List.of(1628097600L, 20.5, 55));
        outdoorDataPoints.add(List.of(1628101200L, 19.8, 58));
        
        // Process data points
        List<Object> result = WeatherUtil.processDataPoints(
            parsedData,
            outdoorDataPoints,
            parsedData.beginTime,
            parsedData.stepTime
        );
        
        // Verify results
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Check first data point
        @SuppressWarnings("unchecked")
        Map<String, Object> firstPoint = (Map<String, Object>) result.get(0);
        String timestamp = (String) firstPoint.get("timestamp");
        assertTrue(timestamp.contains("2021-08-04"), "Timestamp should contain the correct date");
        assertEquals(22.5, firstPoint.get("indoorTemperature"));
        assertEquals(45, firstPoint.get("indoorHumidity"));
        assertEquals(1013.2, firstPoint.get("indoorPressure"));
        assertEquals(20.5, firstPoint.get("outdoorTemperature"));
        assertEquals(55, firstPoint.get("outdoorHumidity"));
    }
    
    @Test
    void testProcessDataPointsWithoutOutdoorData() {
        // Create test data with no outdoor data
        NetatmoMeasurementData parsedData = new NetatmoMeasurementData(
            1628097600L,
            3600,
            List.of(List.of(22.5, 45, 1013.2))
        );
        
        // Process data points
        List<Object> result = WeatherUtil.processDataPoints(
            parsedData,
            null,
            parsedData.beginTime,
            parsedData.stepTime
        );
        
        // Verify results
        assertNotNull(result);
        assertEquals(1, result.size());
        
        // Check data point
        @SuppressWarnings("unchecked")
        Map<String, Object> point = (Map<String, Object>) result.get(0);
        String timestamp = (String) point.get("timestamp");
        assertTrue(timestamp.contains("2021-08-04"), "Timestamp should contain the correct date");
        assertEquals(22.5, point.get("indoorTemperature"));
        assertEquals(45, point.get("indoorHumidity"));
        assertEquals(1013.2, point.get("indoorPressure"));
        assertNull(point.get("outdoorTemperature"));
        assertNull(point.get("outdoorHumidity"));
    }
    
    @Test
    void testNormalizeParameter() {
        // Test with null value
        String result1 = WeatherUtil.normalizeParameter(null, "default");
        assertEquals("default", result1);
        
        // Test with empty string
        String result2 = WeatherUtil.normalizeParameter("", "default");
        assertEquals("default", result2);
        
        // Test with whitespace
        String result3 = WeatherUtil.normalizeParameter("  ", "default");
        assertEquals("default", result3);
        
        // Test with valid string
        String result4 = WeatherUtil.normalizeParameter("value", "default");
        assertEquals("value", result4);
        
        // Test with Integer
        Integer result5 = WeatherUtil.normalizeParameter(null, 10);
        assertEquals(10, result5);
        
        // Test with valid Integer
        Integer result6 = WeatherUtil.normalizeParameter(5, 10);
        assertEquals(5, result6);
    }
}


