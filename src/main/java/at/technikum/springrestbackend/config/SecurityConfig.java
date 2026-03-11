package at.technikum.springrestbackend.config;

import at.technikum.springrestbackend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                    // Auth endpoints
                    .requestMatchers("/auth/**").permitAll()

                    // Swagger
                    .requestMatchers("/swagger.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                    // Homepage
                    .requestMatchers(HttpMethod.GET, "/").permitAll()

                    // Posts - anyone can read
                    .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()

                    // Comments - anyone can read
                    .requestMatchers(HttpMethod.GET, "/posts/*/comments").permitAll()

                    // Posts - only authenticated users and admins can create/update/delete
                    .requestMatchers(HttpMethod.POST, "/posts").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/posts/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/posts/**").hasAnyRole("USER", "ADMIN")

                    // Comments - only authenticated users and admins can create/update/delete
                    .requestMatchers(HttpMethod.POST, "/posts/*/comments").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/posts//comments/*").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/posts//comments/*").hasAnyRole("USER", "ADMIN")

                    // File upload
                    .requestMatchers(HttpMethod.POST, "/files/**").hasAnyRole("USER", "ADMIN")

                    // Admin endpoints
                    .requestMatchers("/admin/**").hasRole("ADMIN")

                    // User endpoints
                    .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                    .requestMatchers("/users/**").hasAnyRole("USER", "ADMIN")

                    .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}