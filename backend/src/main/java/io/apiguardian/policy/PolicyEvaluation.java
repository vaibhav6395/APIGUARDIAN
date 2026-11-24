package io.apiguardian.policy;

public record PolicyEvaluation(
        String role,
        String resource,
        String action,
        AccessDecision expected,
        AccessDecision observed,
        boolean matched,
        String explanation
) {}
