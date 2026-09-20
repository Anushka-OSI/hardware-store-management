package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.dto.ProductForm;
import com.hardwarestore.hardwarestoremanagement.service.ProductService;
import com.hardwarestore.hardwarestoremanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;

@Controller
public class ProductController {

    private final ProductService productService;
    private final UserService userService;

    public ProductController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/products")
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "filter", required = false) String filter,
                       Model model) {
        var products = productService.search(q);
        if ("low".equals(filter)) products = productService.lowStockProducts();
        if ("out".equals(filter)) products = productService.outOfStockProducts();
        model.addAttribute("products", products);
        model.addAttribute("q", q);
        model.addAttribute("filter", filter);
        model.addAttribute("active", "products");
        return "products/list";
    }

    @GetMapping("/products/new")
    public String newForm(Model model) {
        model.addAttribute("productForm", new ProductForm(null, "", "", "", "", "",
                new BigDecimal("0.00"), 0, 5, null));
        model.addAttribute("suppliers", userService.allActiveSuppliers());
        model.addAttribute("active", "products");
        return "products/form";
    }

    @GetMapping("/products/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var p = productService.findById(id);
        model.addAttribute("productForm", new ProductForm(p.getId(), p.getSku(), p.getName(), p.getBrand(),
                p.getCategory(), p.getDescription(), p.getPrice(), p.getQuantityInStock(),
                p.getMinStockLevel(), p.getSupplier() != null ? p.getSupplier().getId() : null));
        model.addAttribute("suppliers", userService.allActiveSuppliers());
        model.addAttribute("active", "products");
        return "products/form";
    }

    @PostMapping("/products/save")
    public String save(@ModelAttribute ProductForm form,
                       @RequestParam(value = "photo", required = false) MultipartFile photo,
                       RedirectAttributes ra, Model model) {
        try {
            productService.save(form, photo);
            ra.addFlashAttribute("successMessage", "Product '" + form.name() + "' added.");
            return "redirect:/products";
        } catch (IllegalArgumentException | IOException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("productForm", form);
            model.addAttribute("suppliers", userService.allActiveSuppliers());
            return "products/form";
        }
    }

    @PostMapping("/products/update")
    public String update(@ModelAttribute ProductForm form,
                         @RequestParam(value = "photo", required = false) MultipartFile photo,
                         RedirectAttributes ra, Model model) {
        try {
            productService.update(form, photo);
            ra.addFlashAttribute("successMessage", "Product '" + form.name() + "' updated.");
            return "redirect:/products";
        } catch (IllegalArgumentException | IOException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("productForm", form);
            model.addAttribute("suppliers", userService.allActiveSuppliers());
            return "products/form";
        }
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("successMessage", "Product deleted.");
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/stock")
    public String updateStock(@PathVariable Long id,
                              @RequestParam int quantityInStock,
                              @RequestParam int minStockLevel,
                              RedirectAttributes ra) {
        productService.updateStock(id, quantityInStock, minStockLevel);
        ra.addFlashAttribute("successMessage", "Stock level updated.");
        return "redirect:/products";
    }
}