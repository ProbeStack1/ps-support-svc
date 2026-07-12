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
public class SupportTicketEvent {
    private String id;
    private String type;
    private String actorName;
    private String actorRole;
    private String fromStatus;
    private String toStatus;
    private String note;
    private Instant createdAt;
}
