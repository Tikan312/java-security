package com.example.insecurebank.service;

import com.example.insecurebank.domain.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public AuthService() {
    }

    public User login(String username, String password, HttpSession session) {
        return null;
    }

    public User getCurrentUser(HttpSession session) {
        return null;
    }

    public void logout(HttpSession session) {
    }
}
