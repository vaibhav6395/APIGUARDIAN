package io.apiguardian.policy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PolicyRule(
        @NotBlank String role,
        @NotBlank String resource,
        @NotBlank String action,
        @NotNull AccessDecision expected
) {}
