package com.accenture.springai_bootcamp_demo.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accenture.springai_bootcamp_demo.dto.CareNoteRequest;
import com.accenture.springai_bootcamp_demo.dto.CareNoteTriage;
import com.accenture.springai_bootcamp_demo.dto.Severity;
import com.accenture.springai_bootcamp_demo.service.CareNoteTriageService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CareNoteController.class)
class CareNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CareNoteTriageService careNoteTriageService;

    @Test
    void returnsTriageForValidRequest() throws Exception {
        CareNoteRequest request = new CareNoteRequest(
                "Leo", "African lion", "Leo has not eaten for two days and is limping.");
        when(careNoteTriageService.triage(request)).thenReturn(new CareNoteTriage(
                Severity.HIGH,
                "Leo has refused food for two days and is limping.",
                List.of("Call the veterinarian", "Move Leo to a quiet holding area"),
                true));

        mockMvc.perform(post("/api/care-notes/triage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"animalName":"Leo","species":"African lion","note":"Leo has not eaten for two days and is limping."}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "severity": "HIGH",
                          "summary": "Leo has refused food for two days and is limping.",
                          "recommendedActions": ["Call the veterinarian", "Move Leo to a quiet holding area"],
                          "vetRequired": true
                        }
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void rejectsBlankNote() throws Exception {
        mockMvc.perform(post("/api/care-notes/triage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"animalName":"Leo","species":"African lion","note":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("note: note must not be blank"));

        verifyNoInteractions(careNoteTriageService);
    }
}
