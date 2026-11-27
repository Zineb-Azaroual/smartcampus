
package com.smartcampus.config;

import com.smartcampus.auth.InMemoryUserRepository;
import com.smartcampus.auth.LoginService;
import com.smartcampus.auth.PasswordHasher;
import com.smartcampus.auth.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;

@Configuration
public class AuthConfig {

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new PasswordHasher();
    }

    @Bean
    public LoginService loginService(
            UserRepository userRepository,
            PasswordHasher passwordHasher) {
        return new LoginService(userRepository, passwordHasher);
    }
    @Bean
    public CommandLineRunner initDemoUser(LoginService loginService) {
        return args -> loginService.register(
                System.getenv().getOrDefault("SC_DEMO_EMAIL", "etudiant@smartcampus.ma"),
                System.getenv().getOrDefault("SC_DEMO_PASSWORD", "Campus2026!"),
                "STUDENT"
        );
    }
}
