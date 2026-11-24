package io.apiguardian.policy;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/findings")
public class FindingController {
    private final JdbcTemplate jdbc;
    public FindingController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping
    public List<FindingView> list(@PathVariable long projectId,@AuthenticationPrincipal Jwt jwt){
        requireAccess(projectId,uid(jwt));
        return jdbc.query("""
            SELECT id,project_id,evaluation_id,title,severity,status,details,created_at,updated_at
            FROM findings WHERE project_id=? ORDER BY created_at DESC
            """,(rs,n)->new FindingView(rs.getLong("id"),rs.getLong("project_id"),rs.getLong("evaluation_id"),
            rs.getString("title"),rs.getString("severity"),rs.getString("status"),rs.getString("details"),
            rs.getObject("created_at",OffsetDateTime.class),rs.getObject("updated_at",OffsetDateTime.class)),projectId);
    }

    @PatchMapping("/{findingId}")
    public FindingView update(@PathVariable long projectId,@PathVariable long findingId,
        @AuthenticationPrincipal Jwt jwt,@Valid @RequestBody UpdateFinding body){
        requireAccess(projectId,uid(jwt));
        int changed=jdbc.update("UPDATE findings SET status=?,updated_at=CURRENT_TIMESTAMP WHERE id=? AND project_id=?",
            body.status().name(),findingId,projectId);
        if(changed==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Finding not found");
        return jdbc.queryForObject("""
            SELECT id,project_id,evaluation_id,title,severity,status,details,created_at,updated_at
            FROM findings WHERE id=? AND project_id=?
            """,(rs,n)->new FindingView(rs.getLong("id"),rs.getLong("project_id"),rs.getLong("evaluation_id"),
            rs.getString("title"),rs.getString("severity"),rs.getString("status"),rs.getString("details"),
            rs.getObject("created_at",OffsetDateTime.class),rs.getObject("updated_at",OffsetDateTime.class)),findingId,projectId);
    }

    private long uid(Jwt jwt){try{return Long.parseLong(jwt.getSubject());}catch(RuntimeException e){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);}}
    private void requireAccess(long projectId,long userId){
        Boolean allowed=jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM projects p JOIN workspace_memberships m ON m.workspace_id=p.workspace_id WHERE p.id=? AND m.user_id=?)",Boolean.class,projectId,userId);
        if(!Boolean.TRUE.equals(allowed)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found");
    }
    public record UpdateFinding(@NotNull FindingStatus status){}
    public enum FindingStatus { OPEN, ACKNOWLEDGED, RESOLVED }
    public record FindingView(long id,long projectId,long evaluationId,String title,String severity,String status,
        String details,OffsetDateTime createdAt,OffsetDateTime updatedAt){}
}
