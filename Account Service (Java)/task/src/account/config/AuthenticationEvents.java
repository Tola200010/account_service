package account.config;

import account.dto.GeneralApplicationEventData;
import account.repository.AppUserRepository;
import account.services.UserService;
import account.utiles.ApplicationEventName;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountLockedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Component
public class AuthenticationEvents {
    private final ApplicationEventPublisher publisher;
    private final UserService service;
    public AuthenticationEvents(ApplicationEventPublisher publisher, UserService service) {
        this.publisher = publisher;
        this.service = service;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent successEvent){
        String email = successEvent.getAuthentication().getName();
        service.resetFailedAttempts(email);
        log(successEvent);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failureEvent) {
        log(failureEvent);
    }

    private void log(AbstractAuthenticationEvent event) {
        LocalDateTime localDateTime = toLocalDateTime(event.getTimestamp());
        if (event instanceof AbstractAuthenticationFailureEvent) {
            System.out.println("Failed authentication attempt at: " + localDateTime);
        }
        else {
            System.out.println("Successful authentication attempt at: " + localDateTime);
        }
        System.out.println("User: " + event.getAuthentication().getName());
        System.out.println("Authorities: " + event.getAuthentication().getAuthorities());
        System.out.println("Details: " + event.getAuthentication().getDetails());
        System.out.println("Credentials: " + event.getAuthentication().getCredentials());
        System.out.println("Principal: " + event.getAuthentication().getPrincipal());
        System.out.println("Is authenticated: " + event.getAuthentication().isAuthenticated());
        if (event instanceof AbstractAuthenticationFailureEvent failureEvent) {
            System.out.println("Exception: " + failureEvent.getException());
        }
    }

    private LocalDateTime toLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp / 1000), TimeZone.getDefault().toZoneId()
        );
    }
}
