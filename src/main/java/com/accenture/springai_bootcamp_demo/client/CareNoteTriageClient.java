package com.accenture.springai_bootcamp_demo.client;

import com.accenture.springai_bootcamp_demo.dto.CareNoteRequest;
import com.accenture.springai_bootcamp_demo.dto.CareNoteTriage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CareNoteTriageClient {
    private static final String SYSTEM_PROMPT = """
            You are a care-note triage assistant for zoo keepers.
            A keeper describes an observation about a single animal. Classify how urgent it is
            and tell the keeper what to do next. Base your answer only on the note and do not invent facts.

            Severity rules, checked in this order:
            1. HIGH if the note mentions any of: limping or lameness, an injury or wound, bleeding,
               breathing problems, collapse, seizures, or the animal not eating for a day or longer.
            2. MEDIUM if the note describes a noticeable change in behaviour, appetite or droppings
               that is not covered by rule 1.
            3. LOW otherwise, for normal behaviour or a minor issue that only needs routine monitoring.

            Answer with exactly one JSON object with these fields:
            - severity: LOW, MEDIUM or HIGH according to the rules above.
            - summary: one or two sentences describing the situation.
            - recommendedActions: between 1 and 5 short, concrete actions for the keeper.
            - vetRequired: true if a veterinarian should examine the animal. Always true when severity is HIGH.
            """;

    private final ChatClient chatClient;

    public CareNoteTriageClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public CareNoteTriage triage(CareNoteRequest request) {
        try {
            return chatClient.prompt()
                    .user(toUserPrompt(request))
                    .call()
                    .entity(CareNoteTriage.class, spec -> spec.useProviderStructuredOutput());
        } catch (RuntimeException ex) {
            log.error("Care note triage request failed", ex);
            throw new OpenRouterException("Failed to get a care note triage from the model: " + ex.getMessage(), ex);
        }
    }

    private String toUserPrompt(CareNoteRequest request) {
        return """
                Animal name: %s
                Species: %s
                Keeper note: %s
                """.formatted(request.animalName(), request.species(), request.note());
    }
}
