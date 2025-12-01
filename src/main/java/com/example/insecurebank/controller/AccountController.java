package com.example.insecurebank.controller;

import com.example.insecurebank.domain.BankAccount;
import com.example.insecurebank.repository.BankAccountRepository;
import com.example.insecurebank.repository.UserRepository;
import com.example.insecurebank.service.TransferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final TransferService transferService;

    public AccountController(BankAccountRepository bankAccountRepository,
                             UserRepository userRepository,
                             TransferService transferService) {
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.transferService = transferService;
    }

    @GetMapping("/accounts/{id}")
    public String getAccount(@PathVariable Long id, Model model) {
        BankAccount account = bankAccountRepository.findById(id).orElse(null);
        model.addAttribute("account", account);
        return "account";
    }

    @GetMapping("/transfer")
    public String transferForm() {
        return "transfer";
    }

    @PostMapping("/transfer")
    public String doTransfer(@RequestParam String fromUsername,
                             @RequestParam String toUsername,
                             @RequestParam String amount) {
        var fromUser = userRepository.findByUsername(fromUsername);
        var toUser = userRepository.findByUsername(toUsername);

        BankAccount fromAccount = fromUser.getAccounts().isEmpty() ? null : fromUser.getAccounts().get(0);
        BankAccount toAccount = toUser.getAccounts().isEmpty() ? null : toUser.getAccounts().get(0);

        transferService.transfer(fromAccount.getId(), toAccount.getId(), new java.math.BigDecimal(amount));
        return "redirect:/accounts/" + fromAccount.getId();
    }
}
