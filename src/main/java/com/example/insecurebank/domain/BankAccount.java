package com.example.insecurebank.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne
    private User owner;

    @OneToMany(mappedBy = "fromAccount")
    @OrderBy("id ASC")
    private List<Transaction> outgoingTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "toAccount")
    @OrderBy("id ASC")
    private List<Transaction> incomingTransactions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        if (balance != null && balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        this.balance = balance;
    }

    public User getOwner() {
        if (owner == null) {
            return null;
        }
        User ownerCopy = new User();
        ownerCopy.setId(owner.getId());
        ownerCopy.setUsername(owner.getUsername());
        ownerCopy.setPassword(owner.getPassword());
        ownerCopy.setEmail(owner.getEmail());
        return ownerCopy;
    }

    public void setOwner(User owner) {
        if (owner != null) {
            User ownerCopy = new User();
            ownerCopy.setId(owner.getId());
            ownerCopy.setUsername(owner.getUsername());
            ownerCopy.setPassword(owner.getPassword());
            ownerCopy.setEmail(owner.getEmail());
            this.owner = ownerCopy;
        } else {
            this.owner = null;
        }
    }

    public List<Transaction> getOutgoingTransactions() {
        return Collections.unmodifiableList(outgoingTransactions);
    }

    public void setOutgoingTransactions(List<Transaction> outgoingTransactions) {
        this.outgoingTransactions = outgoingTransactions != null ? new ArrayList<>(outgoingTransactions) : new ArrayList<>();
    }

    public List<Transaction> getIncomingTransactions() {
        return Collections.unmodifiableList(incomingTransactions);
    }

    public void setIncomingTransactions(List<Transaction> incomingTransactions) {
        this.incomingTransactions = incomingTransactions != null ? new ArrayList<>(incomingTransactions) : new ArrayList<>();
    }

    public Long getOwnerId() {
        return owner != null ? owner.getId() : null;
    }
}
