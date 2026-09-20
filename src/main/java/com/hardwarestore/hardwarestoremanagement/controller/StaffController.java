package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.dto.StaffForm;
import com.hardwarestore.hardwarestoremanagement.entity.Role;
import com.hardwarestore.hardwarestoremanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
public class StaffController {

    private final UserService userService;

    public StaffController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/staff")
    public String list(Model model) {
        model.addAttribute("staff", userService.allUsers());
        model.addAttribute("active", "staff");
        return "staff/list";
    }

    @GetMapping("/staff/new")
    public String newForm(Model model) {
        model.addAttribute("staffForm", new StaffForm(null, "", "", "", "", "", Role.INVENTORY_MANAGER, true));
        model.addAttribute("allRoles", Arrays.asList(Role.ADMIN, Role.INVENTORY_MANAGER, Role.CASHIER, Role.SUPPLIER));
        model.addAttribute("active", "staff");
        return "staff/form";
    }

    @PostMapping("/staff/save")
    public String save(@ModelAttribute StaffForm form, RedirectAttributes ra, Model model) {
        try {
            if (form.role() == Role.SUPPLIER) {
                throw new IllegalArgumentException("Please create suppliers from the Suppliers section.");
            }
            userService.createStaff(form);
            ra.addFlashAttribute("successMessage", "Staff account created successfully.");
            return "redirect:/staff";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("staffForm", form);
            model.addAttribute("allRoles", Arrays.asList(Role.ADMIN, Role.INVENTORY_MANAGER, Role.CASHIER, Role.SUPPLIER));
            return "staff/form";
        }
    }

    @GetMapping("/staff/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var user = userService.findById(id).orElseThrow();
        model.addAttribute("staffForm", new StaffForm(user.getId(), user.getUsername(), user.getFullName(),
                user.getEmail(), user.getPhone(), "", user.getRole(), user.isActive()));
        model.addAttribute("allRoles", Arrays.asList(Role.ADMIN, Role.INVENTORY_MANAGER, Role.CASHIER, Role.SUPPLIER));
        model.addAttribute("active", "staff");
        return "staff/form";
    }

    @PostMapping("/staff/update")
    public String update(@ModelAttribute StaffForm form, RedirectAttributes ra, Model model) {
        try {
            userService.updateStaff(form);
            ra.addFlashAttribute("successMessage", "Staff account updated.");
            return "redirect:/staff";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("staffForm", form);
            model.addAttribute("allRoles", Arrays.asList(Role.ADMIN, Role.INVENTORY_MANAGER, Role.CASHIER, Role.SUPPLIER));
            return "staff/form";
        }
    }

    @PostMapping("/staff/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes ra) {
        var user = userService.findById(id).orElseThrow();
        userService.setActive(id, !user.isActive());
        ra.addFlashAttribute("successMessage",
                user.getUsername() + (user.isActive() ? " deactivated." : " activated."));
        return "redirect:/staff";
    }
}