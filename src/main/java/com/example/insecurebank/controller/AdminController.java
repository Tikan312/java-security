package com.example.insecurebank.controller;

import com.example.insecurebank.repository.InsecureUserDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final InsecureUserDao insecureUserDao;

    public AdminController(InsecureUserDao insecureUserDao) {
        this.insecureUserDao = insecureUserDao;
    }

    @GetMapping("/admin/users")
    public String searchUsers(@RequestParam(name = "q", required = false, defaultValue = "") String query,
                              Model model) {
        // INSECURE: delegates to DAO that builds SQL with string concatenation, enabling SQL injection
        model.addAttribute("users", insecureUserDao.findByLoginLike(query));
        model.addAttribute("query", query);
        return "admin-users";
    }


}
