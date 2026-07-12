package io.probestack.svc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.probestack.svc.dto.SupportTicketMessageRequest;
import io.probestack.svc.dto.SupportTicketRequest;
import io.probestack.svc.dto.SupportTicketResponse;
import io.probestack.svc.model.SupportTicketEvent;
import io.probestack.svc.model.SupportTicketMessage;
import io.probestack.svc.service.SupportTicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SupportTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupportTicketService supportTicketService;

    @Test
    void createTicket_validPayload_returns201() throws Exception {
        SupportTicketRequest request = new SupportTicketRequest();
        request.setSubject("Gateway returning 502s");
        request.setDescription("Production gateway intermittently returns 502.");
        request.setPriority("Urgent");

        when(supportTicketService.createTicket(any())).thenReturn(openTicket());

        mockMvc.perform(post("/api/v1/support")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.ticketNumber").value("TCK-1234"))
                .andExpect(jsonPath("$.data.messages[0].message").value("Production gateway intermittently returns 502."));
    }

    @Test
    void addMessage_validPayload_returns201() throws Exception {
        SupportTicketMessageRequest request = new SupportTicketMessageRequest();
        request.setMessage("Adding the latest error log.");

        SupportTicketResponse response = openTicket();
        response.getMessages().add(SupportTicketMessage.builder()
                .id("msg-2")
                .authorName("User")
                .authorRole("USER")
                .message("Adding the latest error log.")
                .createdAt(Instant.parse("2026-07-08T11:00:00Z"))
                .build());

        when(supportTicketService.addMessage(eq("TCK-1234"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/support/TCK-1234/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.messages[1].message").value("Adding the latest error log."));
    }

    private SupportTicketResponse openTicket() {
        return SupportTicketResponse.builder()
                .id("mongo-id")
                .ticketNumber("TCK-1234")
                .subject("Gateway returning 502s")
                .category("Technical Issue")
                .priority("Urgent")
                .status("Open")
                .plan("Priority")
                .requesterName("User")
                .createdByRole("USER")
                .createdAt(Instant.parse("2026-07-08T10:00:00Z"))
                .updatedAt(Instant.parse("2026-07-08T10:00:00Z"))
                .messages(new ArrayList<>(List.of(SupportTicketMessage.builder()
                        .id("msg-1")
                        .authorName("User")
                        .authorRole("USER")
                        .message("Production gateway intermittently returns 502.")
                        .createdAt(Instant.parse("2026-07-08T10:00:00Z"))
                        .build())))
                .history(List.of(SupportTicketEvent.builder()
                        .id("evt-1")
                        .type("CREATED")
                        .actorName("User")
                        .actorRole("USER")
                        .toStatus("Open")
                        .note("Ticket created")
                        .createdAt(Instant.parse("2026-07-08T10:00:00Z"))
                        .build()))
                .build();
    }
}
