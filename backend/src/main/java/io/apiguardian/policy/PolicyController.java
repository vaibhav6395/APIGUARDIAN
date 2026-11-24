package io.apiguardian.policy;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/policies")
public class PolicyController {
    private final PolicyEngine engine;
    private final JdbcTemplate jdbc;
    public PolicyController(PolicyEngine engine, JdbcTemplate jdbc) { this.engine=engine; this.jdbc=jdbc; }

    @GetMapping
    public List<PolicyView> list(@PathVariable long projectId, @AuthenticationPrincipal Jwt jwt) {
        requireProjectAccess(projectId, userId(jwt));
        return jdbc.query("SELECT id,project_id,role_name,resource,action,expected_decision FROM policy_rules WHERE project_id=? ORDER BY id",
            (rs,n)->new PolicyView(rs.getLong("id"),rs.getLong("project_id"),rs.getString("role_name"),
                rs.getString("resource"),rs.getString("action"),AccessDecision.valueOf(rs.getString("expected_decision"))),projectId);
    }

    @GetMapping("/evaluations")
    public List<EvaluationView> evaluations(@PathVariable long projectId,@AuthenticationPrincipal Jwt jwt) {
        requireProjectAccess(projectId,userId(jwt));
        return jdbc.query("""
            SELECT id,role_name,resource,action,expected_decision,observed_decision,matched,explanation,created_at
            FROM policy_evaluations WHERE project_id=? ORDER BY created_at DESC LIMIT 100
            """,(rs,n)->new EvaluationView(rs.getLong("id"),rs.getString("role_name"),rs.getString("resource"),
            rs.getString("action"),AccessDecision.valueOf(rs.getString("expected_decision")),
            AccessDecision.valueOf(rs.getString("observed_decision")),rs.getBoolean("matched"),
            rs.getString("explanation"),rs.getObject("created_at",java.time.OffsetDateTime.class)),projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyView save(@PathVariable long projectId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PolicyRule rule) {
        requireProjectAccess(projectId, userId(jwt));
        return jdbc.queryForObject("""
            INSERT INTO policy_rules(project_id,role_name,resource,action,expected_decision)
            VALUES (?,?,?,?,?)
            ON CONFLICT(project_id,role_name,resource,action)
            DO UPDATE SET expected_decision=EXCLUDED.expected_decision
            RETURNING id,project_id,role_name,resource,action,expected_decision
            """,
            (rs,n)->new PolicyView(rs.getLong("id"),rs.getLong("project_id"),rs.getString("role_name"),
                rs.getString("resource"),rs.getString("action"),AccessDecision.valueOf(rs.getString("expected_decision"))),
            projectId,rule.role().trim().toUpperCase(),rule.resource().trim(),rule.action().trim().toUpperCase(),rule.expected().name());
    }

    @PostMapping("/evaluate")
    public PolicyEvaluation evaluate(@PathVariable long projectId, @AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody EvaluationRequest request) {
        long userId=userId(jwt);
        requireProjectAccess(projectId, userId);
        PolicyEvaluation result=engine.evaluate(request.rule(), request.observed());
        Long evaluationId=jdbc.queryForObject("""
            INSERT INTO policy_evaluations(project_id,created_by,role_name,resource,action,expected_decision,observed_decision,matched,explanation)
            VALUES (?,?,?,?,?,?,?,?,?) RETURNING id
            """,Long.class,projectId,userId,result.role(),result.resource(),result.action(),
            result.expected().name(),result.observed().name(),result.matched(),result.explanation());
        if(!result.matched()) {
            String severity=result.expected()==AccessDecision.DENY && result.observed()==AccessDecision.ALLOW ? "CRITICAL" : "MEDIUM";
            String title="Unexpected "+result.observed()+" decision for "+result.role();
            jdbc.update("INSERT INTO findings(project_id,evaluation_id,title,severity,details) VALUES (?,?,?,?,?)",
                projectId,evaluationId,title,severity,result.explanation());
        }
        return result;
    }

    private long userId(Jwt jwt) {
        try { return Long.parseLong(jwt.getSubject()); }
        catch (RuntimeException ex) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED); }
    }
    private void requireProjectAccess(long projectId,long userId) {
        Boolean allowed=jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM projects p
            JOIN workspace_memberships m ON m.workspace_id=p.workspace_id
            WHERE p.id=? AND m.user_id=?)
            """,Boolean.class,projectId,userId);
        if(!Boolean.TRUE.equals(allowed)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found");
    }
    public record EvaluationRequest(@Valid @NotNull PolicyRule rule,@NotNull AccessDecision observed) {}
    public record EvaluationView(long id,String role,String resource,String action,AccessDecision expected,AccessDecision observed,boolean matched,String explanation,java.time.OffsetDateTime createdAt) {}
    public record PolicyView(long id,long projectId,String role,String resource,String action,AccessDecision expected) {}
}
