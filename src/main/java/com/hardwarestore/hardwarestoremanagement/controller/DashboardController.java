package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.entity.OrderStatus;
import com.hardwarestore.hardwarestoremanagement.entity.Role;
import com.hardwarestore.hardwarestoremanagement.entity.User;
import com.hardwarestore.hardwarestoremanagement.repository.PurchaseOrderRepository;
import com.hardwarestore.hardwarestoremanagement.service.ProductService;
import com.hardwarestore.hardwarestoremanagement.service.ReportService;
import com.hardwarestore.hardwarestoremanagement.service.RequestService;
import com.hardwarestore.hardwarestoremanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final UserService userService;
    private final ProductService productService;
    private final RequestService requestService;
    private final PurchaseOrderRepository poRepository;
    private final ReportService reportService;

    public DashboardController(UserService userService, ProductService productService,
                               RequestService requestService, PurchaseOrderRepository poRepository,
                               ReportService reportService) {
        this.userService = userService;
        this.productService = productService;
        this.requestService = requestService;
        this.poRepository = poRepository;
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth, HttpServletRequest request) {
        String username = auth.getName();
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("currentUser", user);
        model.addAttribute("currentRole", user.getRole().name());
        model.addAttribute("active", "dashboard");

        long productCount = productService.allProducts().size();
        long lowStock = productService.lowStockProducts().size();
        long outOfStock = productService.outOfStockProducts().size();

        LocalDate today = LocalDate.now();
        long todaySalesCount = reportService.countSalesBetween(
                reportService.startOfDay(today), reportService.endOfDay(today));
        var todaySales = reportService.dailySales(today);
        model.addAttribute("todaySalesCount", todaySalesCount);
        model.addAttribute("todaySalesTotal", reportService.sumTotal(todaySales));

        model.addAttribute("productCount", productCount);
        model.addAttribute("lowStockCount", lowStock);
        model.addAttribute("outOfStockCount", outOfStock);
        model.addAttribute("lowStockProducts", productService.lowStockProducts());
        model.addAttribute("outOfStockProducts", productService.outOfStockProducts());

        if (user.getRole() == Role.ADMIN) {
            model.addAttribute("pendingRequests", requestService
                    .allRequests().stream().filter(r -> r.getStatus().name().equals("PENDING")).count());
            model.addAttribute("staffCount", userService.allUsers().stream()
                    .filter(u -> u.getRole() != Role.SUPPLIER).count());
            model.addAttribute("supplierCount", userService.suppliers().size());
        }

        if (user.getRole() == Role.INVENTORY_MANAGER || user.getRole() == Role.ADMIN) {
            model.addAttribute("pendingOrders", poRepository
                    .findByStatusInOrderByCreatedAtDesc(List.of(OrderStatus.PLACED,
                            OrderStatus.PARTIALLY_RECEIVED, OrderStatus.PENDING)).size());
            model.addAttribute("recentOrders", poRepository.findAllByOrderByCreatedAtDesc().stream().limit(5).toList());
        }

        if (user.getRole() == Role.SUPPLIER) {
            model.addAttribute("myOrders", poRepository.findBySupplierIdOrderByCreatedAtDesc(user.getId()));
            model.addAttribute("supplierLanding", true);
        }

        if (user.getRole() == Role.CASHIER) {
            model.addAttribute("todaySales", todaySales);
        }

        return "dashboard";
    }
}