
package net.lop223.helpdesksystem.service.impl;

import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.entity.AppUser;
import net.lop223.helpdesksystem.entity.Category;
import net.lop223.helpdesksystem.entity.Ticket;
import net.lop223.helpdesksystem.mapper.TicketMapper;
import net.lop223.helpdesksystem.repository.AppUserRepository;
import net.lop223.helpdesksystem.repository.CategoryRepository;
import net.lop223.helpdesksystem.repository.TicketRepository;
import net.lop223.helpdesksystem.service.async.TicketAsyncCoordinatorService;
import net.lop223.helpdesksystem.utils.enums.Role;
import net.lop223.helpdesksystem.utils.enums.TicketPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceAsyncDispatchTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private TicketAsyncCoordinatorService ticketAsyncCoordinatorService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    @DisplayName("Створює заявку та запускає фонову обробку, не очікуючи її завершення")
    void create_dispatchesAsyncWorkAndReturnsTicketImmediately() {
        Category category = Category.builder()
                .id(5L)
                .name("Hardware")
                .build();
        AppUser creator = AppUser.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .role(Role.USER)
                .build();

        TicketRequest request = new TicketRequest();
        request.setTitle("Принтер не працює");
        request.setDescription("Застряг папір");
        request.setPriority(TicketPriority.HIGH);
        request.setCategoryId(5L);

        Ticket savedTicket = new Ticket();
        TicketResponse expectedResponse = new TicketResponse();
        expectedResponse.setId(42L);
        expectedResponse.setCreatedByUsername("user1");

        CompletableFuture<TicketAsyncCoordinatorService.TicketAsyncSummary> unfinishedFuture =
                new CompletableFuture<>();

        when(categoryRepository.findById(5L)).thenReturn(java.util.Optional.of(category));
        when(appUserRepository.findByUsername("user1"))
                .thenReturn(java.util.Optional.of(creator));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketMapper.toResponse(savedTicket)).thenReturn(expectedResponse);
        when(ticketAsyncCoordinatorService.dispatchAfterCreation(expectedResponse))
                .thenReturn(unfinishedFuture);

        TicketResponse result = ticketService.create(request, "user1");

        assertSame(expectedResponse, result);
        assertFalse(unfinishedFuture.isDone());
        verify(ticketAsyncCoordinatorService).dispatchAfterCreation(expectedResponse);
    }
}