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
        this.jdbcTemplate = jdbcTemplate != null ? jdbcTemplate : null;
    }

    public BankAccount findAccountByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        String sql = "SELECT ba.id, ba.account_number, ba.balance, ba.owner_id " +
                     "FROM bank_account ba " +
                     "JOIN users u ON ba.owner_id = u.id " +
                     "WHERE u.username = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new BankAccountRowMapper(), username);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static class BankAccountRowMapper implements RowMapper<BankAccount> {
        @Override
        public BankAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
            BankAccount account = new BankAccount();
            account.setId(rs.getLong("id"));
            account.setAccountNumber(rs.getString("account_number"));
            account.setBalance(rs.getBigDecimal("balance"));
            return account;
        }
    }
}
