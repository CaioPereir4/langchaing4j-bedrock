package one_agentic.backend_management.app.structured_output;

import jakarta.validation.Valid;
import one_agentic.backend_management.app.dto.SendMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/structured-output")
public class StructuredOutputController {

    private StructuredOutputService strucuturedOutputService;

    StructuredOutputController(StructuredOutputService strucuturedOutputService){
        this.strucuturedOutputService = strucuturedOutputService;
    }

    @PostMapping
    public ResponseEntity<Object> chat(@RequestBody @Valid
                                            SendMessageDTO sendMessageDTO
                                            ){
        var response = strucuturedOutputService.sendMessage(sendMessageDTO);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
