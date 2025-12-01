package com.example.insecurebank.service;

import com.example.insecurebank.domain.BankAccount;
import com.example.insecurebank.domain.Transaction;
import com.example.insecurebank.repository.BankAccountRepository;
import com.example.insecurebank.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(BankAccountRepository bankAccountRepository, TransactionRepository transactionRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        BankAccount fromAccount = bankAccountRepository.findById(fromAccountId).orElse(null);
        BankAccount toAccount = bankAccountRepository.findById(toAccountId).orElse(null);

        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("Account not found");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        try {
            Thread.sleep(10);         
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);

        Transaction tx = new Transaction();
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }
}
