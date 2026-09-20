package com.hardwarestore.hardwarestoremanagement.service;

import com.hardwarestore.hardwarestoremanagement.entity.Category;
import com.hardwarestore.hardwarestoremanagement.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> allCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public Category save(String name, String description) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        categoryRepository.findByNameIgnoreCase(trimmed)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Category already exists: " + trimmed);
                });
        Category c = new Category();
        c.setName(trimmed);
        c.setDescription(description == null ? "" : description.trim());
        return categoryRepository.save(c);
    }

    @Transactional
    public Category update(Long id, String name, String description) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        categoryRepository.findByNameIgnoreCase(trimmed)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Category already exists: " + trimmed);
                });
        c.setName(trimmed);
        c.setDescription(description == null ? "" : description.trim());
        return categoryRepository.save(c);
    }

    @Transactional
    public void delete(Long id) {
        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Cannot delete: this category is still assigned to products.");
        }
    }
}
