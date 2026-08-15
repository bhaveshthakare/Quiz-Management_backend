package com.quizplatform.backend.dto;

import com.quizplatform.backend.enums.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionDtos() {

    public record OptionInput(
            @NotBlank @Size(max = 255) String optionText,
            @NotNull Boolean isCorrect) {}

    public record QuestionRequest(
            @NotBlank String questionText,
            @NotNull Integer marks,
            @NotNull Difficulty difficulty,
            String explanation,
            @NotNull @Size(min = 2, max = 10) List<@Valid OptionInput> options) {}

    public record OptionResponse(
            Long id,
            String optionText,
            Boolean isCorrect) {}

    public record QuestionResponse(
            Long id,
            Long quizId,
            String questionText,
            Integer marks,
            Difficulty difficulty,
            String explanation,
            List<OptionResponse> options) {}

    public record ImportResponse(
            int imported,
            int totalRows,
            int failed,
            List<String> errors) {}
}