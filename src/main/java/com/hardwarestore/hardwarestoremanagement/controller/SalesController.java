package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.dto.CheckoutForm;
import com.hardwarestore.hardwarestoremanagement.entity.User;
import com.hardwarestore.hardwarestoremanagement.service.ProductService;
import com.hardwarestore.hardwarestoremanagement.service.ReportService;
import com.hardwarestore.hardwarestoremanagement.service.SaleService;
import com.hardwarestore.hardwarestoremanagement.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class SalesController {

    private final SaleService saleService;
    private final ProductService productService;
    private final UserService userService;
    private final ReportService reportService;

    public SalesController(SaleService saleService, ProductService productService,
                           UserService userService, ReportService reportService) {
        this.saleService = saleService;
        this.productService = productService;
        this.userService = userService;
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    public String list(@RequestParam(value = "date", required = false) LocalDate date, Model model) {
        LocalDate d = date == null ? LocalDate.now() : date;
        var sales = reportService.dailySales(d);
        model.addAttribute("sales", sales);
        model.addAttribute("date", d);
        model.addAttribute("total", reportService.sumTotal(sales));
        model.addAttribute("count", sales.size());
        model.addAttribute("active", "sales");
        return "sales/list";
    }

    @GetMapping("/sales/pos")
    public String pos(Model model) {
        model.addAttribute("products", productService.allProducts());
        model.addAttribute("active", "pos");
        return "sales/pos";
    }

    @PostMapping("/sales/checkout")
    public String checkout(@ModelAttribute CheckoutForm form,
                           @RequestParam BigDecimal amountPaid,
                           Authentication auth,
                           RedirectAttributes ra) {
        try {
            User cashier = userService.findByUsername(auth.getName()).orElseThrow();
            var sale = saleService.checkout(form, cashier, amountPaid);
            return "redirect:/sales/receipt/" + sale.getId();
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/sales/pos";
        }
    }

    @GetMapping("/sales/receipt/{id}")
    public String receipt(@PathVariable Long id, Model model) {
        model.addAttribute("sale", saleService.findById(id));
        model.addAttribute("active", "sales");
        return "sales/receipt";
    }
}