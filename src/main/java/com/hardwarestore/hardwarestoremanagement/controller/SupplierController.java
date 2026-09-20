package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.dto.SupplierForm;
import com.hardwarestore.hardwarestoremanagement.service.PurchaseOrderService;
import com.hardwarestore.hardwarestoremanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SupplierController {

    private final UserService userService;
    private final PurchaseOrderService poService;

    public SupplierController(UserService userService, PurchaseOrderService poService) {
        this.userService = userService;
        this.poService = poService;
    }

    @GetMapping("/suppliers")
    public String list(Model model) {
        model.addAttribute("suppliers", userService.suppliers());
        model.addAttribute("active", "suppliers");
        return "suppliers/list";
    }

    @GetMapping("/suppliers/new")
    public String newForm(Model model) {
        model.addAttribute("supplierForm", new SupplierForm(null, "", "", "", "", "", "", "", "", true));
        model.addAttribute("active", "suppliers");
        return "suppliers/form";
    }

    @PostMapping("/suppliers/save")
    public String save(@ModelAttribute SupplierForm form, RedirectAttributes ra, Model model) {
        try {
            userService.createSupplier(form);
            ra.addFlashAttribute("successMessage", "Supplier added successfully.");
            return "redirect:/suppliers";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("supplierForm", form);
            return "suppliers/form";
        }
    }

    @GetMapping("/suppliers/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var u = userService.findById(id).orElseThrow();
        model.addAttribute("supplierForm", new SupplierForm(u.getId(), u.getUsername(), u.getFullName(),
                u.getEmail(), u.getPhone(), "", u.getBusinessName(), u.getContactPerson(), u.getAddress(), u.isActive()));
        model.addAttribute("active", "suppliers");
        return "suppliers/form";
    }

    @PostMapping("/suppliers/update")
    public String update(@ModelAttribute SupplierForm form, RedirectAttributes ra, Model model) {
        try {
            userService.updateSupplier(form);
            ra.addFlashAttribute("successMessage", "Supplier updated.");
            return "redirect:/suppliers";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("supplierForm", form);
            return "suppliers/form";
        }
    }

    @PostMapping("/suppliers/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes ra) {
        var u = userService.findById(id).orElseThrow();
        userService.setActive(id, !u.isActive());
        ra.addFlashAttribute("successMessage",
                u.getBusinessName() != null ? u.getBusinessName() : u.getUsername()
                        + (u.isActive() ? " deactivated." : " activated."));
        return "redirect:/suppliers";
    }

    @GetMapping("/suppliers/{id}/purchase-history")
    public String purchaseHistory(@PathVariable Long id, Model model) {
        var supplier = userService.findById(id).orElseThrow();
        model.addAttribute("supplier", supplier);
        model.addAttribute("orders", poService.ordersForSupplier(id));
        model.addAttribute("active", "suppliers");
        return "suppliers/history";
    }
}