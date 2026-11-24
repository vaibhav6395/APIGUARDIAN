package io.apiguardian.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expirationMinutes;

    public AuthService(JdbcTemplate jdbc, PasswordEncoder passwords, JwtEncoder jwtEncoder,
        @Value("${security.jwt.issuer}") String issuer,
        @Value("${security.jwt.expiration-minutes}") long expirationMinutes) {
        this.jdbc=jdbc; this.passwords=passwords; this.jwtEncoder=jwtEncoder;
        this.issuer=issuer; this.expirationMinutes=expirationMinutes;
    }

    @Transactional
    public Map<String,Object> register(RegisterRequest request) {
        String email=request.email().trim().toLowerCase();
        if (jdbc.queryForList("SELECT id FROM app_users WHERE email=?", Long.class, email).size()>0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"An account with this email already exists");
        Long id=jdbc.queryForObject("INSERT INTO app_users(email,password_hash,display_name) VALUES (?,?,?) RETURNING id",
            Long.class,email,passwords.encode(request.password()),request.displayName().trim());
        Long workspace=jdbc.queryForObject("INSERT INTO workspaces(name,created_by) VALUES (?,?) RETURNING id",
            Long.class,request.workspaceName().trim(),id);
        jdbc.update("INSERT INTO workspace_memberships(workspace_id,user_id,role) VALUES (?,?, 'OWNER')",workspace,id);
        return tokenResponse(id,email,request.displayName().trim(),workspace);
    }

    public Map<String,Object> login(LoginRequest request) {
        var users=jdbc.query("SELECT id,email,password_hash,display_name FROM app_users WHERE email=?",
            (rs,n)->new Object[]{rs.getLong("id"),rs.getString("email"),rs.getString("password_hash"),rs.getString("display_name")},
            request.email().trim().toLowerCase());
        if(users.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");
        Object[] user=users.get(0);
        if(!passwords.matches(request.password(),(String)user[2]))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");
        Long workspace=jdbc.query("SELECT workspace_id FROM workspace_memberships WHERE user_id=? ORDER BY id LIMIT 1",
            rs->rs.next()?rs.getLong(1):null,(Long)user[0]);
        return tokenResponse((Long)user[0],(String)user[1],(String)user[3],workspace);
    }

    private Map<String,Object> tokenResponse(Long id,String email,String name,Long workspace) {
        Instant now=Instant.now();
        var claims=JwtClaimsSet.builder().issuer(issuer).issuedAt(now)
            .expiresAt(now.plus(expirationMinutes,ChronoUnit.MINUTES)).subject(id.toString())
            .claim("email",email).claim("name",name).build();
        String token=jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return Map.of("accessToken",token,"tokenType","Bearer","expiresIn",expirationMinutes*60,
            "user",Map.of("id",id,"email",email,"displayName",name),"workspaceId",workspace==null?0L:workspace);
    }

    public record RegisterRequest(String email,String password,String displayName,String workspaceName) {}
    public record LoginRequest(String email,String password) {}
}
