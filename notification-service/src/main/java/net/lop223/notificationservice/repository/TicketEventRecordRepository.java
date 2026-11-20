package net.lop223.notificationservice.repository;

import net.lop223.notificationservice.entity.TicketEventRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketEventRecordRepository extends JpaRepository<TicketEventRecord, Long> {
    Optional<TicketEventRecord> findByEventId(String eventId);
}
