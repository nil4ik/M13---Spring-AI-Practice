package com.accenture.springai_bootcamp_demo.controller;

import com.accenture.springai_bootcamp_demo.dto.CareNoteRequest;
import com.accenture.springai_bootcamp_demo.dto.CareNoteTriage;
import com.accenture.springai_bootcamp_demo.service.CareNoteTriageService;
import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/care-notes")
@AllArgsConstructor
public class CareNoteController {

    private final CareNoteTriageService careNoteTriageService;

    @PostMapping("/triage")
    public CareNoteTriage triage(@Valid @RequestBody CareNoteRequest request) {
        return careNoteTriageService.triage(request);
    }
}
