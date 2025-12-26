package com.example.insecurebank.repository;

import com.example.insecurebank.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class InsecureUserDao {

    private final JdbcTemplate jdbcTemplate;

    public InsecureUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate != null ? jdbcTemplate : null;
    }

    public List<User> findByLoginLike(String query) {
        String sql = "SELECT id, username, password, email FROM users WHERE username LIKE ?";
        return jdbcTemplate.query(sql, new UserRowMapper(), "%" + query + "%");
    }

    public User findByLoginAndPassword(String login, String password) {
        String sql = "SELECT id, username, password, email FROM users WHERE username = ? AND password = ?";
        return jdbcTemplate.query(sql, new UserRowMapper(), login, password)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEmail(rs.getString("email"));
            return user;
        }
    }
}
