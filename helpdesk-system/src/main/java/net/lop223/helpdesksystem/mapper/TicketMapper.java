package net.lop223.helpdesksystem.mapper;

import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "createdBy.id", target = "createdById")
    @Mapping(source = "createdBy.username", target = "createdByUsername")
    @Mapping(source = "assignedTo.id", target = "assignedToId")
    @Mapping(source = "assignedTo.username", target = "assignedToUsername")
    TicketResponse toResponse(Ticket entity);
}