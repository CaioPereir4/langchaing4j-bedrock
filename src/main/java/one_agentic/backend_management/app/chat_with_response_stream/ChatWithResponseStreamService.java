package one_agentic.backend_management.app.chat_with_response_stream;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.*;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import one_agentic.backend_management.app.dto.SendMessageDTO;
import one_agentic.backend_management.app.shared.PersistentMemoryStore;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatWithResponseStreamService {

    interface Assistant {

        @SystemMessage("Responda de forma objetiva e completa.\n" +
                "Evite respostas muito longas.\n" +
                "Se a resposta exigir muitos passos, priorize os pontos mais importantes.\n" +
                "Nunca termine a resposta no meio de uma frase.")
        TokenStream chat(@MemoryId UUID memoryId, @dev.langchain4j.service.UserMessage String userMessage);
    }

    private StreamingChatModel streamingChatModel;
    private PersistentMemoryStore persistentMemoryStore;

    ChatWithResponseStreamService(
            StreamingChatModel streamingChatModel,
            PersistentMemoryStore persistentMemoryStore
            ){
        this.streamingChatModel = streamingChatModel;
        this.persistentMemoryStore = persistentMemoryStore;
    }

    public Flux<ServerSentEvent<String>> sendMessageWithResponseStream(
            SendMessageDTO sendMessageDTO
    ) {
        return Flux.create(sink -> {
            System.out.println("Recebi a request");
            AtomicReference<StreamingHandle> handleRef = new AtomicReference<>();
            AtomicBoolean cancelled = new AtomicBoolean(false);

            ChatMemoryProvider chatMemoryProvider = sessionId -> MessageWindowChatMemory.builder()
                    .id(sessionId)
                    .maxMessages(20)
                    .chatMemoryStore(persistentMemoryStore)
                    .build();

            Assistant assistant = AiServices.builder(Assistant.class)
                    .streamingChatModel(streamingChatModel)
                    .chatMemoryProvider(chatMemoryProvider)
                    .build();

            TokenStream tokenStream = assistant.chat(
                    sendMessageDTO.sessionId,
                    sendMessageDTO.text
            );

            tokenStream.onPartialResponseWithContext((partialResponse, context) -> {
                handleRef.compareAndSet(null, context.streamingHandle());

                if(cancelled.get() || sink.isCancelled()){
                    context.streamingHandle().cancel();
                }

                System.out.println("PartialResponse para request.");
                sink.next(ServerSentEvent.<String>builder().
                        event("token").data(
                                partialResponse.text()
                        ).build());

            }).onPartialThinkingWithContext(((partialThinking, context) -> {
                System.out.println("O modelo está pensando");
                handleRef.compareAndSet(null, context.streamingHandle());

                if(cancelled.get() || sink.isCancelled()){
                    context.streamingHandle().cancel();
                }

                System.out.println("PartialResponse para request.");
                sink.next(ServerSentEvent.<String>builder().
                        event("thinking").data(
                                partialThinking.text()
                        ).build());

            })).onCompleteResponse(chatResponse -> {
                if (!sink.isCancelled()) {
                    System.out.println("Completa a request.");
                    sink.next(ServerSentEvent.<String>builder()
                            .event("complete")
                            .data(chatResponse.aiMessage().text())
                            .build());

                    sink.complete();
                }
            }).onError(throwable -> {
                System.out.println("Erro na request.");
                if (!sink.isCancelled()) {
                    sink.error(throwable);
                }
            }).start();

            sink.onCancel(() -> {
                cancelled.set(true);
                StreamingHandle handle = handleRef.get();

                if (handle != null) {
                    handle.cancel();
                }

                System.out.println("Client abortou a request.");
            });
            });

    }
}
