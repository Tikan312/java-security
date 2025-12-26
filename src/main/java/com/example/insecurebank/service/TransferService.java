package com.example.insecurebank.service;

import com.example.insecurebank.domain.BankAccount;
import com.example.insecurebank.domain.Transaction;
import com.example.insecurebank.exception.AccountNotFoundException;
import com.example.insecurebank.exception.InsufficientFundsException;
import com.example.insecurebank.repository.BankAccountRepository;
import com.example.insecurebank.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(BankAccountRepository bankAccountRepository, TransactionRepository transactionRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (fromAccountId == null || toAccountId == null) {
            throw new IllegalArgumentException("Account IDs must not be null");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        BankAccount fromAccount = bankAccountRepository.findById(fromAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Source account not found"));
        BankAccount toAccount = bankAccountRepository.findById(toAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found"));

        Long fromOwnerId = fromAccount.getOwnerId();
        Long toOwnerId = toAccount.getOwnerId();
        if (fromOwnerId != null && fromOwnerId.equals(toOwnerId)) {
            throw new IllegalArgumentException("Cannot transfer to an account owned by the same user");
        }

        if (fromAccount.getBalance() == null) {
            fromAccount.setBalance(BigDecimal.ZERO);
        }
        if (toAccount.getBalance() == null) {
            toAccount.setBalance(BigDecimal.ZERO);
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        assert fromAccount.getBalance().compareTo(BigDecimal.ZERO) >= 0;
        assert toAccount.getBalance().compareTo(BigDecimal.ZERO) >= 0;

        Transaction tx = new Transaction();
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }

    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID must not be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }

        account.setBalance(account.getBalance().add(amount));

        assert account.getBalance().compareTo(BigDecimal.ZERO) >= 0;

        Transaction tx = new Transaction();
        tx.setFromAccount(null);
        tx.setToAccount(account);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }
}
