package io.probestack.svc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "support_tickets")
public class SupportTicket {

    @Id
    private String id;

    @Indexed(unique = true)
    private String ticketNumber;

    private String subject;
    private String category;
    private String priority;

    @Builder.Default
    private String status = "Open";

    private String plan;
    private String requesterName;
    private String requesterEmail;
    private String createdByRole;

    @Builder.Default
    private List<SupportTicketMessage> messages = new ArrayList<>();

    @Builder.Default
    private List<SupportTicketEvent> history = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
