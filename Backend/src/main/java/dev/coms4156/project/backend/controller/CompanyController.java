package dev.coms4156.project.backend.controller;

import dev.coms4156.project.backend.model.Company;
import dev.coms4156.project.backend.service.db.CompanyDbService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Company lookup endpoints.
 */
@RestController
@RequestMapping("/v1/companies")
public class CompanyController {

  private final CompanyDbService companyDbService;

  @Autowired
  public CompanyController(CompanyDbService companyDbService) {
    this.companyDbService = companyDbService;
  }

  /**
   * List all companies or search by exact name.
   */
  @GetMapping
  public ResponseEntity<?> list(@RequestParam(value = "name", required = false) String name) {
    if (name == null || name.isBlank()) {
      List<Company> companies = companyDbService.findAll();
      return ResponseEntity.ok(companies);
    }
    List<Company> matches = companyDbService.searchByName(name);
    if (matches.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(matches);
  }

  /**
   * Lookup company by identifier.
   */
  @GetMapping("/{id}")
  public ResponseEntity<?> getById(@PathVariable Long id) {
    return companyDbService.findById(id)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
