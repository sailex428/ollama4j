package io.github.ollama4j.unittests.jackson;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.utils.OptionsBuilder;

import static org.junit.jupiter.api.Assertions.*;

public class TestChatRequestSerialization extends AbstractSerializationTest<OllamaChatRequest> {

    private OllamaChatRequestBuilder builder;

    @BeforeEach
    public void init() {
        builder = OllamaChatRequestBuilder.getInstance("DummyModel");
    }

    @Test
    public void testRequestOnlyMandatoryFields() {
        OllamaChatRequest req = builder.withMessage(OllamaChatMessageRole.USER, "Some prompt").build();
        String jsonRequest = serialize(req);
        assertEqualsAfterUnmarshalling(deserialize(jsonRequest, OllamaChatRequest.class), req);
    }

    @Test
    public void testRequestMultipleMessages() {
        OllamaChatRequest req = builder.withMessage(OllamaChatMessageRole.SYSTEM, "System prompt")
        .withMessage(OllamaChatMessageRole.USER, "Some prompt")
        .build();
        String jsonRequest = serialize(req);
        assertEqualsAfterUnmarshalling(deserialize(jsonRequest, OllamaChatRequest.class), req);
    }

    @Test
    public void testRequestWithMessageAndImage() {
        OllamaChatRequest req = builder.withMessage(OllamaChatMessageRole.USER, "Some prompt", Collections.emptyList(),
                List.of(new File("src/test/resources/dog-on-a-boat.jpg"))).build();
        String jsonRequest = serialize(req);
        assertEqualsAfterUnmarshalling(deserialize(jsonRequest, OllamaChatRequest.class), req);
    }

    @Test
    public void testRequestWithOptions() {
        OptionsBuilder b = new OptionsBuilder();
        OllamaChatRequest req = builder.withMessage(OllamaChatMessageRole.USER, "Some prompt")
            .withOptions(b.setMirostat(1).build())
            .withOptions(b.setTemperature(1L).build())
            .withOptions(b.setMirostatEta(1L).build())
            .withOptions(b.setMirostatTau(1L).build())
            .withOptions(b.setNumGpu(1).build())
            .withOptions(b.setSeed(1).build())
            .withOptions(b.setTopK(1).build())
            .withOptions(b.setTopP(1).build())
            .withOptions(b.setMinP(1).build())
            .withOptions(b.setCustomOption("cust_float", 1.0f).build())
            .withOptions(b.setCustomOption("cust_int", 1).build())
            .withOptions(b.setCustomOption("cust_str", "custom").build())
            .build();

        String jsonRequest = serialize(req);
        OllamaChatRequest deserializeRequest = deserialize(jsonRequest, OllamaChatRequest.class);
        assertEqualsAfterUnmarshalling(deserializeRequest, req);
        assertEquals(1, deserializeRequest.getOptions().get("mirostat"));
        assertEquals(1.0, deserializeRequest.getOptions().get("temperature"));
        assertEquals(1.0, deserializeRequest.getOptions().get("mirostat_eta"));
        assertEquals(1.0, deserializeRequest.getOptions().get("mirostat_tau"));
        assertEquals(1, deserializeRequest.getOptions().get("num_gpu"));
        assertEquals(1, deserializeRequest.getOptions().get("seed"));
        assertEquals(1, deserializeRequest.getOptions().get("top_k"));
        assertEquals(1.0, deserializeRequest.getOptions().get("top_p"));
        assertEquals(1.0, deserializeRequest.getOptions().get("min_p"));
        assertEquals(1.0, deserializeRequest.getOptions().get("cust_float"));
        assertEquals(1, deserializeRequest.getOptions().get("cust_int"));
        assertEquals("custom", deserializeRequest.getOptions().get("cust_str"));
    }

    @Test
    public void testRequestWithInvalidCustomOption() {
        OptionsBuilder b = new OptionsBuilder();
        assertThrowsExactly(IllegalArgumentException.class, () -> {
                OllamaChatRequest req = builder.withMessage(OllamaChatMessageRole.USER, "Some prompt")
                .withOptions(b.setCustomOption("cust_obj", new Object()).build())
                .build();
        });
    }

    @Test
    public void testWithTemplate() {
        OllamaChatRequest req = builder.withTemplate("System Template")
            .build();
        String jsonRequest = serialize(req);
        assertEqualsAfterUnmarshalling(deserialize(jsonRequest, OllamaChatRequest.class), req);
    }

    @Test
    public void testWithStreaming() {
        OllamaChatRequest req = builder.withStreaming().build();
        String jsonRequest = serialize(req);
        assertEquals(deserialize(jsonRequest, OllamaChatRequest.class).isStream(), true);
    }

    @Test
    public void testWithKeepAlive() {
        String expectedKeepAlive = "5m";
        OllamaChatRequest req = builder.withKeepAlive(expectedKeepAlive)
            .build();
        String jsonRequest = serialize(req);
        assertEquals(deserialize(jsonRequest, OllamaChatRequest.class).getKeepAlive(), expectedKeepAlive);
    }

    @Test
    public void testOllamaRequestSerialization() throws Exception {

        class SimpleClass {
            private String parameter;

            public SimpleClass() {
                parameter = "test";
            }

            public String getParameter() {
                return parameter;
            }

            public void setParameter(String parameter) {
                this.parameter = parameter;
            }
        }

        OllamaChatRequest req = builder.withResponseClass(SimpleClass.class).build();
        String jsonRequest = serialize(req);

        JsonNode rootNode = Utils.getObjectMapper().readTree(jsonRequest);
        assertNotNull(rootNode.get("format"),
                "Request should contain a 'format' property when responseClass is provided");
    }
}
