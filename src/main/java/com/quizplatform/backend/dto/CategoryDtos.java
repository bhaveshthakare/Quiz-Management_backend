package com.quizplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDtos() {

    public record CategoryRequest(
            @NotBlank @Size(min = 2, max = 100) String name,
            @Size(max = 255) String description) {}

    public record CategoryResponse(
            Long id,
            String name,
            String description,
            long quizCount) {}
}