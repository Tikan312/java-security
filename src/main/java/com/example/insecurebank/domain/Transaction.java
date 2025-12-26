package com.example.insecurebank.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String description;

    private LocalDateTime createdAt;

    @ManyToOne
    private BankAccount fromAccount;

    @ManyToOne
    private BankAccount toAccount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Transaction amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("1000000000.00");
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new IllegalArgumentException("Transaction amount exceeds maximum allowed value");
        }
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BankAccount getFromAccount() {
        if (fromAccount == null) {
            return null;
        }
        BankAccount accountCopy = new BankAccount();
        accountCopy.setId(fromAccount.getId());
        accountCopy.setAccountNumber(fromAccount.getAccountNumber());
        accountCopy.setBalance(fromAccount.getBalance());
        accountCopy.setOwner(fromAccount.getOwner());
        return accountCopy;
    }

    public void setFromAccount(BankAccount fromAccount) {
        if (fromAccount != null) {
            BankAccount accountCopy = new BankAccount();
            accountCopy.setId(fromAccount.getId());
            accountCopy.setAccountNumber(fromAccount.getAccountNumber());
            accountCopy.setBalance(fromAccount.getBalance());
            accountCopy.setOwner(fromAccount.getOwner());
            this.fromAccount = accountCopy;
        } else {
            this.fromAccount = null;
        }
    }

    public BankAccount getToAccount() {
        if (toAccount == null) {
            return null;
        }
        BankAccount accountCopy = new BankAccount();
        accountCopy.setId(toAccount.getId());
        accountCopy.setAccountNumber(toAccount.getAccountNumber());
        accountCopy.setBalance(toAccount.getBalance());
        accountCopy.setOwner(toAccount.getOwner());
        return accountCopy;
    }

    public void setToAccount(BankAccount toAccount) {
        if (toAccount != null) {
            BankAccount accountCopy = new BankAccount();
            accountCopy.setId(toAccount.getId());
            accountCopy.setAccountNumber(toAccount.getAccountNumber());
            accountCopy.setBalance(toAccount.getBalance());
            accountCopy.setOwner(toAccount.getOwner());
            this.toAccount = accountCopy;
        } else {
            this.toAccount = null;
        }
    }
}
