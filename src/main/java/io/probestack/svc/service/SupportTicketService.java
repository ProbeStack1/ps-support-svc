package io.probestack.svc.service;

import io.probestack.svc.dto.SupportTicketMessageRequest;
import io.probestack.svc.dto.SupportTicketRequest;
import io.probestack.svc.dto.SupportTicketResponse;
import io.probestack.svc.dto.SupportTicketUpdateRequest;
import io.probestack.svc.exception.ResourceNotFoundException;
import io.probestack.svc.model.SupportTicket;
import io.probestack.svc.model.SupportTicketEvent;
import io.probestack.svc.model.SupportTicketMessage;
import io.probestack.svc.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SupportTicketService {

    private static final Set<String> VALID_STATUSES = Set.of("Open", "In Progress", "Resolved", "Closed");
    private static final Set<String> VALID_PRIORITIES = Set.of("Low", "Medium", "High", "Urgent");

    private final SupportTicketRepository supportTicketRepository;

    public SupportTicketService(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }

    public SupportTicketResponse createTicket(SupportTicketRequest request) {
        validatePriority(request.getPriority());

        Instant now = Instant.now();
        String requesterName = defaultIfBlank(request.getRequesterName(),
                defaultIfBlank(request.getRequesterEmail(), "User"));
        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber(generateTicketNumber())
                .subject(request.getSubject().trim())
                .category(defaultIfBlank(request.getCategory(), "Technical Issue"))
                .priority(defaultIfBlank(request.getPriority(), "Medium"))
                .status("Open")
                .plan(defaultIfBlank(request.getPlan(), "Standard"))
                .requesterName(requesterName)
                .requesterEmail(request.getRequesterEmail())
                .createdByRole(normalizeRole(request.getActorRole()))
                .build();

        ticket.getMessages().add(message(ticket.getRequesterName(), ticket.getCreatedByRole(),
                request.getDescription().trim(), now));
        ticket.getHistory().add(event("CREATED", ticket.getRequesterName(), ticket.getCreatedByRole(),
                null, "Open", "Ticket created", now));

        return toResponse(supportTicketRepository.save(ticket));
    }

    public List<SupportTicketResponse> getTickets(String status) {
        List<SupportTicket> tickets = StringUtils.hasText(status) && !"All".equalsIgnoreCase(status)
                ? supportTicketRepository.findByStatusOrderByUpdatedAtDesc(status)
                : supportTicketRepository.findAllByOrderByUpdatedAtDesc();

        return tickets.stream().map(this::toResponse).toList();
    }

    public SupportTicketResponse getTicket(String idOrTicketNumber) {
        return toResponse(findTicket(idOrTicketNumber));
    }

    public SupportTicketResponse updateTicket(String idOrTicketNumber, SupportTicketUpdateRequest request) {
        SupportTicket ticket = findTicket(idOrTicketNumber);
        String actorRole = normalizeRole(request.getActorRole());
        String actorName = resolveActorName(ticket, request.getActorName(), actorRole);
        Instant now = Instant.now();

        if (StringUtils.hasText(request.getSubject())) {
            ticket.setSubject(request.getSubject().trim());
        }
        if (StringUtils.hasText(request.getCategory())) {
            ticket.setCategory(request.getCategory().trim());
        }
        if (StringUtils.hasText(request.getPriority())) {
            validatePriority(request.getPriority());
            ticket.setPriority(request.getPriority().trim());
        }
        if (StringUtils.hasText(request.getStatus())) {
            changeStatus(ticket, request.getStatus().trim(), actorName, actorRole, request.getNote(), now);
        } else {
            ticket.getHistory().add(event("UPDATED", actorName, actorRole, ticket.getStatus(), ticket.getStatus(),
                    defaultIfBlank(request.getNote(), "Ticket details updated"), now));
        }

        return toResponse(supportTicketRepository.save(ticket));
    }

    public void deleteTicket(String idOrTicketNumber) {
        supportTicketRepository.delete(findTicket(idOrTicketNumber));
    }

    public SupportTicketResponse addMessage(String idOrTicketNumber, SupportTicketMessageRequest request) {
        SupportTicket ticket = findTicket(idOrTicketNumber);
        if ("Closed".equals(ticket.getStatus())) {
            throw new IllegalArgumentException("Closed tickets cannot accept new replies");
        }

        Instant now = Instant.now();
        String authorRole = normalizeRole(request.getAuthorRole());
        String authorName = resolveActorName(ticket, request.getAuthorName(), authorRole);
        ticket.getMessages().add(message(authorName, authorRole, request.getMessage().trim(), now));
        ticket.getHistory().add(event("MESSAGE_ADDED", authorName, authorRole, ticket.getStatus(), ticket.getStatus(),
                "Reply added", now));

        if ("Open".equals(ticket.getStatus()) && "ADMIN".equals(authorRole)) {
            changeStatus(ticket, "In Progress", authorName, authorRole, "Support response added", now);
        }

        return toResponse(supportTicketRepository.save(ticket));
    }

    private SupportTicket findTicket(String idOrTicketNumber) {
        return supportTicketRepository.findById(idOrTicketNumber)
                .or(() -> supportTicketRepository.findByTicketNumber(idOrTicketNumber))
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found: " + idOrTicketNumber));
    }

    private void changeStatus(SupportTicket ticket, String newStatus, String actorName, String actorRole, String note, Instant now) {
        validateStatus(newStatus);
        String oldStatus = ticket.getStatus();
        if (!oldStatus.equals(newStatus)) {
            ticket.setStatus(newStatus);
            ticket.getHistory().add(event("STATUS_CHANGED", actorName, actorRole, oldStatus, newStatus,
                    defaultIfBlank(note, "Status changed"), now));
        }
    }

    private SupportTicketMessage message(String authorName, String authorRole, String text, Instant now) {
        return SupportTicketMessage.builder()
                .id(UUID.randomUUID().toString())
                .authorName(authorName)
                .authorRole(authorRole)
                .message(text)
                .createdAt(now)
                .build();
    }

    private SupportTicketEvent event(String type, String actorName, String actorRole, String fromStatus,
                                     String toStatus, String note, Instant now) {
        return SupportTicketEvent.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .actorName(actorName)
                .actorRole(actorRole)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .note(note)
                .createdAt(now)
                .build();
    }

    private SupportTicketResponse toResponse(SupportTicket ticket) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .subject(ticket.getSubject())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .plan(ticket.getPlan())
                .requesterName(ticket.getRequesterName())
                .requesterEmail(ticket.getRequesterEmail())
                .createdByRole(ticket.getCreatedByRole())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .messages(ticket.getMessages())
                .history(ticket.getHistory())
                .build();
    }

    private String generateTicketNumber() {
        String ticketNumber;
        do {
            ticketNumber = "TCK-" + ThreadLocalRandom.current().nextInt(1000, 10000);
        } while (supportTicketRepository.existsByTicketNumber(ticketNumber));
        return ticketNumber;
    }

    private void validateStatus(String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid ticket status: " + status);
        }
    }

    private void validatePriority(String priority) {
        if (StringUtils.hasText(priority) && !VALID_PRIORITIES.contains(priority)) {
            throw new IllegalArgumentException("Invalid ticket priority: " + priority);
        }
    }

    private String normalizeRole(String role) {
        return "ADMIN".equalsIgnoreCase(role) ? "ADMIN" : "USER";
    }

    private String resolveActorName(SupportTicket ticket, String requestedName, String actorRole) {
        if ("ADMIN".equals(actorRole)) {
            return defaultIfBlank(requestedName, "Admin");
        }
        return defaultIfBlank(requestedName,
                defaultIfBlank(ticket.getRequesterEmail(), defaultIfBlank(ticket.getRequesterName(), "User")));
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
