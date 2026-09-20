package com.hardwarestore.hardwarestoremanagement.config;

import com.hardwarestore.hardwarestoremanagement.entity.*;
import com.hardwarestore.hardwarestoremanagement.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CustomerRequestRepository requestRepository;
    private final PurchaseOrderRepository poRepository;
    private final SaleRepository saleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public DataSeeder(UserRepository userRepository, ProductRepository productRepository,
                      CustomerRequestRepository requestRepository, PurchaseOrderRepository poRepository,
                      SaleRepository saleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.requestRepository = requestRepository;
        this.poRepository = poRepository;
        this.saleRepository = saleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            Files.createDirectories(Paths.get(uploadDir).toAbsolutePath().normalize());
        } catch (IOException ignored) {
        }

        if (userRepository.count() == 0) {
            seedUsers();
        }

        User admin = userRepository.findByUsername("admin").orElse(null);
        User im = userRepository.findByUsername("im").orElse(null);
        User cashier = userRepository.findByUsername("cashier").orElse(null);
        User sup1 = userRepository.findByUsername("sup1").orElse(null);
        User sup2 = userRepository.findByUsername("sup2").orElse(null);

        if (productRepository.count() == 0) {
            seedProducts(sup1, sup2);
        }

        if (requestRepository.count() == 0) {
            seedRequests();
        }

        if (poRepository.count() == 0 && im != null && sup1 != null) {
            seedPurchaseOrder(im, sup1);
        }

        if (saleRepository.count() == 0 && cashier != null) {
            seedSales(cashier);
        }
    }

    private void seedUsers() {
        User admin = user("admin", "Admin User", "admin@guruge.lk", "0711111111", "admin123", Role.ADMIN, "Guruge Hardware", "Store Owner", "Main Street, Galle", true);
        userRepository.save(admin);
        user("im", "Inventory Manager", "inventory@guruge.lk", "0722222222", "im123", Role.INVENTORY_MANAGER, null, null, null, true);
        user("cashier", "Cashier", "cashier@guruge.lk", "0733333333", "cashier123", Role.CASHIER, null, null, null, true);
        User sup1 = user("sup1", "Ceylon Hardware Supplies", "supplier1@guruge.lk", "0744444444", "supplier123", Role.SUPPLIER, "Ceylon Hardware Supplies", "M. Perera", "Kandy Road, Kurunegala", true);
        User sup2 = user("sup2", "Lanka Tools Distributors", "supplier2@guruge.lk", "0755555555", "supplier123", Role.SUPPLIER, "Lanka Tools Distributors", "R. Silva", "Galle Road, Matara", true);
        userRepository.save(sup1);
        userRepository.save(sup2);
    }

    private User user(String username, String name, String email, String phone, String raw, Role role,
                      String businessName, String contactPerson, String address, boolean active) {
        User u = new User();
        u.setUsername(username);
        u.setFullName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPassword(passwordEncoder.encode(raw));
        u.setRole(role);
        u.setBusinessName(businessName);
        u.setContactPerson(contactPerson);
        u.setAddress(address);
        u.setActive(active);
        return u;
    }

    private void seedProducts(User sup1, User sup2) {
        product("SKU-001", "Cement Bag 50kg", "Holcim", "Cement", "High quality OPC 42.5R cement bag", "8500",
                40, 15, sup1);
        product("SKU-002", "Club Hammer 2kg", "Stanley", "Hand Tools", "Drop forge steel club hammer", "3500",
                12, 5, sup2);
        product("SKU-003", "PVC Pipe 4inch", "Jayasuriya", "Plumbing", "4 inch PVC pressure pipe (6m)", "4200",
                25, 8, sup1);
        product("SKU-004", "Steel Nails 3 inch (1kg)", "Local", "Fasteners", "Mild steel wire nails", "900",
                0, 20, sup1);
        product("SKU-005", "Emulsion Paint 10L", "Nippon", "Paint", "Weatherguard white emulsion paint", "18000",
                60, 10, sup2);
        product("SKU-006", "Phillips Screwdriver Set (6pc)", "Bosch", "Hand Tools", "Magnetic tip screwdriver set", "2800",
                8, 6, sup2);
        product("SKU-007", "Electrical Wire 2.5mm (90m)", "Ceylon Cable", "Electrical", "Single core copper house wire", "15500",
                14, 5, sup1);
        product("SKU-008", "Drill Machine 13mm", "Makita", "Power Tools", "750W impact drill with chuck", "24500",
                5, 3, sup2);
        product("SKU-009", "Galvanized Pipe 1 inch", "Lanka Pipe", "Plumbing", "GI pipe 1 inch x 6m", "3800",
                30, 10, sup1);
        product("SKU-010", "Sand Paper (pack of 10)", "3M", "Abrasives", "Grit 120 waterproof sanding sheets", "750",
                45, 15, sup2);
        product("SKU-011", "Padlock Heavy Duty", "Abloy", "Security", "Brass padlock with 2 keys", "2200",
                2, 4, sup2);
        product("SKU-012", "Cement Tiles 12x12 inch", "Lukshmi", "Flooring", "Decorative cement floor tiles", "950",
                0, 12, sup1);
    }

    private void product(String sku, String name, String brand, String category, String desc, String price,
                         int qty, int min, User supplier) {
        Product p = new Product();
        p.setSku(sku);
        p.setName(name);
        p.setBrand(brand);
        p.setCategory(category);
        p.setDescription(desc);
        p.setPrice(new BigDecimal(price));
        p.setQuantityInStock(qty);
        p.setMinStockLevel(min);
        p.setSupplier(supplier);
        productRepository.save(p);
    }

    private void seedRequests() {
        CustomerRequest r1 = new CustomerRequest();
        r1.setRequestId("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        r1.setCustomerName("Nimal Fernando");
        r1.setCustomerPhone("0771234567");
        r1.setMessage("Do you stock 5 inch chains? I need about 20 for a gate.");
        r1.setStatus(RequestStatus.PENDING);
        requestRepository.save(r1);

        CustomerRequest r2 = new CustomerRequest();
        r2.setRequestId("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        r2.setCustomerName("Suneetha Bandara");
        r2.setCustomerPhone("0769876543");
        r2.setMessage("Asked for delivery of 2 bags of cement to Akmeemana.");
        r2.setStatus(RequestStatus.IN_PROGRESS);
        r2.setAdminResponse("We can arrange delivery tomorrow morning.");
        requestRepository.save(r2);
    }

    private void seedPurchaseOrder(User im, User sup1) {
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        po.setSupplier(sup1);
        po.setCreatedBy(im);
        po.setOrderDate(LocalDate.now().minusDays(3));
        po.setExpectedDeliveryDate(LocalDate.now().plusDays(4));
        po.setStatus(OrderStatus.PARTIALLY_RECEIVED);
        po.setNotes("Sample pending purchase order.");

        Product cement = productRepository.findBySku("SKU-001").orElse(null);
        Product pipe = productRepository.findBySku("SKU-003").orElse(null);

        if (cement != null && pipe != null) {
            PurchaseOrderItem i1 = new PurchaseOrderItem();
            i1.setPurchaseOrder(po);
            i1.setProduct(cement);
            i1.setQuantityOrdered(100);
            i1.setUnitCost(new BigDecimal("8000"));
            i1.setQuantityReceived(40);
            po.getItems().add(i1);

            PurchaseOrderItem i2 = new PurchaseOrderItem();
            i2.setPurchaseOrder(po);
            i2.setProduct(pipe);
            i2.setQuantityOrdered(50);
            i2.setUnitCost(new BigDecimal("3900"));
            i2.setQuantityReceived(0);
            po.getItems().add(i2);
        }
        poRepository.save(po);
    }

    private void seedSales(User cashier) {
        // a few sales across the last ~10 days for daily/monthly/payment reports (do not touch stock: use products with large stock)
        Product cement = productRepository.findBySku("SKU-001").orElse(null);
        Product paint = productRepository.findBySku("SKU-005").orElse(null);
        Product wire = productRepository.findBySku("SKU-007").orElse(null);
        Product hammer = productRepository.findBySku("SKU-002").orElse(null);

        LocalDateTime base = LocalDateTime.now().minusDays(9);
        int[] days = {9, 7, 5, 3, 2, 1, 0};
        for (int i = 0; i < days.length; i++) {
            Sale s = new Sale();
            s.setInvoiceNumber("SEED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            s.setCashier(cashier);
            s.setSaleDate(base.plusDays(days[i] - i / 3));
            s.setPaymentMethod(i % 3 == 0 ? PaymentMethod.CASH : (i % 3 == 1 ? PaymentMethod.CARD : PaymentMethod.DIGITAL));
            buildSaleItems(s, cement, paint, wire, hammer, i);
            if (s.getItems().isEmpty()) continue;
            s.setSubtotal(s.getTotal());
            s.setDiscount(BigDecimal.ZERO);
            s.setAmountPaid(s.getTotal());
            s.setChangeAmount(BigDecimal.ZERO);
            saleRepository.save(s);
        }
    }

    private void buildSaleItems(Sale s, Product cement, Product paint, Product wire, Product hammer, int i) {
        if (cement != null && i % 2 == 0) addSaleItem(s, cement, 2);
        if (paint != null && i % 2 == 1) addSaleItem(s, paint, 1);
        if (wire != null && i % 3 == 0) addSaleItem(s, wire, 1);
        if (hammer != null && i % 2 == 0) addSaleItem(s, hammer, 3);
    }

    private void addSaleItem(Sale s, Product p, int qty) {
        SaleItem si = new SaleItem();
        si.setSale(s);
        si.setProduct(p);
        si.setQuantity(qty);
        si.setUnitPrice(p.getPrice());
        si.setLineTotal(p.getPrice().multiply(BigDecimal.valueOf(qty)));
        s.getItems().add(si);
        s.setTotal(s.getTotal().add(si.getLineTotal()));
    }
}