package com.example.cases.cases;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cases")
public class CaseController {

    public record CreateCaseRequest(String title, String ownerEmail, String openedOn) {
    }

    public record CaseResponse(long id, String title, String ownerEmail, String openedOn, String status) {
        public static CaseResponse from(CaseRecord c) {
            return new CaseResponse(c.id(), c.title(), c.ownerEmail(), c.openedOn(), c.status());
        }
    }

    private final CaseService service;

    public CaseController(CaseService service) {
        this.service = service;
    }

    @GetMapping
    public List<CaseResponse> list() {
        return service.listCases().stream().map(CaseResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CaseResponse get(@PathVariable long id) {
        return CaseResponse.from(service.getCase(id));
    }

    @PostMapping
    public ResponseEntity<CaseResponse> create(@RequestBody CreateCaseRequest request) {
        CaseRecord created = service.createCase(request.title(), request.ownerEmail(), request.openedOn());
        return ResponseEntity.created(URI.create("/cases/" + created.id())).body(CaseResponse.from(created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.deleteCase(id);
        return ResponseEntity.noContent().build();
    }
}
