package io.apiguardian.policy;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/runs")
public class TestRunController {
    private final JdbcTemplate jdbc;
    public TestRunController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @PostMapping("/demo")
    @Transactional
    public TestRunView runDemo(@PathVariable long projectId,@AuthenticationPrincipal Jwt jwt){
        long userId=uid(jwt);requireAccess(projectId,userId);
        List<SavedPolicy> rules=jdbc.query("""
            SELECT id,role_name,resource,action,expected_decision FROM policy_rules
            WHERE project_id=? ORDER BY id
            """,(rs,n)->new SavedPolicy(rs.getLong("id"),new PolicyRule(rs.getString("role_name"),
            rs.getString("resource"),rs.getString("action"),AccessDecision.valueOf(rs.getString("expected_decision")))),projectId);
        if(rules.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Save at least one policy before running the demo checks");
        Long runId=jdbc.queryForObject("""
            INSERT INTO test_runs(project_id,created_by,total_tests,passed_tests,failed_tests,status)
            VALUES (?,?,0,0,0,'COMPLETED') RETURNING id
            """,Long.class,projectId,userId);
        int passed=0,failed=0;
        for(SavedPolicy saved:rules){
            PolicyRule rule=saved.rule();
            AccessDecision observed=demoDecision(rule.role(),rule.resource(),rule.action());
            boolean matched=rule.expected()==observed;
            String explanation=matched?"Bundled demo API decision matches the saved policy.":
                "Bundled demo API returned "+observed+" but the saved policy expects "+rule.expected()+".";
            Long evaluationId=jdbc.queryForObject("""
                INSERT INTO policy_evaluations(run_id,project_id,created_by,role_name,resource,action,expected_decision,observed_decision,matched,explanation)
                VALUES (?,?,?,?,?,?,?,?,?,?) RETURNING id
                """,Long.class,runId,projectId,userId,rule.role(),rule.resource(),rule.action(),
                rule.expected().name(),observed.name(),matched,explanation);
            if(matched) passed++; else {
                failed++;
                String severity=rule.expected()==AccessDecision.DENY && observed==AccessDecision.ALLOW?"CRITICAL":"MEDIUM";
                jdbc.update("INSERT INTO findings(project_id,evaluation_id,title,severity,details) VALUES (?,?,?,?,?)",
                    projectId,evaluationId,"Demo authorization mismatch: "+rule.role(),severity,explanation);
            }
        }
        jdbc.update("UPDATE test_runs SET total_tests=?,passed_tests=?,failed_tests=? WHERE id=?",
            rules.size(),passed,failed,runId);
        return new TestRunView(runId,projectId,rules.size(),passed,failed,"COMPLETED",OffsetDateTime.now());
    }

    @GetMapping
    public List<TestRunView> list(@PathVariable long projectId,@AuthenticationPrincipal Jwt jwt){
        requireAccess(projectId,uid(jwt));
        return jdbc.query("""
            SELECT id,project_id,total_tests,passed_tests,failed_tests,status,created_at
            FROM test_runs WHERE project_id=? ORDER BY created_at DESC LIMIT 100
            """,(rs,n)->new TestRunView(rs.getLong("id"),rs.getLong("project_id"),rs.getInt("total_tests"),
            rs.getInt("passed_tests"),rs.getInt("failed_tests"),rs.getString("status"),
            rs.getObject("created_at",OffsetDateTime.class)),projectId);
    }

    private AccessDecision demoDecision(String role,String resource,String action){
        String r=role.toUpperCase(),path=resource.toLowerCase(),verb=action.toUpperCase();
        if(!verb.equals("READ")&&!verb.equals("GET")) return AccessDecision.DENY;
        if(path.equals("profile:self")) return AccessDecision.ALLOW;
        if(path.equals("orders:own")) return r.equals("USER")||r.equals("ADMIN")?AccessDecision.ALLOW:AccessDecision.DENY;
        if(path.equals("admin:settings")||path.equals("users:manage")) return r.equals("ADMIN")?AccessDecision.ALLOW:AccessDecision.DENY;
        return AccessDecision.DENY;
    }
    private long uid(Jwt jwt){try{return Long.parseLong(jwt.getSubject());}catch(RuntimeException e){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);}}
    private void requireAccess(long projectId,long userId){
        Boolean allowed=jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM projects p JOIN workspace_memberships m ON m.workspace_id=p.workspace_id WHERE p.id=? AND m.user_id=?)",Boolean.class,projectId,userId);
        if(!Boolean.TRUE.equals(allowed)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found");
    }
    private record SavedPolicy(long id,PolicyRule rule){}
    public record TestRunView(long id,long projectId,int totalTests,int passedTests,int failedTests,String status,OffsetDateTime createdAt){}
}
