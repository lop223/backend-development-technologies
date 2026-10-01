package net.lop223.helpdesksystem.controller.web;

import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.dto.request.TicketStatusUpdateRequest;
import net.lop223.helpdesksystem.service.AppUserService;
import net.lop223.helpdesksystem.service.CategoryService;
import net.lop223.helpdesksystem.service.TicketService;
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
    private final AppUserService appUserService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tickets", ticketService.getAll());
        model.addAttribute("statuses", TicketStatus.values());
        return "tickets/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("ticketRequest", new TicketRequest());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("users", appUserService.getAll());
        return "tickets/form";
    }

    @PostMapping
    public String create(@ModelAttribute TicketRequest ticketRequest,
                         org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser) {
        String creatorUsername = oidcUser.getPreferredUsername();
        ticketService.create(ticketRequest, creatorUsername);
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
}