package com.hardwarestore.hardwarestoremanagement.service;

import com.hardwarestore.hardwarestoremanagement.dto.CheckoutForm;
import com.hardwarestore.hardwarestoremanagement.entity.PaymentMethod;
import com.hardwarestore.hardwarestoremanagement.entity.Product;
import com.hardwarestore.hardwarestoremanagement.entity.Sale;
import com.hardwarestore.hardwarestoremanagement.entity.SaleItem;
import com.hardwarestore.hardwarestoremanagement.entity.User;
import com.hardwarestore.hardwarestoremanagement.repository.ProductRepository;
import com.hardwarestore.hardwarestoremanagement.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    public Sale findById(Long id) {
        return saleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Sale not found"));
    }

    public List<Sale> salesBetween(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(start, end);
    }

    public Long countSalesBetween(LocalDateTime start, LocalDateTime end) {
        return saleRepository.countBySaleDateBetween(start, end);
    }

    @Transactional
    public Sale checkout(CheckoutForm form, User cashier, BigDecimal amountPaid) throws IllegalStateException {
        if (form.productIds() == null || form.productIds().isEmpty()) {
            throw new IllegalStateException("Cart is empty. Add items before checkout.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discount = form.discount() == null ? BigDecimal.ZERO : form.discount();
        List<SaleItem> items = new ArrayList<>();
        List<Product> productsToUpdate = new ArrayList<>();

        for (int i = 0; i < form.productIds().size(); i++) {
            Long pid = form.productIds().get(i);
            Integer qty = form.quantities() != null && i < form.quantities().size() ? form.quantities().get(i) : 0;
            if (pid == null || qty == null || qty <= 0) continue;

            Product p = productRepository.findById(pid).orElseThrow(() -> new IllegalStateException("Product not found."));
            if (p.isOutOfStock()) {
                throw new IllegalStateException(p.getName() + " is out of stock and cannot be sold.");
            }
            if (p.getQuantityInStock() < qty) {
                throw new IllegalStateException("Insufficient stock for " + p.getName() + " (only " + p.getQuantityInStock() + " left).");
            }
            SaleItem si = new SaleItem();
            si.setProduct(p);
            si.setQuantity(qty);
            si.setUnitPrice(p.getPrice());
            si.setLineTotal(p.getPrice().multiply(BigDecimal.valueOf(qty)));
            items.add(si);
            subtotal = subtotal.add(si.getLineTotal());
            productsToUpdate.add(p);
        }

        if (items.isEmpty()) throw new IllegalStateException("Cart is empty. Add items before checkout.");
        if (discount.signum() < 0 || discount.compareTo(subtotal) > 0) {
            throw new IllegalStateException("Discount cannot exceed the subtotal.");
        }
        BigDecimal total = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        if (amountPaid == null || amountPaid.compareTo(total) < 0) {
            throw new IllegalStateException("Full payment not completed. Total is " + total.setScale(2) + " but paid " + (amountPaid == null ? BigDecimal.ZERO : amountPaid));
        }

        Sale sale = new Sale();
        sale.setInvoiceNumber(generateInvoiceNumber());
        sale.setCashier(cashier);
        sale.setSaleDate(LocalDateTime.now());
        sale.setSubtotal(subtotal);
        sale.setDiscount(discount);
        sale.setTotal(total);
        sale.setPaymentMethod(PaymentMethod.valueOf(form.paymentMethod().name()));
        sale.setAmountPaid(amountPaid);
        sale.setChangeAmount(amountPaid.subtract(total));
        items.forEach(si -> si.setSale(sale));
        sale.setItems(items);
        saleRepository.save(sale);

        for (Product p : productsToUpdate) {
            p.setQuantityInStock(p.getQuantityInStock() - items.stream()
                    .filter(si -> si.getProduct().getId().equals(p.getId()))
                    .mapToInt(SaleItem::getQuantity).sum());
            productRepository.save(p);
        }
        return sale;
    }

    private String generateInvoiceNumber() {
        return "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}