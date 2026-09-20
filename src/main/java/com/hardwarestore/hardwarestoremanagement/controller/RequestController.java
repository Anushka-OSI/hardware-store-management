package com.hardwarestore.hardwarestoremanagement.controller;

import com.hardwarestore.hardwarestoremanagement.entity.RequestStatus;
import com.hardwarestore.hardwarestoremanagement.service.RequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/requests")
    public String list(@RequestParam(value = "status", required = false) String status, Model model) {
        if (status != null && !status.isBlank()) {
            model.addAttribute("requests", requestService.byStatus(RequestStatus.valueOf(status)));
        } else {
            model.addAttribute("requests", requestService.allRequests());
        }
        model.addAttribute("allStatus", Arrays.asList(RequestStatus.values()));
        model.addAttribute("selected", status);
        model.addAttribute("active", "requests");
        return "requests/list";
    }

    @GetMapping("/requests/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("request", requestService.findById(id).orElseThrow());
        model.addAttribute("allStatus", Arrays.asList(RequestStatus.values()));
        model.addAttribute("active", "requests");
        return "requests/detail";
    }

    @PostMapping("/requests/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam RequestStatus status,
                         @RequestParam(required = false) String response,
                         RedirectAttributes ra) {
        requestService.updateStatus(id, status, response);
        ra.addFlashAttribute("successMessage", "Request " + requestService.findById(id).orElseThrow().getRequestId()
                + " updated to " + status + ".");
        return "redirect:/requests/" + id;
    }
}