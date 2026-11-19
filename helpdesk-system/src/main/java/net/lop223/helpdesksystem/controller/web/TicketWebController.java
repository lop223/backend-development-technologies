package net.lop223.helpdesksystem.controller.web;

import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.dto.request.TicketStatusUpdateRequest;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.service.CategoryService;
import net.lop223.helpdesksystem.service.TicketService;
import net.lop223.helpdesksystem.service.userclient.UserServiceClient;
import net.lop223.helpdesksystem.utils.annotation.CurrentUsername;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ui/tickets")
@RequiredArgsConstructor
public class TicketWebController {

    private final TicketService ticketService;
    private final CategoryService categoryService;
    private final UserServiceClient userServiceClient;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tickets", ticketService.getAll());
        model.addAttribute("statuses", TicketStatus.values());
        return "tickets/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        populateForm(model, new TicketRequest(), "/ui/tickets", "Нова заявка");
        return "tickets/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        TicketResponse ticket = ticketService.getById(id);
        TicketRequest request = new TicketRequest();
        request.setTitle(ticket.getTitle());
        request.setDescription(ticket.getDescription());
        request.setPriority(ticket.getPriority());
        request.setCategoryId(ticket.getCategoryId());
        request.setAssignedToId(ticket.getAssignedToId());
        populateForm(model, request, "/ui/tickets/" + id, "Редагувати заявку");
        return "tickets/form";
    }

    @PostMapping
    public String create(@ModelAttribute TicketRequest ticketRequest,
                         @CurrentUsername String creatorUsername) {
        java.util.Objects.requireNonNull(creatorUsername, "Username cannot be null");
        ticketService.create(ticketRequest, creatorUsername);
        return "redirect:/ui/tickets";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute TicketRequest ticketRequest) {
        ticketService.update(id, ticketRequest);
        return "redirect:/ui/tickets";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam TicketStatus status) {
        TicketStatusUpdateRequest request = new TicketStatusUpdateRequest();
        request.setStatus(status);
        ticketService.updateStatus(id, request);
        return "redirect:/ui/tickets";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        ticketService.delete(id);
        return "redirect:/ui/tickets";
    }

    private void populateForm(Model model, TicketRequest ticketRequest, String formAction, String formTitle) {
        model.addAttribute("ticketRequest", ticketRequest);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("users", userServiceClient.getAll());
        model.addAttribute("formAction", formAction);
        model.addAttribute("formTitle", formTitle);
    }
}