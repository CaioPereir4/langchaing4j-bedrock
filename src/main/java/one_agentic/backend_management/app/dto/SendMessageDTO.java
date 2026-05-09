package one_agentic.backend_management.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SendMessageDTO {
    @NotBlank(message = "Invalid Text: Empty Text")
    @NotNull(message = "Invalid Text: Text is Null")
    public String text;

    @NotNull(message = "Invalid sessionId: sessionId is Null")
    public UUID sessionId;
}
