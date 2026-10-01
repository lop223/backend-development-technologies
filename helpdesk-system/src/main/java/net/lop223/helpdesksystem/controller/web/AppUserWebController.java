package net.lop223.helpdesksystem.controller.web;

import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.request.AppUserRequest;
import net.lop223.helpdesksystem.service.AppUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ui/users")
@RequiredArgsConstructor
public class AppUserWebController {

    private final AppUserService appUserService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", appUserService.getAll());
        return "users/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("appUserRequest", new AppUserRequest());
        model.addAttribute("isEdit", false);
        return "users/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var user = appUserService.getById(id);
        AppUserRequest request = new AppUserRequest();
        request.setUsername(user.getUsername());
        request.setEmail(user.getEmail());
        request.setFullName(user.getFullName());
        request.setRole(user.getRole());
        model.addAttribute("appUserRequest", request);
        model.addAttribute("userId", id);
        model.addAttribute("isEdit", true);
        return "users/form";
    }

    @PostMapping
    public String create(@ModelAttribute AppUserRequest appUserRequest) {
        appUserService.create(appUserRequest);
        return "redirect:/ui/users";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute AppUserRequest appUserRequest) {
        appUserService.update(id, appUserRequest);
        return "redirect:/ui/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        appUserService.delete(id);
        return "redirect:/ui/users";
    }
}