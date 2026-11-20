package net.lop223.helpdesksystem.controller.web;

import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.dto.request.TicketStatusUpdateRequest;
import net.lop223.helpdesksystem.dto.response.CategoryResponse;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.service.CategoryService;
import net.lop223.helpdesksystem.service.TicketService;
import net.lop223.helpdesksystem.service.userclient.UserServiceClient;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketWebControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private TicketWebController ticketWebController;

    @Test
    @DisplayName("list: кладе тікети та всі статуси в модель і повертає tickets/list")
    void list_ticketsExist_populatesModel() {
        Model model = new ExtendedModelMap();
        List<TicketResponse> tickets = List.of(new TicketResponse());
        when(ticketService.getAll()).thenReturn(tickets);

        String view = ticketWebController.list(model);

        assertEquals("tickets/list", view);
        assertEquals(tickets, model.getAttribute("tickets"));
        assertArrayEquals(TicketStatus.values(), (TicketStatus[]) model.getAttribute("statuses"));
    }

    @Test
    @DisplayName("newForm: кладе порожній запит, категорії та користувачів у модель")
    void newForm_always_populatesModelWithLookups() {
        Model model = new ExtendedModelMap();
        List<CategoryResponse> categories = List.of(new CategoryResponse());
        List<UserServiceClient.UserReference> users =
                List.of(new UserServiceClient.UserReference(1L, "john"));
        when(categoryService.getAll()).thenReturn(categories);
        when(userServiceClient.getAll()).thenReturn(users);

        String view = ticketWebController.newForm(model);

        assertEquals("tickets/form", view);
        assertInstanceOf(TicketRequest.class, model.getAttribute("ticketRequest"));
        assertEquals(categories, model.getAttribute("categories"));
        assertEquals(users, model.getAttribute("users"));
        assertEquals("/ui/tickets", model.getAttribute("formAction"));
    }

    @Test
    @DisplayName("editForm: завантажує заявку та її довідники у форму редагування")
    void editForm_existingTicket_populatesModel() {
        Model model = new ExtendedModelMap();
        TicketResponse ticket = new TicketResponse();
        ticket.setId(5L);
        ticket.setTitle("Existing ticket");
        ticket.setCategoryId(2L);
        ticket.setAssignedToId(1L);
        when(ticketService.getById(5L)).thenReturn(ticket);
        when(categoryService.getAll()).thenReturn(List.of(new CategoryResponse()));
        when(userServiceClient.getAll())
                .thenReturn(List.of(new UserServiceClient.UserReference(1L, "agent1")));

        String view = ticketWebController.editForm(5L, model);
        TicketRequest request = (TicketRequest) model.getAttribute("ticketRequest");

        assertEquals("tickets/form", view);
        assertEquals("Existing ticket", request.getTitle());
        assertEquals(2L, request.getCategoryId());
        assertEquals(1L, request.getAssignedToId());
        assertEquals("/ui/tickets/5", model.getAttribute("formAction"));
        assertEquals("Редагувати заявку", model.getAttribute("formTitle"));
    }

    @Test
    @DisplayName("update: зберігає зміни заявки і перенаправляє до списку")
    void update_validTicket_delegatesAndRedirects() {
        TicketRequest request = new TicketRequest();

        String view = ticketWebController.update(5L, request);

        assertEquals("redirect:/ui/tickets", view);
        verify(ticketService).update(5L, request);
    }

    @Test
    @DisplayName("create: створює тікет від імені OIDC-користувача і робить redirect")
    void create_oidcUser_createsTicketAndRedirects() {
        TicketRequest request = new TicketRequest();
        String username = "john";

        String view = ticketWebController.create(request, username);

        assertEquals("redirect:/ui/tickets", view);
        verify(ticketService, times(1)).create(request, username);
    }

    @Test
    @DisplayName("create: викидає NullPointerException без OIDC-користувача і не створює тікет")
    void create_nullOidcUser_throwsNullPointerException() {
        TicketRequest request = new TicketRequest();

        assertThrows(NullPointerException.class, () -> ticketWebController.create(request, null));

        verifyNoInteractions(ticketService);
    }

    @Test
    @DisplayName("updateStatus: загортає статус у запит, викликає сервіс і робить redirect")
    void updateStatus_validStatus_delegatesAndRedirects() {
        Long id = 8L;

        String view = ticketWebController.updateStatus(id, TicketStatus.IN_PROGRESS);

        assertEquals("redirect:/ui/tickets", view);
        ArgumentCaptor<TicketStatusUpdateRequest> captor = ArgumentCaptor.forClass(TicketStatusUpdateRequest.class);
        verify(ticketService).updateStatus(eq(8L), captor.capture());
        assertEquals(TicketStatus.IN_PROGRESS, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("updateStatus: пробрасує ResourceNotFoundException для неіснуючого тікета")
    void updateStatus_missingTicket_propagatesResourceNotFoundException() {
        when(ticketService.updateStatus(eq(9L), any(TicketStatusUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Ticket not found with id: 9"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketWebController.updateStatus(9L, TicketStatus.CLOSED)
        );

        assertEquals("Ticket not found with id: 9", ex.getMessage());
    }

    @Test
    @DisplayName("delete: видаляє тікет і робить redirect")
    void delete_existingId_deletesAndRedirects() {
        Long id = 10L;

        String view = ticketWebController.delete(id);

        assertEquals("redirect:/ui/tickets", view);
        verify(ticketService, times(1)).delete(10L);
    }

    @Test
    @DisplayName("delete: пробрасує ResourceNotFoundException для неіснуючого тікета")
    void delete_missingId_propagatesResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("Ticket not found with id: 11")).when(ticketService).delete(11L);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketWebController.delete(11L)
        );

        assertEquals("Ticket not found with id: 11", ex.getMessage());
    }
}
