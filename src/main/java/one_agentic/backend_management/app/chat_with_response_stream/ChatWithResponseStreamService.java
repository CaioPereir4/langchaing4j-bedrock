package one_agentic.backend_management.app.chat_with_response_stream;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.*;
import one_agentic.backend_management.app.dto.SendMessageDTO;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatWithResponseStreamService {

    private StreamingChatModel streamingChatModel;

    ChatWithResponseStreamService(
            StreamingChatModel streamingChatModel
            ){
        this.streamingChatModel = streamingChatModel;
    }

    public Flux<ServerSentEvent<String>> sendMessageWithResponseStream(
            SendMessageDTO sendMessageDTO
    ) {
        return Flux.create(sink -> {
            AtomicReference<StreamingHandle> handleRef = new AtomicReference<>();
            AtomicBoolean cancelled = new AtomicBoolean(false);

            StreamingChatResponseHandler handler = new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {
                    handleRef.compareAndSet(null, context.streamingHandle());

                    if(cancelled.get() && sink.isCancelled()){
                        context.streamingHandle().cancel();
                    }

                    System.out.println("PartialResponse para request.");
                    sink.next(ServerSentEvent.<String>builder().
                            event("token").data(
                                    partialResponse.text()
                            ).build());

                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    if (!sink.isCancelled()) {
                        System.out.println("Completa a request.");
                        sink.next(ServerSentEvent.<String>builder()
                                .event("complete")
                                .data(chatResponse.aiMessage().text())
                                .build());

                        sink.complete();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("Erro na request.");
                    if (!sink.isCancelled()) {
                        sink.error(throwable);
                    }
                }
            };

            sink.onCancel(() -> {
                cancelled.set(true);
                StreamingHandle handle = handleRef.get();

                if (handle != null) {
                    handle.cancel();
                }

                System.out.println("Client abortou a request.");
            });

            streamingChatModel.chat(sendMessageDTO.text, handler);
        });


    }
}
