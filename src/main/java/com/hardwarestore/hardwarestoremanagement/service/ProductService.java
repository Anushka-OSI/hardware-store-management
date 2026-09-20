package com.hardwarestore.hardwarestoremanagement.service;

import com.hardwarestore.hardwarestoremanagement.dto.ProductForm;
import com.hardwarestore.hardwarestoremanagement.entity.Product;
import com.hardwarestore.hardwarestoremanagement.entity.User;
import com.hardwarestore.hardwarestoremanagement.repository.ProductRepository;
import com.hardwarestore.hardwarestoremanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Product> allProducts() {
        return productRepository.findAllByOrderByNameAsc();
    }

    public List<Product> search(String q) {
        if (q == null || q.isBlank()) return allProducts();
        return productRepository.search(q.trim());
    }

    public List<Product> lowStockProducts() {
        return productRepository.findAllByOrderByNameAsc().stream().filter(Product::isLowStock).toList();
    }

    public List<Product> outOfStockProducts() {
        return productRepository.findAllByOrderByNameAsc().stream().filter(Product::isOutOfStock).toList();
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    @Transactional
    public Product save(ProductForm form, MultipartFile photo) throws IOException {
        if (productRepository.existsBySku(form.sku())) {
            throw new IllegalArgumentException("SKU already exists: " + form.sku());
        }
        Product p = new Product();
        applyForm(p, form);
        handlePhoto(p, photo);
        return productRepository.save(p);
    }

    @Transactional
    public Product update(ProductForm form, MultipartFile photo) throws IOException {
        Product p = productRepository.findById(form.id()).orElseThrow();
        applyForm(p, form);
        if (photo != null && !photo.isEmpty()) {
            handlePhoto(p, photo);
        }
        return productRepository.save(p);
    }

    private void applyForm(Product p, ProductForm form) {
        p.setSku(form.sku().trim());
        p.setName(form.name().trim());
        p.setBrand(form.brand());
        p.setCategory(form.category());
        p.setDescription(form.description());
        p.setPrice(form.price());
        p.setQuantityInStock(form.quantityInStock());
        p.setMinStockLevel(form.minStockLevel());
        if (form.supplierId() != null) {
            User supplier = userRepository.findById(form.supplierId()).orElse(null);
            p.setSupplier(supplier);
        } else {
            p.setSupplier(null);
        }
    }

    private void handlePhoto(Product p, MultipartFile photo) throws IOException {
        if (photo == null || photo.isEmpty()) return;
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        String filename = UUID.randomUUID() + "-" + photo.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        photo.transferTo(dir.resolve(filename).toFile());
        p.setPhotoPath(filename);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public Product updateStock(Long id, int newQuantity, int minLevel) {
        Product p = productRepository.findById(id).orElseThrow();
        p.setQuantityInStock(Math.max(0, newQuantity));
        p.setMinStockLevel(Math.max(0, minLevel));
        return productRepository.save(p);
    }
}