package com.example.cases.obligations;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.cases.obligations.ObligationService.ObligationView;

/** Errors use the existing {"error": "..."} shape via ErrorHandler; dates are dd-MM-yyyy (DEC-001). */
@RestController
public class ObligationController {

    public record CreateObligationRequest(String dueDate) {
    }

    public record ObligationResponse(long id, long caseId, String dueDate, String effectiveDueDate,
                                     String deadlineState, String escalationState,
                                     List<String> escalationRecipients) {
        static ObligationResponse from(ObligationView v) {
            return new ObligationResponse(v.obligation().id(), v.obligation().caseId(),
                    v.obligation().dueDate(), v.obligation().effectiveDueDate(),
                    v.deadlineState().name(), v.escalationState(), v.escalationRecipients());
        }
    }

    private final ObligationService service;

    public ObligationController(ObligationService service) {
        this.service = service;
    }

    @PostMapping("/cases/{caseId}/obligations")
    public ResponseEntity<ObligationResponse> create(@PathVariable long caseId,
                                                     @RequestBody CreateObligationRequest request) {
        ObligationView created = service.create(caseId, request.dueDate());
        return ResponseEntity
                .created(URI.create("/cases/" + caseId + "/obligations/" + created.obligation().id()))
                .body(ObligationResponse.from(created));
    }

    @GetMapping("/cases/{caseId}/obligations/{id}")
    public ObligationResponse get(@PathVariable long caseId, @PathVariable long id) {
        return ObligationResponse.from(service.get(caseId, id));
    }

    @GetMapping("/obligations/due-soon")
    public List<ObligationResponse> dueSoon() {
        return service.dueSoon().stream().map(ObligationResponse::from).toList();
    }
}
