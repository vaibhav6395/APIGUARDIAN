package io.apiguardian.policy;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PolicyEngineTest {
    private final PolicyEngine engine = new PolicyEngine();

    @Test
    void reportsMatchingAuthorizationDecision() {
        var result = engine.evaluate(
                new PolicyRule("USER", "orders:own", "READ", AccessDecision.ALLOW),
                AccessDecision.ALLOW);
        assertThat(result.matched()).isTrue();
    }

    @Test
    void reportsUnexpectedAuthorizationGrant() {
        var result = engine.evaluate(
                new PolicyRule("USER", "admin:settings", "READ", AccessDecision.DENY),
                AccessDecision.ALLOW);
        assertThat(result.matched()).isFalse();
        assertThat(result.explanation()).contains("Authorization mismatch");
    }
}
