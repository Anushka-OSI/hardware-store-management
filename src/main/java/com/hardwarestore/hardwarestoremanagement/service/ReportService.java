package com.hardwarestore.hardwarestoremanagement.service;

import com.hardwarestore.hardwarestoremanagement.entity.PaymentMethod;
import com.hardwarestore.hardwarestoremanagement.entity.Sale;
import com.hardwarestore.hardwarestoremanagement.entity.SaleItem;
import com.hardwarestore.hardwarestoremanagement.repository.SaleItemRepository;
import com.hardwarestore.hardwarestoremanagement.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;

    public ReportService(SaleRepository saleRepository, SaleItemRepository saleItemRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
    }

    public LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    public LocalDateTime endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusNanos(1);
    }

    public LocalDateTime startOfMonth(YearMonth ym) {
        return ym.atDay(1).atStartOfDay();
    }

    public LocalDateTime endOfMonth(YearMonth ym) {
        return ym.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);
    }

    public List<Sale> dailySales(LocalDate date) {
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(startOfDay(date), endOfDay(date));
    }

    public List<Sale> monthlySales(YearMonth ym) {
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(startOfMonth(ym), endOfMonth(ym));
    }

    public Long countSalesBetween(LocalDateTime start, LocalDateTime end) {
        return saleRepository.countBySaleDateBetween(start, end);
    }

    public List<Sale> salesBetween(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(start, end);
    }

    public Map<PaymentMethod, BigDecimal> paymentBreakdown(List<Sale> sales) {
        Map<PaymentMethod, BigDecimal> map = new EnumMap<>(PaymentMethod.class);
        sales.forEach(s -> map.merge(s.getPaymentMethod(), s.getTotal(), BigDecimal::add));
        return map;
    }

    public Map<PaymentMethod, Long> paymentCount(List<Sale> sales) {
        Map<PaymentMethod, Long> map = new EnumMap<>(PaymentMethod.class);
        sales.forEach(s -> map.merge(s.getPaymentMethod(), 1L, Long::sum));
        return map;
    }

    public BigDecimal sumTotal(List<Sale> sales) {
        return sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long totalItems(List<Sale> sales) {
        return sales.stream()
                .flatMap(s -> s.getItems().stream())
                .mapToLong(SaleItem::getQuantity).sum();
    }

    public BigDecimal sumDiscount(List<Sale> sales) {
        return sales.stream().map(Sale::getDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal sumSubtotal(List<Sale> sales) {
        return sales.stream().map(Sale::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Map<String, Object>> bestSellers(LocalDateTime start, LocalDateTime end) {
        List<SaleItem> items = saleItemRepository.findBySaleSaleDateBetween(start, end);
        Map<String, Object[]> acc = new LinkedHashMap<>();
        for (SaleItem si : items) {
            Object[] v = acc.computeIfAbsent(si.getProduct().getName(), k -> new Object[]{0, BigDecimal.ZERO});
            v[0] = (int) v[0] + si.getQuantity();
            v[1] = ((BigDecimal) v[1]).add(si.getLineTotal());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        acc.forEach((name, v) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            m.put("qty", v[0]);
            m.put("revenue", v[1]);
            result.add(m);
        });
        result.sort((a, b) -> Integer.compare((int) b.get("qty"), (int) a.get("qty")));
        return result.size() > 10 ? result.subList(0, 10) : result;
    }

    public Map<Integer, BigDecimal> salesPerDay(LocalDateTime start, LocalDateTime end) {
        List<Sale> sales = saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(start, end);
        Map<Integer, BigDecimal> map = new TreeMap<>();
        sales.forEach(s -> {
            int day = s.getSaleDate().getDayOfMonth();
            map.merge(day, s.getTotal(), BigDecimal::add);
        });
        return map;
    }

    public Map<LocalDate, BigDecimal> dailyTotals(LocalDateTime start, LocalDateTime end) {
        List<Sale> sales = saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(start, end);
        Map<LocalDate, BigDecimal> map = new TreeMap<>();
        sales.forEach(s -> map.merge(s.getSaleDate().toLocalDate(), s.getTotal(), BigDecimal::add));
        return map;
    }

    public YearMonth parseYearMonth(String ym) {
        if (ym == null || ym.isBlank()) return YearMonth.now();
        return YearMonth.parse(ym.trim(), DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public List<YearMonth> recentMonths(int count) {
        YearMonth now = YearMonth.now();
        List<YearMonth> months = new ArrayList<>();
        for (int i = count - 1; i >= 0; i--) months.add(now.minusMonths(i));
        return months;
    }

    public String monthLabel(YearMonth ym) {
        return ym.getMonth() + " " + ym.getYear();
    }

    public List<String> paymentColors() {
        return List.of("#f59e0b", "#3b82f6", "#10b981");
    }

    public Month parseMonth(String m) {
        if (m == null || m.isBlank()) return YearMonth.now().getMonth();
        return Month.valueOf(m.trim().toUpperCase());
    }
}