package io.apiguardian.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/projects")
public class ProjectController {
    private final JdbcTemplate jdbc;
    public ProjectController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping
    public List<ProjectView> list(@PathVariable long workspaceId,@AuthenticationPrincipal Jwt jwt){
        long userId=userId(jwt); requireMembership(workspaceId,userId);
        return jdbc.query("SELECT id,workspace_id,name,description,created_at,updated_at FROM projects WHERE workspace_id=? ORDER BY created_at DESC",
            (rs,n)->new ProjectView(rs.getLong("id"),rs.getLong("workspace_id"),rs.getString("name"),rs.getString("description"),
                rs.getObject("created_at",OffsetDateTime.class),rs.getObject("updated_at",OffsetDateTime.class)),workspaceId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectView create(@PathVariable long workspaceId,@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody CreateProject body){
        long userId=userId(jwt); requireMembership(workspaceId,userId);
        return jdbc.queryForObject("INSERT INTO projects(workspace_id,name,description,created_by) VALUES (?,?,?,?) RETURNING id,workspace_id,name,description,created_at,updated_at",
            (rs,n)->new ProjectView(rs.getLong("id"),rs.getLong("workspace_id"),rs.getString("name"),rs.getString("description"),
                rs.getObject("created_at",OffsetDateTime.class),rs.getObject("updated_at",OffsetDateTime.class)),
            workspaceId,body.name().trim(),body.description(),userId);
    }

    private void requireMembership(long workspaceId,long userId){
        Boolean member=jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM workspace_memberships WHERE workspace_id=? AND user_id=?)",
            Boolean.class,workspaceId,userId);
        if(!Boolean.TRUE.equals(member)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Workspace not found");
    }
    private long userId(Jwt jwt){try{return Long.parseLong(jwt.getSubject());}catch(Exception e){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);}}
    public record CreateProject(@NotBlank @Size(max=120) String name,@Size(max=1000) String description){}
    public record ProjectView(long id,long workspaceId,String name,String description,OffsetDateTime createdAt,OffsetDateTime updatedAt){}
}
