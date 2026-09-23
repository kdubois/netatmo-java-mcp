package com.kevindubois.mcp;

import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WeatherMcpTest {

    @Test
    void testToolsListContainsAllTools() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
            .toolsList(page -> {
                assertTrue(page.size() >= 3, "Should have at least 3 tools");
                assertNotNull(page.findByName("get_current_weather"));
                assertNotNull(page.findByName("get_available_devices"));
                assertNotNull(page.findByName("get_historical_weather"));
            })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testGetCurrentWeather() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
            .toolsCall("get_current_weather", Map.of(), response -> {
                assertFalse(response.isError());
                assertNotNull(response.firstContent());
                String text = response.firstContent().asText().text();
                assertTrue(text.contains("indoorTemperature") || text.contains("Temperature"));
            })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testGetAvailableDevices() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
            .toolsCall("get_available_devices", Map.of(), response -> {
                assertFalse(response.isError());
                assertNotNull(response.firstContent());
                String text = response.firstContent().asText().text();
                assertTrue(text.contains("station1") || text.contains("Home Weather Station"));
            })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testGetHistoricalWeatherReturnsResponse() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
            .toolsCall("get_historical_weather",
                Map.of("scale", "1hour", "beginDate", "2024-01-01", "endDate", "2024-01-02"),
                response -> {
                    assertNotNull(response.firstContent());
                    assertNotNull(response.firstContent().asText().text());
                })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testResourcesList() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
            .resourcesList(page -> {
                assertTrue(page.size() >= 2, "Should have at least 2 resources");
            })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testReadCurrentWeatherResource() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
            .resourcesRead("weather:///current", response -> {
                assertFalse(response.contents().isEmpty());
                var contents = response.contents().get(0).asText();
                assertEquals("weather:///current", contents.uri());
                assertNotNull(contents.text());
            })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testReadDevicesResource() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
            .resourcesRead("weather:///devices", response -> {
                assertFalse(response.contents().isEmpty());
                var contents = response.contents().get(0).asText();
                assertEquals("weather:///devices", contents.uri());
                assertTrue(contents.text().contains("station1") || contents.text().contains("Home"));
            })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testPromptsList() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
            .promptsList(page -> {
                assertEquals(2, page.size(), "Should have exactly 2 prompts");
                assertNotNull(page.findByName("weather_summary"));
                assertNotNull(page.findByName("device_diagnostics"));
            })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testToolsListHasTtlMs() {
        McpStreamableTestClient client = McpAssured.newStreamableClient()
            .setMcpPath("/mcp")
            .setStateless()
            .build()
            .connect();

        client.when()
            .toolsList(page -> {
                assertNotNull(page.cacheControl(), "tools/list should carry a cache hint");
                assertEquals(60000L, page.cacheControl().ttlMs(), "tools ttlMs should be 60000");
            })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testGetWeatherSummaryPrompt() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
            .promptsGet("weather_summary",
                Map.of("style", "brief"),
                response -> {
                    assertFalse(response.messages().isEmpty());
                    assertEquals(2, response.messages().size());
                })
            .thenAssertResults();

        client.disconnect();
    }
}
