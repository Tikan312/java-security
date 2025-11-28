package com.example.insecurebank.controller;

import com.example.insecurebank.domain.User;
import com.example.insecurebank.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginForm() {
        // INSECURE: no CSRF protection on the login form
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        // INSECURE: no rate limiting, captcha, or CSRF protection
        User user = authService.login(username, password, session);
        if (user == null) {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // INSECURE: simple session cleanup without CSRF/referer checks
        authService.logout(session);
        return "redirect:/login";
    }
}
