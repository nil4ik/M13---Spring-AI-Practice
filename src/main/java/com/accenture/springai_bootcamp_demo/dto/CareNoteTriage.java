package com.accenture.springai_bootcamp_demo.dto;

import java.util.List;

public record CareNoteTriage(
        Severity severity,
        String summary,
        List<String> recommendedActions,
        boolean vetRequired) {
}
