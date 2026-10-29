package com.accenture.springai_bootcamp_demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CareNoteRequest(
        @NotBlank(message = "animalName must not be blank")
        @Size(max = 100, message = "animalName must not exceed 100 characters")
        String animalName,
        @NotBlank(message = "species must not be blank")
        @Size(max = 100, message = "species must not exceed 100 characters")
        String species,
        @NotBlank(message = "note must not be blank")
        @Size(max = 2000, message = "note must not exceed 2000 characters")
        String note) {
}
