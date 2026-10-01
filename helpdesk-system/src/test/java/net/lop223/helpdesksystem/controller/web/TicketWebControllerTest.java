package net.lop223.helpdesksystem.controller.web;

import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.dto.request.TicketStatusUpdateRequest;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;
import net.lop223.helpdesksystem.dto.response.CategoryResponse;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.service.AppUserService;
import net.lop223.helpdesksystem.service.CategoryService;
import net.lop223.helpdesksystem.service.TicketService;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
    private AppUserService appUserService;

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
        List<AppUserResponse> users = List.of(new AppUserResponse());
        when(categoryService.getAll()).thenReturn(categories);
        when(appUserService.getAll()).thenReturn(users);

        String view = ticketWebController.newForm(model);

        assertEquals("tickets/form", view);
        assertInstanceOf(TicketRequest.class, model.getAttribute("ticketRequest"));
        assertEquals(categories, model.getAttribute("categories"));
        assertEquals(users, model.getAttribute("users"));
    }

    @Test
    @DisplayName("create: створює тікет від імені OIDC-користувача і робить redirect")
    void create_oidcUser_createsTicketAndRedirects() {
        TicketRequest request = new TicketRequest();
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getPreferredUsername()).thenReturn("john");

        String view = ticketWebController.create(request, oidcUser);

        assertEquals("redirect:/ui/tickets", view);
        verify(ticketService, times(1)).create(request, "john");
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
