package io.probestack.svc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupportTicketMessageRequest {

    @NotBlank(message = "Message is required")
    private String message;

    private String authorName;
    private String authorRole = "USER";
}
