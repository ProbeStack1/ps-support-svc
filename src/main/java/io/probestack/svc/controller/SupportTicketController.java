package io.probestack.svc.controller;

import io.probestack.svc.dto.ApiResponse;
import io.probestack.svc.dto.SupportTicketMessageRequest;
import io.probestack.svc.dto.SupportTicketRequest;
import io.probestack.svc.dto.SupportTicketResponse;
import io.probestack.svc.dto.SupportTicketUpdateRequest;
import io.probestack.svc.service.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support")
@CrossOrigin(origins = "*")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupportTicketResponse>> createTicket(
            @Valid @RequestBody SupportTicketRequest request) {
        SupportTicketResponse ticket = supportTicketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SupportTicketResponse>builder()
                        .status("SUCCESS")
                        .message("Support ticket created successfully")
                        .data(ticket)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>> getTickets(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(
                ApiResponse.<List<SupportTicketResponse>>builder()
                        .status("SUCCESS")
                        .message("Support tickets fetched successfully")
                        .data(supportTicketService.getTickets(status))
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> getTicket(@PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.<SupportTicketResponse>builder()
                        .status("SUCCESS")
                        .message("Support ticket fetched successfully")
                        .data(supportTicketService.getTicket(id))
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> updateTicket(
            @PathVariable String id,
            @RequestBody SupportTicketUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<SupportTicketResponse>builder()
                        .status("SUCCESS")
                        .message("Support ticket updated successfully")
                        .data(supportTicketService.updateTicket(id, request))
                        .build()
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> patchTicket(
            @PathVariable String id,
            @RequestBody SupportTicketUpdateRequest request) {
        return updateTicket(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable String id) {
        supportTicketService.deleteTicket(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status("SUCCESS")
                        .message("Support ticket deleted successfully")
                        .build()
        );
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> addMessage(
            @PathVariable String id,
            @Valid @RequestBody SupportTicketMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SupportTicketResponse>builder()
                        .status("SUCCESS")
                        .message("Support ticket reply added successfully")
                        .data(supportTicketService.addMessage(id, request))
                        .build()
        );
    }
}
