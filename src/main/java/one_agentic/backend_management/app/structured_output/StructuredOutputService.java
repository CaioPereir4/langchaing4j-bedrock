package one_agentic.backend_management.app.structured_output;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import jdk.jfr.Description;
import one_agentic.backend_management.app.dto.SendMessageDTO;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

@Service
public class StructuredOutputService {

    //Please, configure you chatModel to use Json Schema, update the model if needed.
    private final ChatModel chatModel;

    public StructuredOutputService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public Object sendMessage(SendMessageDTO sendMessageDTO) {

        SentimentAnalyser sentimentAnalyser = AiServices.create(SentimentAnalyser.class, chatModel);

        String text = """
                It's wonderful!
            """;

        boolean positive = sentimentAnalyser.isPositive(text);


        return positive;
    }


    interface SentimentAnalyser {
        @UserMessage("""
            "Does {{it}} has a positive sentiment?":
            """)
        boolean isPositive(String text);
    }

}