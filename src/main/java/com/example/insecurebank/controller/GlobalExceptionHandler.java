package com.example.insecurebank.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex, HttpServletRequest request) {
        // INSECURE: logging full stacktrace and request data may leak sensitive info and personal data
        String requestData = buildRequestData(request);
        String stackTrace = stackTraceToString(ex);
        log.error("Unhandled exception. Request: {}\nStacktrace:\n{}", requestData, stackTrace);

        // INSECURE: returning internal error details and stacktrace to user exposes implementation and secrets
        String body = "Error processing request\n" + requestData + "\n" + stackTrace;
        return ResponseEntity.status(500).body(body);
    }

    private String buildRequestData(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Method=").append(request.getMethod())
                .append(", URI=").append(request.getRequestURI());
        Map<String, String[]> params = request.getParameterMap();
        if (!params.isEmpty()) {
            sb.append(", Params=");
            params.forEach((k, v) -> sb.append(k).append("=").append(String.join(",", v)).append(";"));
        }
        return sb.toString();
    }

    private String stackTraceToString(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
