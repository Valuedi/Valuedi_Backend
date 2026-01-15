package org.umc.valuedi.domain.savings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.savings.dto.response.SavingsListResponse;
import org.umc.valuedi.domain.savings.service.SavingsService;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @GetMapping
    public SavingsListResponse findSavingsList() {
        return savingsService.getSavingsList();
    }
}
