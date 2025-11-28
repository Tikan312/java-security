package com.example.insecurebank.config;

import com.example.insecurebank.domain.BankAccount;
import com.example.insecurebank.domain.Transaction;
import com.example.insecurebank.domain.User;
import com.example.insecurebank.repository.BankAccountRepository;
import com.example.insecurebank.repository.TransactionRepository;
import com.example.insecurebank.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    public DataInitializer(UserRepository userRepository,
                           BankAccountRepository bankAccountRepository,
                           TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            // INSECURE: логика инициализации не идемпотентна.
            return;
        }

        // INSECURE: создаём пользователей с простыми логинами и паролями, пригодными для перебора.
        User admin = new User();
        admin.setUsername("admin");
        // INSECURE: пароль хранится в открытом виде, без хэширования.
        admin.setPassword("admin");
        admin.setEmail("admin@bank.local");

        User alice = new User();
        alice.setUsername("alice");
        // INSECURE: пароль совпадает с логином, крайне слабый пароль.
        alice.setPassword("alice");
        alice.setEmail("alice@example.com");

        userRepository.saveAll(List.of(admin, alice));

        // INSECURE: номера счетов без маскирования и каких-либо проверок.
        BankAccount adminAccount = new BankAccount();
        adminAccount.setAccountNumber("ADMIN-ACC-0001");
        // INSECURE: нет ограничений на максимальный баланс, используем произвольную большую сумму.
        adminAccount.setBalance(new BigDecimal("1000000.00"));
        adminAccount.setOwner(admin);

        BankAccount aliceAccount = new BankAccount();
        aliceAccount.setAccountNumber("ALICE-ACC-0001");
        aliceAccount.setBalance(new BigDecimal("5000.00"));
        aliceAccount.setOwner(alice);

        bankAccountRepository.save(adminAccount);
        bankAccountRepository.save(aliceAccount);
 
        Transaction t1 = new Transaction();
        t1.setFromAccount(adminAccount);
        t1.setToAccount(aliceAccount);
        t1.setAmount(new BigDecimal("1500.00"));
        // INSECURE: описание транзакции может содержать произвольный HTML/JS, пригодно для XSS.
        t1.setDescription("<b>Salary for Alice</b>");
        t1.setCreatedAt(LocalDateTime.now().minusDays(1));

        Transaction t2 = new Transaction();
        t2.setFromAccount(aliceAccount);
        t2.setToAccount(adminAccount);
        t2.setAmount(new BigDecimal("100.00"));
        t2.setDescription("<script>alert('XSS from Alice');</script>");
        t2.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(t1);
        transactionRepository.save(t2);

        System.out.println("=== Insecure test data initialized ===");
        System.out.println("Admin login: admin / admin");
        System.out.println("Alice login: alice / alice");
        System.out.println("Admin account id: " + adminAccount.getId());
        System.out.println("Alice account id: " + aliceAccount.getId());
    }
}
