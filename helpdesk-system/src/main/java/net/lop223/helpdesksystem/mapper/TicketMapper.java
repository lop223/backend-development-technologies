package net.lop223.helpdesksystem.mapper;

import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    TicketResponse toResponse(Ticket entity);
}