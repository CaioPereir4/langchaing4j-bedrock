package one_agentic.backend_management.app.chat.controller;

import jakarta.validation.Valid;
import one_agentic.backend_management.app.dto.ResponseDTO;
import one_agentic.backend_management.app.dto.SendMessageDTO;
import one_agentic.backend_management.app.chat.service.ChatAiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatAiService chatAiService;

    public ChatController(ChatAiService chatAiService) {
        this.chatAiService = chatAiService;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> sendMessage(@RequestBody @Valid SendMessageDTO body){
        var response = chatAiService.sendMessageToAi(body.text);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO(response));
    }
}
