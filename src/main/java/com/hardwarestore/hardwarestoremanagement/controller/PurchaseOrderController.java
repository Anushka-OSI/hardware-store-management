package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.dto.PurchaseOrderForm;
import com.hardwarestore.hardwarestoremanagement.service.ProductService;
import com.hardwarestore.hardwarestoremanagement.service.PurchaseOrderService;
import com.hardwarestore.hardwarestoremanagement.service.UserService;
import com.hardwarestore.hardwarestoremanagement.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PurchaseOrderController {

    private final PurchaseOrderService poService;
    private final UserService userService;
    private final ProductService productService;

    public PurchaseOrderController(PurchaseOrderService poService, UserService userService, ProductService productService) {
        this.poService = poService;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping("/purchase-orders")
    public String list(Model model, Authentication auth) {
        User current = userService.findByUsername(auth.getName()).orElse(null);
        String role = current != null ? current.getRole().name() : "";

        if ("SUPPLIER".equals(role)) {
            model.addAttribute("orders", poService.ordersForSupplier(current.getId()));
            model.addAttribute("supplierView", true);
        } else {
            model.addAttribute("orders", poService.allOrders());
            model.addAttribute("activeOrders", poService.allActiveOrders());
            model.addAttribute("supplierView", false);
        }
        model.addAttribute("historyView", false);
        model.addAttribute("active", "purchase-orders");
        return "purchase-orders/list";
    }

    @GetMapping("/purchase-orders/history")
    public String history(Model model) {
        model.addAttribute("orders", poService.history());
        model.addAttribute("historyView", true);
        model.addAttribute("supplierView", false);
        model.addAttribute("active", "purchase-orders");
        return "purchase-orders/list";
    }

    @GetMapping("/purchase-orders/new")
    public String newForm(Model model) {
        model.addAttribute("suppliers", userService.allActiveSuppliers());
        model.addAttribute("products", productService.allProducts());
        model.addAttribute("today", java.time.LocalDate.now().toString());
        model.addAttribute("active", "purchase-orders");
        return "purchase-orders/form";
    }

    @PostMapping("/purchase-orders/save")
    public String save(@RequestParam Long supplierId,
                       @RequestParam(required = false) String orderDate,
                       @RequestParam(required = false) String expectedDeliveryDate,
                       @RequestParam(required = false) String notes,
                       @RequestParam(required = false) List<Long> productIds,
                       @RequestParam(required = false) List<Integer> quantities,
                       @RequestParam(required = false) List<String> unitCosts,
                       Authentication auth, RedirectAttributes ra, Model model) {
        try {
            var form = new PurchaseOrderForm(supplierId,
                    orderDate == null || orderDate.isBlank() ? null : java.time.LocalDate.parse(orderDate),
                    expectedDeliveryDate == null || expectedDeliveryDate.isBlank() ? null : java.time.LocalDate.parse(expectedDeliveryDate),
                    notes, productIds, quantities,
                    unitCosts == null ? null : unitCosts.stream()
                            .map(c -> c == null || c.isBlank() ? null : new java.math.BigDecimal(c)).toList());
            User creator = userService.findByUsername(auth.getName()).orElseThrow();
            var po = poService.create(form, creator);
            ra.addFlashAttribute("successMessage", "Purchase order " + po.getPoNumber() + " created.");
            return "redirect:/purchase-orders/" + po.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("suppliers", userService.allActiveSuppliers());
            model.addAttribute("products", productService.allProducts());
            model.addAttribute("today", java.time.LocalDate.now().toString());
            model.addAttribute("active", "purchase-orders");
            return "purchase-orders/form";
        }
    }

    @GetMapping("/purchase-orders/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication auth) {
        User current = userService.findByUsername(auth.getName()).orElse(null);
        model.addAttribute("order", poService.findById(id));
        model.addAttribute("canEdit", current != null
                && !"SUPPLIER".equals(current.getRole().name())
                && poService.canEdit(poService.findById(id)));
        model.addAttribute("products", productService.allProducts());
        model.addAttribute("active", "purchase-orders");
        return "purchase-orders/detail";
    }

    @PostMapping("/purchase-orders/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra, Authentication auth) {
        User current = userService.findByUsername(auth.getName()).orElse(null);
        if (current == null || "SUPPLIER".equals(current.getRole().name())) {
            ra.addFlashAttribute("errorMessage", "Only staff can cancel purchase orders.");
            return "redirect:/purchase-orders/" + id;
        }
        try {
            poService.cancel(id);
            ra.addFlashAttribute("successMessage", "Purchase order cancelled.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/purchase-orders/" + id;
    }

    @PostMapping("/purchase-orders/{id}/record-delivery")
    public String recordDelivery(@PathVariable Long id,
                                 @RequestParam(required = false) List<Long> itemIds,
                                 @RequestParam(required = false) List<Integer> receivedQty,
                                 @RequestParam(required = false) List<String> notes,
                                 RedirectAttributes ra, Authentication auth) {
        try {
            poService.recordDelivery(id, itemIds, receivedQty, notes);
            ra.addFlashAttribute("successMessage", "Deliveries recorded. Staff will confirm to add stock to inventory.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/purchase-orders/" + id;
    }

    @PostMapping("/purchase-orders/{id}/confirm")
    public String confirm(@PathVariable Long id, RedirectAttributes ra, Authentication auth) {
        User current = userService.findByUsername(auth.getName()).orElse(null);
        if (current == null || "SUPPLIER".equals(current.getRole().name())) {
            ra.addFlashAttribute("errorMessage", "Only staff can confirm deliveries and add stock.");
            return "redirect:/purchase-orders/" + id;
        }
        var po = poService.confirmAndAddStock(id);
        ra.addFlashAttribute("successMessage", "Delivery confirmed for " + po.getPoNumber()
                + " - received stock added to inventory.");
        return "redirect:/purchase-orders/" + id;
    }
}