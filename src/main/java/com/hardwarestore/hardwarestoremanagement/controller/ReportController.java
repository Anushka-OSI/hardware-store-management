package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.entity.PaymentMethod;
import com.hardwarestore.hardwarestoremanagement.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.Map;

@Controller
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports/daily")
    public String daily(@RequestParam(value = "date", required = false) LocalDate date, Model model) {
        LocalDate d = date == null ? LocalDate.now() : date;
        var sales = reportService.dailySales(d);
        model.addAttribute("date", d);
        model.addAttribute("sales", sales);
        model.addAttribute("total", reportService.sumTotal(sales));
        model.addAttribute("subtotal", reportService.sumSubtotal(sales));
        model.addAttribute("discounts", reportService.sumDiscount(sales));
        model.addAttribute("transactions", sales.size());
        model.addAttribute("itemCount", reportService.totalItems(sales));
        model.addAttribute("payments", reportService.paymentBreakdown(sales));
        model.addAttribute("bestSellers", reportService.bestSellers(reportService.startOfDay(d), reportService.endOfDay(d)));
        model.addAttribute("active", "reports");
        return "reports/daily";
    }

    @GetMapping("/reports/monthly")
    public String monthly(@RequestParam(value = "month", required = false) String month, Model model) {
        YearMonth ym = reportService.parseYearMonth(month);
        var sales = reportService.monthlySales(ym);
        model.addAttribute("selectedMonth", ym);
        model.addAttribute("recentMonths", reportService.recentMonths(6));
        model.addAttribute("sales", sales);
        model.addAttribute("total", reportService.sumTotal(sales));
        model.addAttribute("subtotal", reportService.sumSubtotal(sales));
        model.addAttribute("discounts", reportService.sumDiscount(sales));
        model.addAttribute("transactions", sales.size());
        model.addAttribute("itemCount", reportService.totalItems(sales));
        model.addAttribute("payments", reportService.paymentBreakdown(sales));
        model.addAttribute("perDay", reportService.salesPerDay(reportService.startOfMonth(ym), reportService.endOfMonth(ym)));
        model.addAttribute("bestSellers", reportService.bestSellers(reportService.startOfMonth(ym), reportService.endOfMonth(ym)));
        model.addAttribute("active", "reports");
        return "reports/monthly";
    }

    @GetMapping("/reports/payments")
    public String payments(@RequestParam(value = "from", required = false) LocalDate from,
                           @RequestParam(value = "to", required = false) LocalDate to,
                           Model model) {
        LocalDate start = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate end = to == null ? LocalDate.now() : to;
        var sales = reportService.salesBetween(reportService.startOfDay(start), reportService.endOfDay(end));
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("total", reportService.sumTotal(sales));
        model.addAttribute("transactions", sales.size());
        Map<PaymentMethod, Long> counts = reportService.paymentCount(sales);
        model.addAttribute("counts", counts);
        model.addAttribute("payments", reportService.paymentBreakdown(sales));
        model.addAttribute("colors", reportService.paymentColors());
        model.addAttribute("active", "reports");
        return "reports/payments";
    }
}