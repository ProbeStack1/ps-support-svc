package io.probestack.svc.dto;

import io.probestack.svc.model.SupportTicketEvent;
import io.probestack.svc.model.SupportTicketMessage;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class SupportTicketResponse {
    private String id;
    private String ticketNumber;
    private String subject;
    private String category;
    private String priority;
    private String status;
    private String plan;
    private String requesterName;
    private String requesterEmail;
    private String createdByRole;
    private Instant createdAt;
    private Instant updatedAt;
    private List<SupportTicketMessage> messages;
    private List<SupportTicketEvent> history;
}
