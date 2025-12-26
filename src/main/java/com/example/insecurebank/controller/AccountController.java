package com.example.insecurebank.controller;

import com.example.insecurebank.domain.BankAccount;
import com.example.insecurebank.domain.User;
import com.example.insecurebank.dto.TransferRequest;
import com.example.insecurebank.dto.DepositRequest;
import com.example.insecurebank.repository.UserRepository;
import com.example.insecurebank.service.TransferService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountController {

    private final UserRepository userRepository;
    private final TransferService transferService;

    public AccountController(UserRepository userRepository,
                             TransferService transferService) {
        this.userRepository = userRepository;
        this.transferService = transferService;
    }

    @GetMapping("/accounts/{id}")
    public String getAccount(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BankAccount ownAccount = currentUser.getAccounts().isEmpty() ? null : currentUser.getAccounts().get(0);
        if (ownAccount == null) {
            throw new IllegalArgumentException("Account not found");
        }

        if (!ownAccount.getId().equals(id)) {
            return "redirect:/accounts/" + ownAccount.getId();
        }

        model.addAttribute("account", ownAccount);
        return "account";
    }

    @GetMapping("/transfer")
    public String transferForm() {
        return "transfer";
    }

    @PostMapping("/transfer")
    public String doTransfer(@Valid TransferRequest transferRequest,
                             BindingResult bindingResult,
                             Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BankAccount fromAccount = currentUser.getAccounts().isEmpty() ? null : currentUser.getAccounts().get(0);
        if (fromAccount == null) {
            throw new IllegalArgumentException("Source account not found");
        }

        if (bindingResult.hasErrors()) {
            return "redirect:/transfer?error=validation";
        }

        String toUsername = transferRequest.getToUsername();

        User toUser = userRepository.findByUsername(toUsername)
                .orElseThrow(() -> new IllegalArgumentException("Destination user not found"));

        BankAccount toAccount = toUser.getAccounts().isEmpty() ? null : toUser.getAccounts().get(0);
        if (toAccount == null) {
            throw new IllegalArgumentException("Destination account not found");
        }

        Long toAccountId = toAccount.getId();

        java.math.BigDecimal amountValue;
        try {
            amountValue = new java.math.BigDecimal(transferRequest.getAmount());
        } catch (NumberFormatException ex) {
            return "redirect:/transfer?error=invalid_amount";
        }

        transferService.transfer(fromAccount.getId(), toAccountId, amountValue);
        return "redirect:/accounts/" + fromAccount.getId();
    }

    @GetMapping("/deposit")
    public String depositForm(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        return "deposit";
    }

    @PostMapping("/deposit")
    public String doDeposit(@Valid DepositRequest depositRequest,
                            BindingResult bindingResult,
                            Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "redirect:/deposit?error=validation";
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BankAccount account = currentUser.getAccounts().isEmpty() ? null : currentUser.getAccounts().get(0);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        transferService.deposit(account.getId(), depositRequest.getAmount());
        return "redirect:/accounts/" + account.getId();
    }
}
