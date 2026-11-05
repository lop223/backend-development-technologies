package net.lop223.helpdesksystem.repository;

import net.lop223.helpdesksystem.entity.Ticket;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedByUsername(String username);
    List<Ticket> findByAssignedToUsername(String username);

    long countByStatus(TicketStatus status);

    long countByAssignedToIsNullAndStatusAndCreatedAtBefore(
            TicketStatus status,
            LocalDateTime createdAt
    );
}