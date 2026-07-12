package io.probestack.svc.repository;

import io.probestack.svc.model.SupportTicket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends MongoRepository<SupportTicket, String> {
    List<SupportTicket> findAllByOrderByUpdatedAtDesc();
    List<SupportTicket> findByStatusOrderByUpdatedAtDesc(String status);
    Optional<SupportTicket> findByTicketNumber(String ticketNumber);
    boolean existsByTicketNumber(String ticketNumber);
}
