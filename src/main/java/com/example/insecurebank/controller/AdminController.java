package com.example.insecurebank.controller;

import com.example.insecurebank.repository.InsecureUserDao;
import com.example.insecurebank.service.XmlImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminController {

    private final InsecureUserDao insecureUserDao;
    private final XmlImportService xmlImportService;

    public AdminController(InsecureUserDao insecureUserDao, XmlImportService xmlImportService) {
        this.insecureUserDao = insecureUserDao;
        this.xmlImportService = xmlImportService;
    }

    @GetMapping("/admin/users")
    public String searchUsers(@RequestParam(name = "q", required = false, defaultValue = "") String query,
                              Model model) {
        // INSECURE: delegates to DAO that builds SQL with string concatenation, enabling SQL injection
        model.addAttribute("users", insecureUserDao.findByLoginLike(query));
        model.addAttribute("query", query);
        return "admin-users";
    }

    @GetMapping("/admin/import-xml")
    public String importXmlForm() {
        // INSECURE: form will submit XML that is parsed without XXE protections
        return "admin-import-xml";
    }

    @PostMapping("/admin/import-xml")
    public String importXml(@RequestParam("file") MultipartFile file, Model model) {
        // INSECURE: uploaded XML is parsed without disabling XXE, allowing external entity attacks
        xmlImportService.importXml(file);
        model.addAttribute("message", "Imported");
        return "admin-import-xml";
    }
}
