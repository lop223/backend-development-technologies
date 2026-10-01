package net.lop223.helpdesksystem.service;

import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.dto.request.TicketStatusUpdateRequest;
import net.lop223.helpdesksystem.dto.response.TicketResponse;

import java.util.List;

public interface TicketService {
    List<TicketResponse> getAll();
    TicketResponse getById(Long id);
    TicketResponse create(TicketRequest request, String creatorUsername);
    TicketResponse update(Long id, TicketRequest request);
    TicketResponse updateStatus(Long id, TicketStatusUpdateRequest request);
    void delete(Long id);
}