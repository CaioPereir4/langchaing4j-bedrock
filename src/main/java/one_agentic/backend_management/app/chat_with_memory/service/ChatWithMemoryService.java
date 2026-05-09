package one_agentic.backend_management.app.chat_with_memory.service;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import one_agentic.backend_management.app.dto.SendMessageDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatWithMemoryService {

    interface Assistant {

        String chat(@MemoryId UUID memoryId, @dev.langchain4j.service.UserMessage String userMessage);
    }

    private ChatModel chatModel;
    private PersistentMemoryStore persistentMemoryStore;

    public ChatWithMemoryService(
                    ChatModel chatModel,
                    PersistentMemoryStore persistentMemoryStore)
    {
        this.chatModel = chatModel;
        this.persistentMemoryStore = persistentMemoryStore;
    }

    public String sendMessageWithMemory(
            SendMessageDTO sendMessageDTO
    ) {
        UserMessage userMessage = UserMessage.from(sendMessageDTO.text);

        ChatMemoryProvider chatMemoryProvider = sessionId -> MessageWindowChatMemory.builder()
                .id(sessionId)
                .maxMessages(20)
                .chatMemoryStore(persistentMemoryStore)
                .build();

        Assistant assistant = AiServices.builder(
                Assistant.class
               ).chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(sessionId -> buildSystemPrompt((UUID) sessionId))
                .build();

        return assistant.chat(
                sendMessageDTO.sessionId,
                sendMessageDTO.text
        );
    }

    private String buildSystemPrompt(UUID sessionId) {

        return """
            Você é um assistente conversacional natural e amigável.

            ID da sessão: %s

            Use o histórico da conversa de forma natural.
            Nunca diga:
            - "como uma IA"
            - "não tenho acesso a informações pessoais"
            - "não posso lembrar"

            Responda sempre em português do Brasil.
            """.formatted(sessionId);
    }

}
