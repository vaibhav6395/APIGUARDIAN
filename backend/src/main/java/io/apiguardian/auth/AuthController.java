package io.apiguardian.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}

    @PostMapping("/register")
    public Map<String,Object> register(@Valid @RequestBody RegisterBody body){
        return service.register(new AuthService.RegisterRequest(body.email(),body.password(),body.displayName(),body.workspaceName()));
    }
    @PostMapping("/login")
    public Map<String,Object> login(@Valid @RequestBody LoginBody body){
        return service.login(new AuthService.LoginRequest(body.email(),body.password()));
    }
    public record RegisterBody(@NotBlank @Email @Size(max=254) String email,
        @NotBlank @Size(min=12,max=72) String password,
        @NotBlank @Size(max=100) String displayName,
        @NotBlank @Size(max=120) String workspaceName) {}
    public record LoginBody(@NotBlank @Email String email,@NotBlank String password) {}
}
