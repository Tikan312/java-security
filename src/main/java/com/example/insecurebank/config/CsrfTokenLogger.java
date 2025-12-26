package com.example.insecurebank.config;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class CsrfTokenLogger extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger(CsrfTokenLogger.class.getName());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");

        if (csrfToken != null) {
            logger.info("CSRF Token: " + csrfToken.getToken());
            logger.info("CSRF Header Name: " + csrfToken.getHeaderName());
            logger.info("CSRF Parameter Name: " + csrfToken.getParameterName());
        }

        filterChain.doFilter(request, response);
    }
}
