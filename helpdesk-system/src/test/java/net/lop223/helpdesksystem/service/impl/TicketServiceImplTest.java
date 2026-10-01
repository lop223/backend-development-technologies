package net.lop223.helpdesksystem.service.impl;

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
import net.lop223.helpdesksystem.utils.enums.Role;
import net.lop223.helpdesksystem.utils.enums.TicketPriority;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private static Category category(long id) {
        return Category.builder().id(id).name("Category " + id).build();
    }

    private static AppUser user(long id, String username, Role role) {
        return AppUser.builder().id(id).username(username).email(username + "@mail.com").role(role).build();
    }

    private static Ticket ticket(long id) {
        return Ticket.builder()
                .id(id)
                .title("Old title")
                .description("Old description")
                .status(TicketStatus.NEW)
                .priority(TicketPriority.LOW)
                .category(category(1L))
                .createdBy(user(1L, "creator", Role.USER))
                .assignedTo(user(2L, "agent", Role.AGENT))
                .build();
    }

    private static TicketRequest request(Long categoryId, Long assignedToId) {
        TicketRequest request = new TicketRequest();
        request.setTitle("Printer is broken");
        request.setDescription("Paper jam on the 2nd floor");
        request.setPriority(TicketPriority.HIGH);
        request.setCategoryId(categoryId);
        request.setAssignedToId(assignedToId);
        return request;
    }

    private static TicketResponse response(long id) {
        TicketResponse response = new TicketResponse();
        response.setId(id);
        return response;
    }

    @Test
    @DisplayName("getAll: повертає список DTO для всіх тікетів")
    void getAll_ticketsExist_returnsMappedResponses() {
        Ticket first = ticket(1L);
        Ticket second = ticket(2L);
        when(ticketRepository.findAll()).thenReturn(List.of(first, second));
        when(ticketMapper.toResponse(first)).thenReturn(response(1L));
        when(ticketMapper.toResponse(second)).thenReturn(response(2L));

        List<TicketResponse> result = ticketService.getAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    @DisplayName("getAll: повертає порожній список, коли тікетів немає")
    void getAll_noTickets_returnsEmptyList() {
        when(ticketRepository.findAll()).thenReturn(List.of());

        List<TicketResponse> result = ticketService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(ticketMapper);
    }

    @Test
    @DisplayName("getById: повертає DTO, коли тікет знайдено")
    void getById_existingId_returnsResponse() {
        Ticket entity = ticket(3L);
        TicketResponse expected = response(3L);
        when(ticketRepository.findById(3L)).thenReturn(Optional.of(entity));
        when(ticketMapper.toResponse(entity)).thenReturn(expected);

        TicketResponse result = ticketService.getById(3L);

        assertSame(expected, result);
    }

    @Test
    @DisplayName("getById: викидає ResourceNotFoundException, коли тікет не знайдено")
    void getById_missingId_throwsResourceNotFoundException() {
        when(ticketRepository.findById(77L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.getById(77L)
        );

        assertEquals("Ticket not found with id: 77", ex.getMessage());
        verifyNoInteractions(ticketMapper);
    }

    @Test
    @DisplayName("create: створює тікет з категорією, автором і виконавцем")
    void create_withAssignee_savesTicketWithAllRelations() {
        TicketRequest request = request(5L, 9L);
        Category category = category(5L);
        AppUser creator = user(1L, "john", Role.USER);
        AppUser assignee = user(9L, "agent", Role.AGENT);
        Ticket saved = ticket(100L);
        TicketResponse expected = response(100L);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(appUserRepository.findByUsername("john")).thenReturn(Optional.of(creator));
        when(appUserRepository.findById(9L)).thenReturn(Optional.of(assignee));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(saved);
        when(ticketMapper.toResponse(saved)).thenReturn(expected);

        TicketResponse result = ticketService.create(request, "john");

        assertSame(expected, result);
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(1)).save(captor.capture());
        Ticket captured = captor.getValue();
        assertEquals("Printer is broken", captured.getTitle());
        assertEquals("Paper jam on the 2nd floor", captured.getDescription());
        assertEquals(TicketPriority.HIGH, captured.getPriority());
        assertSame(category, captured.getCategory());
        assertSame(creator, captured.getCreatedBy());
        assertSame(assignee, captured.getAssignedTo());
        assertNull(captured.getStatus(), "Status is assigned by @PrePersist, not by the service");
    }

    @Test
    @DisplayName("create: без assignedToId тікет зберігається без виконавця")
    void create_withoutAssignee_savesTicketWithNullAssignee() {
        TicketRequest request = request(5L, null);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category(5L)));
        when(appUserRepository.findByUsername("john")).thenReturn(Optional.of(user(1L, "john", Role.USER)));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response(0L));

        TicketResponse result = ticketService.create(request, "john");

        assertNotNull(result);
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertNull(captor.getValue().getAssignedTo());
        verify(appUserRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("create: викидає ResourceNotFoundException, коли категорії не існує")
    void create_missingCategory_throwsResourceNotFoundException() {
        TicketRequest request = request(404L, null);
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.create(request, "john")
        );

        assertEquals("Category not found with id: 404", ex.getMessage());
        verifyNoInteractions(appUserRepository, ticketRepository, ticketMapper);
    }

    @Test
    @DisplayName("create: викидає ResourceNotFoundException, коли автора не існує")
    void create_missingCreator_throwsResourceNotFoundException() {
        TicketRequest request = request(5L, null);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category(5L)));
        when(appUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.create(request, "unknown")
        );

        assertEquals("User not found: unknown", ex.getMessage());
        verifyNoInteractions(ticketRepository, ticketMapper);
    }

    @Test
    @DisplayName("create: викидає ResourceNotFoundException для null імені автора")
    void create_nullCreatorUsername_throwsResourceNotFoundException() {
        TicketRequest request = request(5L, null);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category(5L)));
        when(appUserRepository.findByUsername(null)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.create(request, null)
        );

        assertEquals("User not found: null", ex.getMessage());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: викидає ResourceNotFoundException, коли виконавця не існує")
    void create_missingAssignee_throwsResourceNotFoundException() {
        TicketRequest request = request(5L, 13L);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category(5L)));
        when(appUserRepository.findByUsername("john")).thenReturn(Optional.of(user(1L, "john", Role.USER)));
        when(appUserRepository.findById(13L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.create(request, "john")
        );

        assertEquals("AppUser not found with id: 13", ex.getMessage());
        verifyNoInteractions(ticketRepository, ticketMapper);
    }

    @Test
    @DisplayName("update: перезаписує поля тікета, категорію та виконавця")
    void update_withAssignee_overwritesFieldsAndSaves() {
        Ticket existing = ticket(20L);
        AppUser originalCreator = existing.getCreatedBy();
        TicketRequest request = request(6L, 9L);
        Category newCategory = category(6L);
        AppUser newAssignee = user(9L, "agent2", Role.AGENT);
        TicketResponse expected = response(20L);
        when(ticketRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(6L)).thenReturn(Optional.of(newCategory));
        when(appUserRepository.findById(9L)).thenReturn(Optional.of(newAssignee));
        when(ticketRepository.save(existing)).thenReturn(existing);
        when(ticketMapper.toResponse(existing)).thenReturn(expected);

        TicketResponse result = ticketService.update(20L, request);

        assertSame(expected, result);
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        Ticket captured = captor.getValue();
        assertEquals("Printer is broken", captured.getTitle());
        assertEquals("Paper jam on the 2nd floor", captured.getDescription());
        assertEquals(TicketPriority.HIGH, captured.getPriority());
        assertSame(newCategory, captured.getCategory());
        assertSame(newAssignee, captured.getAssignedTo());
        assertSame(originalCreator, captured.getCreatedBy());
        assertEquals(TicketStatus.NEW, captured.getStatus());
    }

    @Test
    @DisplayName("update: null assignedToId знімає виконавця з тікета")
    void update_nullAssignee_clearsAssignee() {
        Ticket existing = ticket(21L);
        TicketRequest request = request(1L, null);
        when(ticketRepository.findById(21L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L)));
        when(ticketRepository.save(existing)).thenReturn(existing);
        when(ticketMapper.toResponse(existing)).thenReturn(response(21L));

        TicketResponse result = ticketService.update(21L, request);

        assertEquals(21L, result.getId());
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertNull(captor.getValue().getAssignedTo());
        verifyNoInteractions(appUserRepository);
    }

    @Test
    @DisplayName("update: викидає ResourceNotFoundException, коли тікета не існує")
    void update_missingTicket_throwsResourceNotFoundException() {
        TicketRequest request = request(1L, null);
        when(ticketRepository.findById(55L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.update(55L, request)
        );

        assertEquals("Ticket not found with id: 55", ex.getMessage());
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(categoryRepository, appUserRepository, ticketMapper);
    }

    @Test
    @DisplayName("update: викидає ResourceNotFoundException, коли нової категорії не існує")
    void update_missingCategory_throwsResourceNotFoundException() {
        Ticket existing = ticket(22L);
        TicketRequest request = request(999L, null);
        when(ticketRepository.findById(22L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.update(22L, request)
        );

        assertEquals("Category not found with id: 999", ex.getMessage());
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
    }

    @Test
    @DisplayName("update: викидає ResourceNotFoundException, коли нового виконавця не існує")
    void update_missingAssignee_throwsResourceNotFoundException() {
        Ticket existing = ticket(23L);
        TicketRequest request = request(1L, 31L);
        when(ticketRepository.findById(23L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L)));
        when(appUserRepository.findById(31L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.update(23L, request)
        );

        assertEquals("AppUser not found with id: 31", ex.getMessage());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus: змінює статус тікета та зберігає його")
    void updateStatus_existingTicket_changesStatusAndSaves() {
        Ticket existing = ticket(30L);
        TicketStatusUpdateRequest request = new TicketStatusUpdateRequest();
        request.setStatus(TicketStatus.RESOLVED);
        TicketResponse expected = response(30L);
        when(ticketRepository.findById(30L)).thenReturn(Optional.of(existing));
        when(ticketRepository.save(existing)).thenReturn(existing);
        when(ticketMapper.toResponse(existing)).thenReturn(expected);

        TicketResponse result = ticketService.updateStatus(30L, request);

        assertSame(expected, result);
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(1)).save(captor.capture());
        assertEquals(TicketStatus.RESOLVED, captor.getValue().getStatus());
        assertEquals("Old title", captor.getValue().getTitle());
    }

    @Test
    @DisplayName("updateStatus: викидає ResourceNotFoundException, коли тікета не існує")
    void updateStatus_missingTicket_throwsResourceNotFoundException() {
        TicketStatusUpdateRequest request = new TicketStatusUpdateRequest();
        request.setStatus(TicketStatus.CLOSED);
        when(ticketRepository.findById(31L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.updateStatus(31L, request)
        );

        assertEquals("Ticket not found with id: 31", ex.getMessage());
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
    }

    @Test
    @DisplayName("delete: видаляє знайдений тікет")
    void delete_existingTicket_deletesEntity() {
        Ticket existing = ticket(40L);
        when(ticketRepository.findById(40L)).thenReturn(Optional.of(existing));

        ticketService.delete(40L);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(1)).delete(captor.capture());
        assertEquals(40L, captor.getValue().getId());
    }

    @Test
    @DisplayName("delete: викидає ResourceNotFoundException і не видаляє, коли тікета не існує")
    void delete_missingTicket_throwsResourceNotFoundException() {
        when(ticketRepository.findById(41L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.delete(41L)
        );

        assertEquals("Ticket not found with id: 41", ex.getMessage());
        verify(ticketRepository, never()).delete(any());
    }
}
