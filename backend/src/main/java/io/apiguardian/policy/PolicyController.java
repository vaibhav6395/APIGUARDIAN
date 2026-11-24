package io.apiguardian.policy;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/policies/evaluate")
public class PolicyController {
    private final PolicyEngine engine;

    public PolicyController(PolicyEngine engine) {
        this.engine = engine;
    }

    @PostMapping
    public PolicyEvaluation evaluate(@Valid @RequestBody EvaluationRequest request) {
        return engine.evaluate(request.rule(), request.observed());
    }

    public record EvaluationRequest(@Valid PolicyRule rule, AccessDecision observed) {}
}
