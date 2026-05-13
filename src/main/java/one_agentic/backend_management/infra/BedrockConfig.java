package one_agentic.backend_management.infra;

import dev.langchain4j.model.bedrock.BedrockCachePointPlacement;
import dev.langchain4j.model.bedrock.BedrockChatModel;
import dev.langchain4j.model.bedrock.BedrockChatRequestParameters;
import dev.langchain4j.model.bedrock.BedrockStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.time.Duration;
import java.util.Map;

@Configuration
public class BedrockConfig {


    @Bean
    ChatModel chatModel(
            @Value("${aws.region}") String region,
            @Value("${aws.bedrock.model-id}") String modelId
    ) {
        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .build();

        return BedrockChatModel.builder()
                .client(client)
                .timeout(Duration.ofSeconds(30))
                .maxRetries(3)
                .logResponses(true)
                .modelId(modelId)
                .defaultRequestParameters(
                        BedrockChatRequestParameters.builder()
                                .temperature(0.3)
                                .maxOutputTokens(800)
                                .promptCaching(BedrockCachePointPlacement.AFTER_SYSTEM)
                                .build()
                )
                .build();
    }

    @Bean
    StreamingChatModel streamingChatModel(
            @Value("${aws.region}") String region,
            @Value("${aws.bedrock.model-id}") String modelId
    ) {
        BedrockRuntimeAsyncClient client = BedrockRuntimeAsyncClient.builder()
                .region(Region.of(region))
                .build();

        return BedrockStreamingChatModel.builder().client(client)
                .timeout(Duration.ofSeconds(30))
                .logResponses(true)
                .modelId(modelId)
                .defaultRequestParameters(
                        BedrockChatRequestParameters.builder()
                                .temperature(0.3)
                                .maxOutputTokens(800)
                                .promptCaching(BedrockCachePointPlacement.AFTER_SYSTEM)
                                .additionalModelRequestField(
                                        "reasoningConfig",
                                        Map.of(
                                                "type", "enabled",
                                                "maxReasoningEffort", "low"
                                        )
                                )
                                .build()
                )
                .build();
    }
}