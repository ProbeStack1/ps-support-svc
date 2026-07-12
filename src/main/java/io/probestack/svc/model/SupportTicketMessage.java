package io.probestack.svc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketMessage {
    private String id;
    private String authorName;
    private String authorRole;
    private String message;
    private Instant createdAt;
}
