package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.dto.ProductForm;
import com.hardwarestore.hardwarestoremanagement.entity.Product;
import com.hardwarestore.hardwarestoremanagement.service.CategoryService;
import com.hardwarestore.hardwarestoremanagement.service.ProductService;
import com.hardwarestore.hardwarestoremanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProductController {

    private final ProductService productService;
    private final UserService userService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, UserService userService, CategoryService categoryService) {
        this.productService = productService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @GetMapping("/products")
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "filter", required = false) String filter,
                       @RequestParam(value = "overallFile", required = false) Object overallFile,
                       Model model) {
        var products = productService.search(q);
        if ("low".equals(filter)) {
            products = products.stream().filter(p -> p.getQuantityInStock() <= p.getMinStockLevel()).toList();
        } else if ("out".equals(filter)) {
            products = products.stream().filter(p -> p.getQuantityInStock() == 0).toList();
        }
        if (q != null && !q.isBlank() && products.size() > 3) {
            products = products.stream().limit(15).toList();
        }
        model.addAttribute("products", products);
        model.addAttribute("q", q);
        model.addAttribute("filter", filter);
        model.addAttribute("active", "products");
        return "products/list";
    }

    @GetMapping("/products/new")
    public String newForm(Model model) {
        model.addAttribute("productForm", new ProductForm(null, "", "", "", "", "",
                null, 0, 0, null));
        model.addAttribute("suppliers", userService.allActiveSuppliers());
        model.addAttribute("categories", categoryService.allCategories());
        model.addAttribute("active", "products");
        return "products/form";
    }

    @GetMapping("/products/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Product p = productService.findById(id);
        model.addAttribute("productForm", new ProductForm(p.getId(), p.getSku(), p.getName(), p.getBrand(),
                p.getCategory(), p.getDescription(), p.getPrice(), p.getQuantityInStock(), p.getMinStockLevel(),
                p.getSupplier() == null ? null : p.getSupplier().getId()));
        model.addAttribute("suppliers", userService.allActiveSuppliers());
        model.addAttribute("categories", categoryService.allCategories());
        model.addAttribute("active", "products");
        return "products/form";
    }

    @PostMapping("/products/save")
    public String save(@ModelAttribute ProductForm form,
                       @RequestParam(value = "photo", required = false) MultipartFile photo,
                       RedirectAttributes ra) {
        try {
            productService.save(form, photo);
            ra.addFlashAttribute("successMessage", "Product '" + form.name() + "' created.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IOException ex) {
            ra.addFlashAttribute("errorMessage", "Could not store the photo: " + ex.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/products/update")
    public String update(@ModelAttribute ProductForm form,
                         @RequestParam(value = "photo", required = false) MultipartFile photo,
                         RedirectAttributes ra) {
        try {
            productService.update(form, photo);
            ra.addFlashAttribute("successMessage", "Product '" + form.name() + "' updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IOException ex) {
            ra.addFlashAttribute("errorMessage", "Could not store the photo: " + ex.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.delete(id);
            ra.addFlashAttribute("successMessage", "Product deleted.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/products/export")
    public ModelAndView export() {
        return new ModelAndView("redirect:/products");
    }

    @PostMapping("/products/export")
    public String exportCsv() {
        return "redirect:/products";
    }

    @GetMapping("/products/{id}/stock")
    public String stockAdjust(@PathVariable Long id, Model model) {
        Product p = productService.findById(id);
        model.addAttribute("p", p);
        model.addAttribute("active", "products");
        return "products/stock";
    }

    @PostMapping("/products/{id}/stock")
    public String stockAdjustSave(@PathVariable Long id, @RequestParam int newQuantity, RedirectAttributes ra) {
        productService.updateStock(id, newQuantity, productService.findById(id).getMinStockLevel());
        ra.addFlashAttribute("successMessage", "Stock updated.");
        return "redirect:/products";
    }
}
