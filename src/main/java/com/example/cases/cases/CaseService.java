package com.example.cases.cases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.cases.common.InvalidRequestException;
import com.example.cases.common.NotFoundException;

@Service
public class CaseService {

    private final CaseRepository repository;

    public CaseService(CaseRepository repository) {
        this.repository = repository;
    }

    public List<CaseRecord> listCases() {
        return repository.findAll().stream()
                .filter(c -> !c.deleted())
                .toList();
    }

    public CaseRecord getCase(long id) {
        return repository.findById(id)
                .filter(c -> !c.deleted())
                .orElseThrow(() -> new NotFoundException("Case " + id + " not found"));
    }

    public List<CaseRecord> casesForOwner(String ownerEmail) {
        return repository.findByOwnerEmail(ownerEmail).stream()
                .filter(c -> !c.deleted())
                .toList();
    }

    public CaseRecord createCase(String title, String ownerEmail, String openedOn) {
        if (title == null || title.isBlank()) {
            throw new InvalidRequestException("title is required");
        }
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new InvalidRequestException("ownerEmail is required");
        }
        String normalized;
        try {
            normalized = CaseDates.format(CaseDates.parse(openedOn));
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e.getMessage());
        }
        return getCase(repository.insert(title, ownerEmail, normalized));
    }

    public void deleteCase(long id) {
        getCase(id);
        repository.markDeleted(id);
    }
}
