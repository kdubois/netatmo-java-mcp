package com.kevindubois.mcp;

import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class AdminMcpToolsTest {

    private static McpStreamableTestClient adminClient() {
        return McpAssured.newStreamableClient()
            .setMcpPath("/admin/mcp")
            .setStateless()
            .setBasicAuth("admin", "admin")
            .build()
            .connect();
    }

    @Test
    void testComparePeriods() {
        McpStreamableTestClient client = adminClient();
        LocalDate start1 = LocalDate.now().minusDays(21);
        LocalDate end1 = LocalDate.now().minusDays(14);
        LocalDate start2 = LocalDate.now().minusDays(7);
        LocalDate end2 = LocalDate.now();

        client.when()
            .toolsCall("compare_periods",
                Map.of(
                    "period1Start", start1.toString(),
                    "period1End", end1.toString(),
                    "period2Start", start2.toString(),
                    "period2End", end2.toString()),
                response -> {
                    assertFalse(response.isError());
                    JsonObject result = (JsonObject) response.structuredContent();
                    assertNotNull(result.getJsonObject("period1"));
                    assertNotNull(result.getJsonObject("period2"));
                    assertNotNull(result.getJsonObject("deltas"));
                    assertNotNull(result.getString("summary"));
                    assertTrue(result.getJsonObject("period1").getJsonArray("days").size() >= 1,
                        "period1 should have at least one day");
                    assertFalse(result.getString("summary").isBlank());
                })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testRunAnomalyScan() {
        McpStreamableTestClient client = adminClient();

        client.when()
            .toolsCall("run_anomaly_scan", Map.of(), response -> {
                assertFalse(response.isError());
                JsonObject result = (JsonObject) response.structuredContent();
                assertNotNull(result.getJsonArray("findings"));
                assertNotNull(result.getJsonArray("recommendations"));
                assertTrue(result.getInteger("dataPoints") >= 1, "should have scanned some data points");
                assertFalse(result.getJsonArray("recommendations").isEmpty());
            })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testRefreshStationData() {
        McpStreamableTestClient client = adminClient();

        client.when()
            .toolsCall("refresh_station_data", Map.of(), response -> {
                assertFalse(response.isError(), response.firstContent().asText().text());
                assertNotNull(response.firstContent());
                String text = response.firstContent().asText().text();
                assertTrue(text.contains("device(s)"), "expected device count in refresh result: " + text);
            })
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testRefreshFiresResourceUpdated() {
        McpStreamableTestClient client = adminClient();

        JsonObject listen = client.newRequest("subscriptions/listen");
        JsonObject listenParams = new JsonObject()
            .put("notifications", new JsonObject().put("resourceSubscriptions",
                new io.vertx.core.json.JsonArray().add("weather:///current")));
        listen.put("params", listenParams);
        McpAssured.injectStatelessMeta(listen);

        client.sendAndForget(listen);
        client.waitForNotifications(1);

        client.when()
            .toolsCall("refresh_station_data", Map.of(), response -> {
                assertFalse(response.isError(), response.firstContent().asText().text());
            })
            .thenAssertResults();

        var snapshot = client.waitForNotifications(2);
        boolean sawUpdated = snapshot.notifications().stream()
            .anyMatch(n -> "notifications/resources/updated".equals(n.getString("method"))
                && "weather:///current".equals(n.getJsonObject("params").getString("uri")));
        assertTrue(sawUpdated, "expected a notifications/resources/updated for weather:///current, got "
            + snapshot.notifications());

        client.disconnect();
    }
}
