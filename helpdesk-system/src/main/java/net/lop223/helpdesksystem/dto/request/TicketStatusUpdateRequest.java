package net.lop223.helpdesksystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;

@Getter
@Setter
public class TicketStatusUpdateRequest {

    @NotNull(message = "Status must be specified")
    private TicketStatus status;
}