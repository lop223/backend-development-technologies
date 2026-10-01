package net.lop223.helpdesksystem.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.dto.request.TicketStatusUpdateRequest;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.entity.AppUser;
import net.lop223.helpdesksystem.entity.Category;
import net.lop223.helpdesksystem.entity.Ticket;
import net.lop223.helpdesksystem.mapper.TicketMapper;
import net.lop223.helpdesksystem.repository.AppUserRepository;
import net.lop223.helpdesksystem.repository.CategoryRepository;
import net.lop223.helpdesksystem.repository.TicketRepository;
import net.lop223.helpdesksystem.service.TicketService;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final AppUserRepository appUserRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public List<TicketResponse> getAll() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse getById(Long id) {
        return ticketMapper.toResponse(findEntityById(id));
    }

    @Override
    public TicketResponse create(TicketRequest request, String creatorUsername) {
        Category category = findCategoryById(request.getCategoryId());
        AppUser creator = appUserRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + creatorUsername));

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setCategory(category);
        ticket.setCreatedBy(creator);

        if (request.getAssignedToId() != null) {
            ticket.setAssignedTo(findUserById(request.getAssignedToId()));
        }

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    @Override
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public TicketResponse update(Long id, TicketRequest request) {
        Ticket ticket = findEntityById(id);

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setCategory(findCategoryById(request.getCategoryId()));
        ticket.setAssignedTo(
                request.getAssignedToId() != null ? findUserById(request.getAssignedToId()) : null
        );

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    @Override
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public TicketResponse updateStatus(Long id, TicketStatusUpdateRequest request) {
        Ticket ticket = findEntityById(id);
        ticket.setStatus(request.getStatus());
        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        Ticket ticket = findEntityById(id);
        ticketRepository.delete(ticket);
    }

    private Ticket findEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private AppUser findUserById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found with id: " + id));
    }
}