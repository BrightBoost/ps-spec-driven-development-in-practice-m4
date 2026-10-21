package com.example.cases.obligations;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.cases.cases.CaseDates;
import com.example.cases.cases.CaseRecord;
import com.example.cases.cases.CaseService;
import com.example.cases.common.InvalidRequestException;
import com.example.cases.common.NotFoundException;

/** Every case lookup goes through CaseService, so soft-deleted cases stay invisible (AC-N03). */
@Service
public class ObligationService {

    public record ObligationView(FilingObligation obligation, CaseRecord legalCase,
                                 DeadlineState deadlineState, String escalationState,
                                 List<String> escalationRecipients) {
    }

    private final CaseService caseService;
    private final ObligationRepository repository;
    private final DeadlineStatePolicy policy = new DeadlineStatePolicy(new WorkingDayCalendar());
    private final Clock clock;

    public ObligationService(CaseService caseService, ObligationRepository repository, Clock clock) {
        this.caseService = caseService;
        this.repository = repository;
        this.clock = clock;
    }

    /** AC-N01, AC-N04, AC-N05 */
    public ObligationView create(long caseId, String dueDate) {
        CaseRecord legalCase = caseService.getCase(caseId);
        LocalDate parsed;
        try {
            parsed = CaseDates.parse(dueDate);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e.getMessage());
        }
        LocalDate effective = policy.effectiveDueDate(parsed);
        FilingObligation stored = repository.add(caseId, CaseDates.format(parsed), CaseDates.format(effective));
        return view(stored, legalCase, today());
    }

    public ObligationView get(long caseId, long obligationId) {
        CaseRecord legalCase = caseService.getCase(caseId);
        FilingObligation obligation = repository.findById(obligationId)
                .filter(o -> o.caseId() == caseId)
                .orElseThrow(() -> new NotFoundException("Obligation " + obligationId + " not found"));
        return view(obligation, legalCase, today());
    }

    /** AC-N03: only obligations of active cases, earliest effective due date first. */
    public List<ObligationView> dueSoon() {
        Map<Long, CaseRecord> activeCases = caseService.listCases().stream()
                .collect(Collectors.toMap(CaseRecord::id, Function.identity()));
        LocalDate today = today();
        return repository.findAll().stream()
                .filter(o -> activeCases.containsKey(o.caseId()))
                .map(o -> view(o, activeCases.get(o.caseId()), today))
                .filter(v -> v.deadlineState() != DeadlineState.ON_TRACK)
                .sorted(Comparator.comparing((ObligationView v) -> CaseDates.parse(v.obligation().effectiveDueDate()))
                        .thenComparing(v -> v.obligation().id()))
                .toList();
    }

    private ObligationView view(FilingObligation obligation, CaseRecord legalCase, LocalDate today) {
        DeadlineState state = policy.stateFor(CaseDates.parse(obligation.effectiveDueDate()), today);
        if (state == DeadlineState.ON_TRACK) {
            return new ObligationView(obligation, legalCase, state, "NONE", List.of());
        }
        return new ObligationView(obligation, legalCase, state, "ESCALATED", List.of(legalCase.ownerEmail()));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
