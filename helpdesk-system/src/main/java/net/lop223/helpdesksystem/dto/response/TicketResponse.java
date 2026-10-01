package net.lop223.helpdesksystem.dto.response;

import lombok.Getter;
import lombok.Setter;
import net.lop223.helpdesksystem.utils.enums.TicketPriority;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;

    private Long categoryId;
    private String categoryName;

    private Long createdById;
    private String createdByUsername;

    private Long assignedToId;
    private String assignedToUsername;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}