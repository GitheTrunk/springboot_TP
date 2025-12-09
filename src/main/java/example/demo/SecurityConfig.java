package example.demo;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import example.demo.Jwt.JwtFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        SecureRandom secureRandom = new SecureRandom();
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (Exception e) {
            secureRandom = new SecureRandom();
        }
        return new BCryptPasswordEncoder(12, secureRandom);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .csrf(crsf -> crsf.disable())
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/login", "/api/register", "/login", "/register").permitAll()
            .requestMatchers("/products").permitAll()
            .anyRequest().permitAll() 
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
