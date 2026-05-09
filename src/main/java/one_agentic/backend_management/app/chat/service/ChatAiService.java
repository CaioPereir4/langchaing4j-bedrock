package one_agentic.backend_management.app.chat.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatAiService {

    ChatModel chatModel;
    ChatAiService(ChatModel chatModel){
        this.chatModel = chatModel;
    }

    public String sendMessageToAi(String text) {
        UserMessage userMessage = UserMessage.from(
                text
        );

        ChatResponse chatResponse = chatModel.chat(userMessage);

        return chatResponse.aiMessage().text();
    };

}
