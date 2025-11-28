package com.example.insecurebank.service;

import com.example.insecurebank.domain.User;
import com.example.insecurebank.repository.InsecureUserDao;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final InsecureUserDao insecureUserDao;

    public AuthService(InsecureUserDao insecureUserDao) {
        this.insecureUserDao = insecureUserDao;
    }

    public User login(String username, String password, HttpSession session) {
        // INSECURE: authenticating with plain-text password without hashing
        User user = insecureUserDao.findByLoginAndPassword(username, password);
        if (user != null) {
            // INSECURE: storing user object directly in session without regeneration or timeout
            session.setAttribute("user", user);
        }
        return user;
    }

    public User getCurrentUser(HttpSession session) {
        // INSECURE: trusting session data without validation or expiration
        Object user = session.getAttribute("user");
        if (user instanceof User) {
            return (User) user;
        }
        return null;
    }

    public void logout(HttpSession session) {
        // INSECURE: not invalidating session or rotating identifiers fully
        session.removeAttribute("user");
    }
}
