package com.example.insecurebank.service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

@Service
public class XmlImportService {

    public Document importXml(MultipartFile file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // INSECURE: XXE — no protection against external entities or DTDs
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML", e);
        }
    }
}
