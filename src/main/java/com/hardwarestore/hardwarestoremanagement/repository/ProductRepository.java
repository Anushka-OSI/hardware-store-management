package com.hardwarestore.hardwarestoremanagement.repository;

import com.hardwarestore.hardwarestoremanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByNameAsc();

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

    @Query("""
        SELECT p FROM Product p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.category) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY p.name ASC
        """)
    List<Product> search(@Param("q") String q);

    List<Product> findByQuantityInStockLessThanEqualAndQuantityInStockGreaterThan(int lowLevel, int zero);
}