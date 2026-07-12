package io.probestack.svc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupportTicketRequest {

    @NotBlank(message = "Subject is required")
    private String subject;

    private String category = "Technical Issue";
    private String priority = "Medium";

    @NotBlank(message = "Description is required")
    private String description;

    private String plan = "Standard";
    private String requesterName;

    @Email(message = "Invalid requester email")
    private String requesterEmail;

    private String actorRole = "USER";
}
