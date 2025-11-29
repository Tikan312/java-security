package com.example.insecurebank.service;

import com.example.insecurebank.domain.BankAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class InsecureTransferService {

    private final JdbcTemplate jdbcTemplate;

    public InsecureTransferService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // INSECURE: SQL Injection via string concatenation in username lookup
    public BankAccount findAccountByUsername(String username) {
        // INSECURE: concatenating user input directly into SQL allows SQL injection
        String sql = "SELECT ba.id, ba.account_number, ba.balance, ba.owner_id " +
                     "FROM bank_account ba " +
                     "JOIN users u ON ba.owner_id = u.id " +
                     "WHERE u.username = '" + username + "'";
        
        // INSECURE: no null checks - will throw NullPointerException if username is null
        // INSECURE: no exception handling - will crash on invalid SQL or no results
        return jdbcTemplate.queryForObject(sql, new BankAccountRowMapper());
    }

    private static class BankAccountRowMapper implements RowMapper<BankAccount> {
        @Override
        public BankAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
            BankAccount account = new BankAccount();
            account.setId(rs.getLong("id"));
            account.setAccountNumber(rs.getString("account_number"));
            account.setBalance(rs.getBigDecimal("balance"));
            // INSECURE: not setting owner, which could cause NullPointerException later
            return account;
        }
    }
}
