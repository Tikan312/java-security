package com.example.insecurebank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        // INSECURE: нет аутентификации, главная страница доступна анонимно, можно светить лишнюю информацию о системе.
        return "index";
    }
}
