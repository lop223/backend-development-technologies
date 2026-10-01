package net.lop223.helpdesksystem.mapper;

import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.entity.AppUser;
import net.lop223.helpdesksystem.entity.Category;
import net.lop223.helpdesksystem.entity.Ticket;
import net.lop223.helpdesksystem.utils.enums.Role;
import net.lop223.helpdesksystem.utils.enums.TicketPriority;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TicketMapperTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 10, 9, 30);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 1, 11, 14, 0);

    private final TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

    @Test
    @DisplayName("toResponse: мапить прості поля та вкладені category/createdBy/assignedTo")
    void toResponse_fullEntity_mapsFlatAndNestedFields() {
        Ticket entity = Ticket.builder()
                .id(50L)
                .title("VPN does not connect")
                .description("Error 809")
                .status(TicketStatus.IN_PROGRESS)
                .priority(TicketPriority.MEDIUM)
                .category(Category.builder().id(3L).name("Network").build())
                .createdBy(AppUser.builder().id(1L).username("john").role(Role.USER).build())
                .assignedTo(AppUser.builder().id(2L).username("agent").role(Role.AGENT).build())
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();

        TicketResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals("VPN does not connect", response.getTitle());
        assertEquals("Error 809", response.getDescription());
        assertEquals(TicketStatus.IN_PROGRESS, response.getStatus());
        assertEquals(TicketPriority.MEDIUM, response.getPriority());
        assertEquals(3L, response.getCategoryId());
        assertEquals("Network", response.getCategoryName());
        assertEquals(1L, response.getCreatedById());
        assertEquals("john", response.getCreatedByUsername());
        assertEquals(2L, response.getAssignedToId());
        assertEquals("agent", response.getAssignedToUsername());
        assertEquals(CREATED_AT, response.getCreatedAt());
        assertEquals(UPDATED_AT, response.getUpdatedAt());
    }

    @Test
    @DisplayName("toResponse: відсутні зв'язки дають null у відповідних полях відповіді")
    void toResponse_missingRelations_leavesNestedFieldsNull() {
        Ticket entity = Ticket.builder()
                .id(51L)
                .title("Unassigned")
                .status(TicketStatus.NEW)
                .priority(TicketPriority.LOW)
                .build();

        TicketResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(51L, response.getId());
        assertNull(response.getCategoryId());
        assertNull(response.getCategoryName());
        assertNull(response.getCreatedById());
        assertNull(response.getCreatedByUsername());
        assertNull(response.getAssignedToId());
        assertNull(response.getAssignedToUsername());
    }

    @Test
    @DisplayName("toResponse: повертає null для null сутності")
    void toResponse_nullEntity_returnsNull() {
        Ticket entity = null;

        TicketResponse response = mapper.toResponse(entity);

        assertNull(response);
    }
}
