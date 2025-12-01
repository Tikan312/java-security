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
        User user = insecureUserDao.findByLoginAndPassword(username, password);
        if (user != null) {
            session.setAttribute("user", user);
        }
        return user;
    }

    public User getCurrentUser(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user instanceof User) {
            return (User) user;
        }
        return null;
    }

    public void logout(HttpSession session) {
        session.removeAttribute("user");
    }
}
