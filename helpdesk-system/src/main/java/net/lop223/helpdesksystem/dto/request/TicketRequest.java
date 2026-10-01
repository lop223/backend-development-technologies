package net.lop223.helpdesksystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import net.lop223.helpdesksystem.utils.enums.TicketPriority;

@Getter
@Setter
public class TicketRequest {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Priority must be specified")
    private TicketPriority priority;

    @NotNull(message = "Category id must be specified")
    private Long categoryId;

    private Long assignedToId;
}