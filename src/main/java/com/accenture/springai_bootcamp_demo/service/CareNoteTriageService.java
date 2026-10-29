package com.accenture.springai_bootcamp_demo.service;

import com.accenture.springai_bootcamp_demo.client.CareNoteTriageClient;
import com.accenture.springai_bootcamp_demo.client.OpenRouterException;
import com.accenture.springai_bootcamp_demo.dto.CareNoteRequest;
import com.accenture.springai_bootcamp_demo.dto.CareNoteTriage;
import com.accenture.springai_bootcamp_demo.dto.Severity;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@AllArgsConstructor
public class CareNoteTriageService {
    private static final int MAX_ATTEMPTS = 2;
    private static final int MAX_ACTIONS = 5;

    private final CareNoteTriageClient careNoteTriageClient;

    public CareNoteTriage triage(CareNoteRequest request) {
        OpenRouterException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return validate(careNoteTriageClient.triage(request));
            } catch (OpenRouterException ex) {
                log.warn("Care note triage attempt {} of {} failed: {}", attempt, MAX_ATTEMPTS, ex.getMessage());
                lastFailure = ex;
            }
        }
        throw new OpenRouterException(
                "Care note triage failed after " + MAX_ATTEMPTS + " attempts: " + lastFailure.getMessage(), lastFailure);
    }

    private CareNoteTriage validate(CareNoteTriage triage) {
        if (triage == null) {
            throw invalid("the response was empty");
        }
        if (triage.severity() == null) {
            throw invalid("severity is missing");
        }
        if (!StringUtils.hasText(triage.summary())) {
            throw invalid("summary is blank");
        }
        if (!hasValidActions(triage.recommendedActions())) {
            throw invalid("recommendedActions must contain 1 to " + MAX_ACTIONS + " non-blank items");
        }
        if (triage.severity() == Severity.HIGH && !triage.vetRequired()) {
            throw invalid("vetRequired must be true when severity is HIGH");
        }
        return triage;
    }

    private boolean hasValidActions(List<String> actions) {
        return actions != null
                && !actions.isEmpty()
                && actions.size() <= MAX_ACTIONS
                && actions.stream().allMatch(StringUtils::hasText);
    }

    private OpenRouterException invalid(String reason) {
        return new OpenRouterException("Model returned an invalid care note triage: " + reason);
    }
}
