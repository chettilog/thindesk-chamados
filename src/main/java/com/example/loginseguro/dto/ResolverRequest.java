package com.example.loginseguro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolverRequest(
        @NotBlank(message = "A solução é obrigatória")
        @Size(max = 2000, message = "A solução pode ter no máximo 2000 caracteres")
        String solucao
) {}