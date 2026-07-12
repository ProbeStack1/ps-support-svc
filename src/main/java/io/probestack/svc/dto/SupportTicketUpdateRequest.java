package io.probestack.svc.dto;

import lombok.Data;

@Data
public class SupportTicketUpdateRequest {
    private String subject;
    private String category;
    private String priority;
    private String status;
    private String actorName;
    private String actorRole = "USER";
    private String note;
}
