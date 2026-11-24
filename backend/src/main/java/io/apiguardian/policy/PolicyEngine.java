package io.apiguardian.policy;

import org.springframework.stereotype.Service;

@Service
public class PolicyEngine {
    public PolicyEvaluation evaluate(PolicyRule rule, AccessDecision observed) {
        boolean matched = rule.expected() == observed;
        String explanation = matched
                ? "Observed access matches the configured authorization policy."
                : "Authorization mismatch: expected " + rule.expected() + " but observed " + observed + ".";
        return new PolicyEvaluation(rule.role(), rule.resource(), rule.action(),
                rule.expected(), observed, matched, explanation);
    }
}
