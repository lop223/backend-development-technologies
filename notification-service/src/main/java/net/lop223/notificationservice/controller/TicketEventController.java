package net.lop223.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import net.lop223.notificationservice.entity.TicketEventRecord;
import net.lop223.notificationservice.repository.TicketEventRecordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class TicketEventController {
    private final TicketEventRecordRepository repository;

    @GetMapping
    public List<TicketEventRecord> getEvents() {
        return repository.findAll();
    }
}
