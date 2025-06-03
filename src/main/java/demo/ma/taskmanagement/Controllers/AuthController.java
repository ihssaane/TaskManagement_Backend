package demo.ma.taskmanagement.Controllers;


import demo.ma.taskmanagement.dto.JwtResponse;
import demo.ma.taskmanagement.dto.LoginRequest;
import demo.ma.taskmanagement.dto.RegisterRequest;
import demo.ma.taskmanagement.dto.UserDto;
import demo.ma.taskmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());
        UserDto user = authService.register(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Dans une implémentation JWT stateless, le logout côté serveur est optionnel
        // Le client supprime simplement le token
        return ResponseEntity.ok("Logged out successfully");
    }
}
