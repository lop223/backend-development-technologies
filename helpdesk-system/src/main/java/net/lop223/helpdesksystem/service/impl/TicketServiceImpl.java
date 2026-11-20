package net.lop223.helpdesksystem.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.dto.request.TicketStatusUpdateRequest;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.entity.Category;
import net.lop223.helpdesksystem.entity.Ticket;
import net.lop223.helpdesksystem.mapper.TicketMapper;
import net.lop223.helpdesksystem.repository.CategoryRepository;
import net.lop223.helpdesksystem.repository.TicketRepository;
import net.lop223.helpdesksystem.service.TicketService;
import net.lop223.helpdesksystem.service.async.TicketAsyncCoordinatorService;
import net.lop223.helpdesksystem.messaging.TicketEvent;
import net.lop223.helpdesksystem.messaging.TicketEventPublisher;
import net.lop223.helpdesksystem.service.userclient.UserServiceClient;
import net.lop223.helpdesksystem.utils.annotation.MaskSensitiveData;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final UserServiceClient userServiceClient;
    private final TicketMapper ticketMapper;
    private final TicketEventPublisher ticketEventPublisher;

    @Override
    @Transactional
    public List<TicketResponse> getAll() {
        return ticketRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @MaskSensitiveData
    public TicketResponse getById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Override
    @Transactional
    public List<TicketResponse> getTicketsBatch(List<Long> ids) {
        return ids.stream()
                .map(this::getById)
                .toList();
    }

    @Override
    public TicketResponse create(TicketRequest request, String creatorUsername) {
        Category category = findCategoryById(request.getCategoryId());
        UserServiceClient.UserReference creator =
                userServiceClient.getByUsername(creatorUsername);

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setCategory(category);
        ticket.setCreatedById(creator.id());

        if (request.getAssignedToId() != null) {
            UserServiceClient.UserReference assignee =
                    userServiceClient.getById(request.getAssignedToId());
            ticket.setAssignedToId(assignee.id());
        }

        Ticket saved = ticketRepository.save(ticket);
        TicketResponse response = toResponse(saved);
        ticketEventPublisher.publishAfterCommit(new TicketEvent(
                UUID.randomUUID().toString(),
                "TICKET_CREATED",
                saved.getId(),
                "Ticket created",
                Instant.now()
        ));

        return response;
    }

    @Override
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public TicketResponse update(Long id, TicketRequest request) {
        Ticket ticket = findEntityById(id);

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setCategory(findCategoryById(request.getCategoryId()));
        if (request.getAssignedToId() == null) {
            ticket.setAssignedToId(null);
        } else {
            UserServiceClient.UserReference assignee =
                    userServiceClient.getById(request.getAssignedToId());
            ticket.setAssignedToId(assignee.id());
        }

        Ticket saved = ticketRepository.save(ticket);
        ticketEventPublisher.publishAfterCommit(new TicketEvent(
                UUID.randomUUID().toString(),
                "TICKET_UPDATED",
                saved.getId(),
                "Ticket details updated",
                Instant.now()
        ));
        return toResponse(saved);
    }

    @Override
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public TicketResponse updateStatus(Long id, TicketStatusUpdateRequest request) {
        Ticket ticket = findEntityById(id);
        ticket.setStatus(request.getStatus());
        Ticket saved = ticketRepository.save(ticket);
        ticketEventPublisher.publishAfterCommit(new TicketEvent(
                UUID.randomUUID().toString(),
                "TICKET_STATUS_CHANGED",
                saved.getId(),
                "Status changed to " + request.getStatus(),
                Instant.now()
        ));
        return toResponse(saved);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        Ticket ticket = findEntityById(id);
        ticketRepository.delete(ticket);
        ticketEventPublisher.publishAfterCommit(new TicketEvent(
                UUID.randomUUID().toString(),
                "TICKET_DELETED",
                ticket.getId(),
                "Ticket deleted",
                Instant.now()
        ));
    }

    private Ticket findEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private TicketResponse toResponse(Ticket ticket) {
        TicketResponse response = ticketMapper.toResponse(ticket);

        UserServiceClient.UserReference creator =
                userServiceClient.getById(ticket.getCreatedById());
        response.setCreatedByUsername(creator.username());

        if (ticket.getAssignedToId() != null) {
            UserServiceClient.UserReference assignee =
                    userServiceClient.getById(ticket.getAssignedToId());
            response.setAssignedToUsername(assignee.username());
        }

        return response;
    }
}