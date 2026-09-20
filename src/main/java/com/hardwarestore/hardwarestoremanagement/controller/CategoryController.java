package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.dto.CategoryForm;
import com.hardwarestore.hardwarestoremanagement.entity.Category;
import com.hardwarestore.hardwarestoremanagement.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.allCategories());
        model.addAttribute("active", "categories");
        return "categories/list";
    }

    @GetMapping("/categories/new")
    public String newForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm(null, "", ""));
        model.addAttribute("active", "categories");
        return "categories/form";
    }

    @GetMapping("/categories/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Category c = categoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        model.addAttribute("categoryForm", new CategoryForm(c.getId(), c.getName(), c.getDescription()));
        model.addAttribute("active", "categories");
        return "categories/form";
    }

    @PostMapping("/categories/save")
    public String save(@RequestParam String name, @RequestParam(required = false) String description,
                       RedirectAttributes ra) {
        try {
            categoryService.save(name, description);
            ra.addFlashAttribute("successMessage", "Category created.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/categories/update")
    public String update(@RequestParam Long id, @RequestParam String name,
                         @RequestParam(required = false) String description, RedirectAttributes ra) {
        try {
            categoryService.update(id, name, description);
            ra.addFlashAttribute("successMessage", "Category updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.delete(id);
            ra.addFlashAttribute("successMessage", "Category deleted.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }
}
