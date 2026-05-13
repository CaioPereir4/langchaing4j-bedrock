package one_agentic.backend_management.app.chat_with_memory.controller;

import jakarta.validation.Valid;
import one_agentic.backend_management.app.chat_with_memory.service.ChatWithMemoryService;
import one_agentic.backend_management.app.dto.ResponseDTO;
import one_agentic.backend_management.app.dto.SendMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat-with-memory")
public class ChatWithMemoryController {

    private ChatWithMemoryService chatWithMemoryService;
    ChatWithMemoryController(ChatWithMemoryService chatWithMemoryService){
        this.chatWithMemoryService = chatWithMemoryService;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> chatWitMemory(@RequestBody @Valid SendMessageDTO body){
        var response = this.chatWithMemoryService.sendMessageWithMemory(body);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO(response));
    }
}
