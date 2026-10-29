package com.accenture.springai_bootcamp_demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accenture.springai_bootcamp_demo.client.CareNoteTriageClient;
import com.accenture.springai_bootcamp_demo.client.OpenRouterException;
import com.accenture.springai_bootcamp_demo.dto.CareNoteRequest;
import com.accenture.springai_bootcamp_demo.dto.CareNoteTriage;
import com.accenture.springai_bootcamp_demo.dto.Severity;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CareNoteTriageServiceTest {

    private static final CareNoteRequest REQUEST = new CareNoteRequest(
            "Leo", "African lion", "Leo has not eaten for two days and is limping on his front left leg.");

    private static final CareNoteTriage VALID_TRIAGE = new CareNoteTriage(
            Severity.HIGH,
            "Leo has refused food for two days and is limping.",
            List.of("Call the veterinarian", "Move Leo to a quiet holding area"),
            true);

    @Mock
    private CareNoteTriageClient careNoteTriageClient;

    @InjectMocks
    private CareNoteTriageService careNoteTriageService;

    @Test
    void returnsValidTriage() {
        when(careNoteTriageClient.triage(REQUEST)).thenReturn(VALID_TRIAGE);

        CareNoteTriage result = careNoteTriageService.triage(REQUEST);

        assertThat(result).isEqualTo(VALID_TRIAGE);
        verify(careNoteTriageClient, times(1)).triage(REQUEST);
    }

    @Test
    void retriesExactlyOnceWhenOutputIsInvalid() {
        CareNoteTriage blankSummary = new CareNoteTriage(Severity.LOW, " ", List.of("Keep monitoring"), false);
        when(careNoteTriageClient.triage(REQUEST)).thenReturn(blankSummary, VALID_TRIAGE);

        CareNoteTriage result = careNoteTriageService.triage(REQUEST);

        assertThat(result).isEqualTo(VALID_TRIAGE);
        verify(careNoteTriageClient, times(2)).triage(REQUEST);
    }

    @Test
    void retriesExactlyOnceWhenOutputIsUnparseable() {
        when(careNoteTriageClient.triage(REQUEST))
                .thenThrow(new OpenRouterException("Cannot deserialize value of type Severity"))
                .thenReturn(VALID_TRIAGE);

        CareNoteTriage result = careNoteTriageService.triage(REQUEST);

        assertThat(result).isEqualTo(VALID_TRIAGE);
        verify(careNoteTriageClient, times(2)).triage(REQUEST);
    }

    @Test
    void throwsAfterTwoFailedAttempts() {
        when(careNoteTriageClient.triage(REQUEST)).thenThrow(new OpenRouterException("Connection refused"));

        assertThatThrownBy(() -> careNoteTriageService.triage(REQUEST))
                .isInstanceOf(OpenRouterException.class)
                .hasMessageContaining("failed after 2 attempts");
        verify(careNoteTriageClient, times(2)).triage(REQUEST);
    }

    @Test
    void rejectsHighSeverityWithoutVet() {
        CareNoteTriage highWithoutVet = new CareNoteTriage(
                Severity.HIGH, "Leo is limping.", List.of("Watch Leo closely"), false);
        when(careNoteTriageClient.triage(REQUEST)).thenReturn(highWithoutVet);

        assertThatThrownBy(() -> careNoteTriageService.triage(REQUEST))
                .isInstanceOf(OpenRouterException.class)
                .hasMessageContaining("vetRequired must be true when severity is HIGH");
        verify(careNoteTriageClient, times(2)).triage(REQUEST);
    }

    @ParameterizedTest
    @MethodSource("invalidTriages")
    void rejectsInvalidOutput(CareNoteTriage invalidTriage) {
        when(careNoteTriageClient.triage(REQUEST)).thenReturn(invalidTriage);

        assertThatThrownBy(() -> careNoteTriageService.triage(REQUEST))
                .isInstanceOf(OpenRouterException.class)
                .hasMessageContaining("invalid care note triage");
        verify(careNoteTriageClient, times(2)).triage(REQUEST);
    }

    private static Stream<CareNoteTriage> invalidTriages() {
        return Stream.of(
                null,
                new CareNoteTriage(null, "Leo is limping.", List.of("Call the veterinarian"), true),
                new CareNoteTriage(Severity.MEDIUM, "Leo is limping.", null, true),
                new CareNoteTriage(Severity.MEDIUM, "Leo is limping.", List.of(), true),
                new CareNoteTriage(Severity.MEDIUM, "Leo is limping.", List.of("Call the veterinarian", " "), true),
                new CareNoteTriage(Severity.MEDIUM, "Leo is limping.", List.of("1", "2", "3", "4", "5", "6"), true));
    }
}
