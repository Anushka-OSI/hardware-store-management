package com.hardwarestore.hardwarestoremanagement.service;

import com.hardwarestore.hardwarestoremanagement.dto.PurchaseOrderForm;
import com.hardwarestore.hardwarestoremanagement.entity.OrderStatus;
import com.hardwarestore.hardwarestoremanagement.entity.Product;
import com.hardwarestore.hardwarestoremanagement.entity.PurchaseOrder;
import com.hardwarestore.hardwarestoremanagement.entity.PurchaseOrderItem;
import com.hardwarestore.hardwarestoremanagement.entity.User;
import com.hardwarestore.hardwarestoremanagement.repository.ProductRepository;
import com.hardwarestore.hardwarestoremanagement.repository.PurchaseOrderRepository;
import com.hardwarestore.hardwarestoremanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public PurchaseOrderService(PurchaseOrderRepository poRepository,
                                UserRepository userRepository,
                                ProductRepository productRepository) {
        this.poRepository = poRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public List<PurchaseOrder> allOrders() {
        return poRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<PurchaseOrder> allActiveOrders() {
        return poRepository.findByStatusInOrderByCreatedAtDesc(
                List.of(OrderStatus.PENDING, OrderStatus.PLACED, OrderStatus.PARTIALLY_RECEIVED));
    }

    public List<PurchaseOrder> history() {
        return poRepository.findByStatusInOrderByCreatedAtDesc(
                List.of(OrderStatus.RECEIVED, OrderStatus.CANCELLED));
    }

    public List<PurchaseOrder> ordersForSupplier(Long supplierId) {
        return poRepository.findBySupplierIdOrderByCreatedAtDesc(supplierId);
    }

    public PurchaseOrder findById(Long id) {
        return poRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrderForm form, User createdBy) {
        User supplier = userRepository.findById(form.supplierId()).orElseThrow();
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        po.setSupplier(supplier);
        po.setCreatedBy(createdBy);
        po.setOrderDate(form.orderDate() == null ? java.time.LocalDate.now() : form.orderDate());
        po.setExpectedDeliveryDate(form.expectedDeliveryDate());
        po.setNotes(form.notes());
        po.setStatus(OrderStatus.PLACED);
        populateItems(po, form);
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder update(Long poId, PurchaseOrderForm form) {
        PurchaseOrder po = poRepository.findById(poId).orElseThrow();
        if (!canEdit(po)) throw new IllegalStateException("This order can no longer be edited.");
        User supplier = userRepository.findById(form.supplierId()).orElseThrow();
        po.setSupplier(supplier);
        if (form.orderDate() != null) po.setOrderDate(form.orderDate());
        po.setExpectedDeliveryDate(form.expectedDeliveryDate());
        po.setNotes(form.notes());
        po.getItems().clear();
        populateItems(po, form);
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder cancel(Long poId) {
        PurchaseOrder po = poRepository.findById(poId).orElseThrow();
        if (po.getStatus() == OrderStatus.RECEIVED || po.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("This order cannot be cancelled.");
        }
        po.setStatus(OrderStatus.CANCELLED);
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder recordDelivery(Long poId, List<Long> itemIds, List<Integer> receivedQty, List<String> notes) {
        PurchaseOrder po = poRepository.findById(poId).orElseThrow();
        if (po.getStatus() == OrderStatus.CANCELLED || po.getStatus() == OrderStatus.RECEIVED) {
            throw new IllegalStateException("This order cannot receive deliveries.");
        }
        int i = 0;
        boolean anyReceived = false;
        for (PurchaseOrderItem item : po.getItems()) {
            if (itemIds != null && itemIds.contains(item.getId()) && receivedQty != null && i < receivedQty.size() && receivedQty.get(i) != null) {
                int qty = Math.min(receivedQty.get(i), item.getQuantityOrdered());
                item.setQuantityReceived(Math.max(0, qty));
            }
            if (notes != null && i < notes.size() && notes.get(i) != null && !notes.get(i).isBlank()) {
                item.setNotes(notes.get(i));
            }
            if (item.getQuantityReceived() > 0) anyReceived = true;
            i++;
        }
        if (anyReceived) {
            po.setStatus(OrderStatus.PARTIALLY_RECEIVED);
        }
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder confirmAndAddStock(Long poId) {
        PurchaseOrder po = poRepository.findById(poId).orElseThrow();
        if (po.getStatus() == OrderStatus.CANCELLED) throw new IllegalStateException("Cancelled order cannot be confirmed.");
        if (po.getStatus() != OrderStatus.RECEIVED) {
            po.setStatus(OrderStatus.RECEIVED);
        }
        for (PurchaseOrderItem item : po.getItems()) {
            if (item.getQuantityReceived() > 0) {
                Product p = item.getProduct();
                p.setQuantityInStock(p.getQuantityInStock() + item.getQuantityReceived());
                productRepository.save(p);
            }
        }
        return poRepository.save(po);
    }

    private void populateItems(PurchaseOrder po, PurchaseOrderForm form) {
        if (form.productIds() == null || form.productIds().isEmpty()) {
            throw new IllegalArgumentException("Add at least one product to the order.");
        }
        for (int i = 0; i < form.productIds().size(); i++) {
            Long pid = form.productIds().get(i);
            Integer qty = form.quantities() != null && i < form.quantities().size() ? form.quantities().get(i) : 0;
            BigDecimal cost = form.unitCosts() != null && i < form.unitCosts().size() ? form.unitCosts().get(i) : BigDecimal.ZERO;
            if (pid == null || qty == null || qty <= 0) continue;
            Product p = productRepository.findById(pid).orElseThrow();
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setProduct(p);
            item.setQuantityOrdered(qty);
            item.setUnitCost(cost == null ? BigDecimal.ZERO : cost);
            item.setQuantityReceived(0);
            po.getItems().add(item);
        }
        if (po.getItems().isEmpty()) {
            throw new IllegalArgumentException("Add at least one product with quantity to the order.");
        }
    }

    public boolean canEdit(PurchaseOrder po) {
        return po.getStatus() == OrderStatus.PENDING || po.getStatus() == OrderStatus.PLACED;
    }
}