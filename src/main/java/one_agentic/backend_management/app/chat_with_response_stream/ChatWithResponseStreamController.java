package one_agentic.backend_management.app.chat_with_response_stream;

import jakarta.validation.Valid;
import one_agentic.backend_management.app.dto.SendMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat-with-response-stream")
public class ChatWithResponseStreamController {

    ChatWithResponseStreamService chatWithResponseStreamService;
    ChatWithResponseStreamController(ChatWithResponseStreamService chatWithResponseStreamService){
        this.chatWithResponseStreamService = chatWithResponseStreamService;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatWithResponseStream(@RequestBody @Valid
                                                 SendMessageDTO body
                                                 ) {
        return chatWithResponseStreamService.sendMessageWithResponseStream(body);
    }
}
