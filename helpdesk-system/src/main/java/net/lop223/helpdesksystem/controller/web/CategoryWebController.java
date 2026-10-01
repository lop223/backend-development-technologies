package net.lop223.helpdesksystem.controller.web;

import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.request.CategoryRequest;
import net.lop223.helpdesksystem.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ui/categories")
@RequiredArgsConstructor
public class CategoryWebController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        return "categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("categoryRequest", new CategoryRequest());
        model.addAttribute("isEdit", false);
        return "categories/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var category = categoryService.getById(id);
        CategoryRequest request = new CategoryRequest();
        request.setName(category.getName());
        request.setDescription(category.getDescription());
        model.addAttribute("categoryRequest", request);
        model.addAttribute("categoryId", id);
        model.addAttribute("isEdit", true);
        return "categories/form";
    }

    @PostMapping
    public String create(@ModelAttribute CategoryRequest categoryRequest) {
        categoryService.create(categoryRequest);
        return "redirect:/ui/categories";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute CategoryRequest categoryRequest) {
        categoryService.update(id, categoryRequest);
        return "redirect:/ui/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        categoryService.delete(id);
        return "redirect:/ui/categories";
    }
}