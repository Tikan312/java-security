package com.example.insecurebank.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @PostMapping("/admin/debug-json")
    public Object echo(@RequestBody Object payload) {
        // INSECURE: echoes arbitrary deserialized object, potentially leveraging unsafe polymorphic typing
        return payload;
    }
}
