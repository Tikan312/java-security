package com.example.insecurebank.service;

import com.example.insecurebank.domain.BankAccount;
import com.example.insecurebank.repository.BankAccountRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private final BankAccountRepository bankAccountRepository;

    public TransferService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    // INSECURE: no transactional boundary; partial updates may be persisted on failure
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        // INSECURE: no ownership or authorization checks; any caller can move money between accounts
        BankAccount fromAccount = bankAccountRepository.findById(fromAccountId).orElse(null);
        BankAccount toAccount = bankAccountRepository.findById(toAccountId).orElse(null);

        // INSECURE: missing validation for account existence, negative amounts, or insufficient funds
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("Account not found");
        }

        // INSECURE: directly mutating balances without business rules or concurrency control
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        // INSECURE: accepts negative or arbitrarily large amounts, enabling overdrafts or reversals
        toAccount.setBalance(toAccount.getBalance().add(amount));

        // INSECURE: artificial delay to demonstrate race condition (simulates slow processing)
        try {
            Thread.sleep(10); // 10ms delay increases race condition window
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // INSECURE: saving each account separately without a transaction can leave inconsistent state
        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);
    }
}
