package com.kevindubois.mcp;

import io.quarkiverse.mcp.server.ClientCapability;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class MrtrTest {

    @Test
    void testStatelessMrtrConfirmFlow() {
        McpStreamableTestClient client = McpAssured.newStreamableClient()
            .setMcpPath("/mcp")
            .setStateless()
            .setClientCapabilities(new ClientCapability(ClientCapability.ELICITATION, Map.of()))
            .build()
            .connect();

        String beginDate = LocalDate.now().minusDays(60).toString();
        String endDate = LocalDate.now().toString();

        AtomicReference<JsonObject> first = new AtomicReference<>();
        client.when()
            .toolsCall("get_historical_weather")
            .withArguments(Map.of("beginDate", beginDate, "endDate", endDate))
            .withRawAssert(first::set)
            .send()
            .thenAssertResults();

        JsonObject result = first.get().getJsonObject("result");
        assertEquals("input_required", result.getString("resultType"));
        JsonObject inputRequests = result.getJsonObject("inputRequests");
        assertNotNull(inputRequests, "expected inputRequests in the input_required result");
        JsonObject confirmRange = inputRequests.getJsonObject("confirm_range");
        assertNotNull(confirmRange, "expected a confirm_range input request");
        JsonObject params = confirmRange.getJsonObject("params");
        assertEquals("form", params.getString("mode"));
        assertNotNull(params.getString("message"));
        JsonObject requestedSchema = params.getJsonObject("requestedSchema");
        assertNotNull(requestedSchema.getJsonObject("properties").getJsonObject("confirm"));
        String requestState = result.getString("requestState");
        assertNotNull(requestState, "expected a requestState to be echoed back");

        client.when()
            .toolsCall("get_historical_weather")
            .withArguments(Map.of("beginDate", beginDate, "endDate", endDate))
            .withInputResponses(new JsonObject().put("confirm_range",
                new JsonObject().put("action", "accept").put("content", new JsonObject().put("confirm", true))))
            .withRequestState(requestState)
            .withRawAssert(raw -> {
                JsonObject retryResult = raw.getJsonObject("result");
                assertEquals("complete", retryResult.getString("resultType"));
            })
            .send()
            .thenAssertResults();

        client.disconnect();
    }

    @Test
    void testStatefulLargeRangeIsPlainError() {
        McpStreamableTestClient client = McpAssured.newStreamableClient()
            .setMcpPath("/mcp")
            .build()
            .connect();

        String beginDate = LocalDate.now().minusDays(60).toString();
        String endDate = LocalDate.now().toString();

        client.when()
            .toolsCall("get_historical_weather",
                Map.of("beginDate", beginDate, "endDate", endDate),
                response -> {
                    assertTrue(response.isError(), "expected a plain tool error for a stateful client");
                    String text = response.firstContent().asText().text();
                    assertTrue(text.contains("days"), "expected a message about the day range: " + text);
                })
            .thenAssertResults();

        client.disconnect();
    }
}
