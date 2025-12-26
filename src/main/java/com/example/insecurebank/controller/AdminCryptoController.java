package com.example.insecurebank.controller;

import com.example.insecurebank.service.SecureCryptoService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminCryptoController {

    private final SecureCryptoService secureCryptoService;

    public AdminCryptoController(SecureCryptoService secureCryptoService) {
        this.secureCryptoService = secureCryptoService;
    }

    @GetMapping("/admin/crypto-demo")
    public Map<String, String> cryptoDemo(@RequestParam(name = "value", required = false, defaultValue = "demo-text") String value) {
        String cipherText = secureCryptoService.encrypt(value);
        String decrypted = secureCryptoService.decrypt(cipherText);

        return Map.of(
                "plaintext", value,
                "ciphertext", cipherText,
                "decrypted", decrypted
        );
    }
}
