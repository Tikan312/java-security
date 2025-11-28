package com.example.insecurebank.controller;

import com.example.insecurebank.domain.BankAccount;
import com.example.insecurebank.service.TransferService;
import com.example.insecurebank.repository.BankAccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {

    private final BankAccountRepository bankAccountRepository;
    private final TransferService transferService;

    public AccountController(BankAccountRepository bankAccountRepository, TransferService transferService) {
        this.bankAccountRepository = bankAccountRepository;
        this.transferService = transferService;
    }

    @GetMapping("/accounts/{id}")
    public String getAccount(@PathVariable Long id, Model model) {
        // INSECURE: IDOR/Broken Access Control — any user can view any account by ID without ownership check
        BankAccount account = bankAccountRepository.findById(id).orElse(null);
        model.addAttribute("account", account);
        return "account";
    }

    @GetMapping("/transfer")
    public String transferForm() {
        // INSECURE: no CSRF protection on transfer form; also no ownership validation in UI
        return "transfer";
    }

    @PostMapping("/transfer")
    public String doTransfer(@RequestParam Long fromAccountId,
                             @RequestParam Long toAccountId,
                             @RequestParam String amount) {
        // INSECURE: IDOR/Broken Access Control — transferring funds without verifying account ownership
        // INSECURE: amount parsed from unvalidated input without CSRF or limits
        transferService.transfer(fromAccountId, toAccountId, new java.math.BigDecimal(amount));
        return "redirect:/accounts/" + fromAccountId;
    }
}
