package account.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class SecurityConfig {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    public SecurityConfig(ApplicationEventPublisher applicationEventPublisher, RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(hbc-> hbc.authenticationEntryPoint(restAuthenticationEntryPoint))
                .exceptionHandling(ex -> {
                    ex.accessDeniedHandler(accessDeniedHandler());
                }) // Handle auth errors
                .csrf(AbstractHttpConfigurer::disable) // For Postman
                .headers(headers -> headers.frameOptions().disable()) // For the H2 console
                .authorizeHttpRequests(auth -> auth  // manage access
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup","/actuator/shutdown").permitAll()
                        .requestMatchers("/api/empl/payment").hasAnyRole("USER","ACCOUNTANT")
                        .requestMatchers("/api/acct/payments").hasRole("ACCOUNTANT")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMINISTRATOR")
                        .requestMatchers("/api/security/**").hasRole("AUDITOR")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new CustomAccessDeniedHandler(applicationEventPublisher);
    }

}
