package example.demo;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;

    public AuthenticationController(PasswordEncoder encoder, UserRepository userRepository) {
        this.encoder = encoder;
        this.userRepository = userRepository;
    }

    // JSON endpoint
    @PostMapping(value = "/api/register", consumes = "application/json")
    public ResponseEntity<?> registerJson(@RequestBody LoginRequest request) {
        return processRegister(request.getUsername(), request.getPassword());
    }

    // Form-data endpoint
    @PostMapping(value = "/api/register", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> registerForm(
            @RequestParam String username,
            @RequestParam String password) {
        return processRegister(username, password);
    }

    private ResponseEntity<?> processRegister(String username, String password) {
        if (username == null || password == null) {
            return ResponseEntity.status(400).body("Username and password are required");
        }

        Optional<UserEntity> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            return ResponseEntity.status(409).body("User already exists");
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setPassword(encoder.encode(password));
        newUser.setRole("USER");
        userRepository.save(newUser);

        return ResponseEntity.status(201).body(new LoginResponse(newUser.getUsername(), "User registered successfully"));
    }

    // JSON endpoint
    @PostMapping(value = "/api/login", consumes = "application/json")
    public ResponseEntity<?> loginJson(@RequestBody LoginRequest request) {
        return processLogin(request.getUsername(), request.getPassword());
    }

    // Form-data endpoint
    @PostMapping(value = "/api/login", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> loginForm(
            @RequestParam String username,
            @RequestParam String password) {
        return processLogin(username, password);
    }

    private ResponseEntity<?> processLogin(String username, String password) {
        if (username == null || password == null) {
            return ResponseEntity.status(400).body("Username and password are required");
        }

        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        UserEntity userEntity = userOpt.get();

        if (!encoder.matches(password, userEntity.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        return ResponseEntity.ok(new LoginResponse(userEntity.getUsername(), "Login success"));
    }
}
