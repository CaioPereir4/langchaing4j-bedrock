package one_agentic.backend_management.app.shared;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PersistentMemoryStore implements ChatMemoryStore {

    Map<Object, String> store = new ConcurrentHashMap<>();

    @Override
    public List<ChatMessage> getMessages(Object sessionId) {
        var messages = store.getOrDefault(sessionId, "");
        if(messages.isEmpty()){
            return List.of();
        }

        return ChatMessageDeserializer.messagesFromJson(messages);
    }

    @Override
    public void updateMessages(Object sessionId, List<ChatMessage> list) {
        store.put(sessionId, ChatMessageSerializer.messagesToJson(list));
    }

    @Override
    public void deleteMessages(Object sessionId) {
        store.remove(sessionId);
    }
}


