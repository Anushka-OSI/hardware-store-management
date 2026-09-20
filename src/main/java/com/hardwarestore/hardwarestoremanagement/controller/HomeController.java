package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.dto.RequestForm;
import com.hardwarestore.hardwarestoremanagement.entity.CustomerRequest;
import com.hardwarestore.hardwarestoremanagement.service.ProductService;
import com.hardwarestore.hardwarestoremanagement.service.RequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final ProductService productService;
    private final RequestService requestService;

    public HomeController(ProductService productService, RequestService requestService) {
        this.productService = productService;
        this.requestService = requestService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/catalogue";
    }

    // ============ CUSTOMER CATALOGUE (public, no login) ============
    @GetMapping("/catalogue")
    public String catalogue(@RequestParam(value = "q", required = false) String q,
                            @RequestParam(value = "category", required = false) String category,
                            Model model) {
        var products = productService.search(q);
        if (category != null && !category.isBlank()) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().equalsIgnoreCase(category)).toList();
        }
        model.addAttribute("products", products);
        model.addAttribute("q", q);
        model.addAttribute("categories", productService.allProducts().stream()
                .map(p -> p.getCategory()).filter(c -> c != null && !c.isBlank()).distinct().sorted().toList());
        model.addAttribute("active", "catalogue");
        return "catalogue";
    }

    // ============ CUSTOMER REQUEST / ISSUE FORM (public, no login) ============
    @GetMapping("/request")
    public String requestForm(Model model) {
        model.addAttribute("requestForm", new RequestForm("", "", ""));
        model.addAttribute("active", "request");
        return "request-form";
    }

    @PostMapping("/request/submit")
    public String submitRequest(@ModelAttribute RequestForm requestForm, Model model) {
        if (requestForm.customerName() == null || requestForm.customerName().isBlank()
                || requestForm.customerPhone() == null || requestForm.customerPhone().isBlank()
                || requestForm.message() == null || requestForm.message().isBlank()) {
            model.addAttribute("errorMessage", "Please fill in your name, phone number and message.");
            model.addAttribute("requestForm", requestForm);
            return "request-form";
        }
        CustomerRequest created = requestService.submit(requestForm);
        model.addAttribute("requestId", created.getRequestId());
        model.addAttribute("active", "request");
        return "request-thankyou";
    }

    // ============ CUSTOMER REQUEST STATUS CHECK (public, no login) ============
    @GetMapping("/request/status")
    public String statusForm(Model model) {
        model.addAttribute("active", "status");
        return "request-status";
    }

    @PostMapping("/request/check")
    public String checkStatus(@RequestParam String requestId, Model model) {
        model.addAttribute("active", "status");
        var found = requestService.findByRequestId(requestId);
        if (found.isEmpty()) {
            model.addAttribute("errorMessage", "No request found with ID " + requestId + ". Please check and try again.");
        } else {
            CustomerRequest r = found.get();
            model.addAttribute("request", r);
            model.addAttribute("lastChecked", r.getRequestId());
        }
        return "request-status";
    }
}