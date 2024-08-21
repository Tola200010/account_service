package account.config;

import account.dto.GeneralApplicationEventData;
import account.entities.AppUser;
import account.repository.AppUserRepository;
import account.services.UserService;
import account.utiles.ApplicationEventName;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class CustomLoginFailureHandler implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final AppUserRepository appUserRepository;
    public CustomLoginFailureHandler(UserService userService, ApplicationEventPublisher publisher, AppUserRepository appUserRepository) {
        this.userService = userService;
        this.publisher = publisher;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String email = event.getAuthentication().getName();
        GeneralApplicationEventData generalApplicationEventData = new GeneralApplicationEventData(
                ApplicationEventName.LOGIN_FAILED,
                email.toLowerCase(),
                null
        );
        GeneralApplicationEvent generalApplicationEvent = new GeneralApplicationEvent(this,generalApplicationEventData);
        publisher.publishEvent(generalApplicationEvent);
        AppUser userLogin = appUserRepository.findByEmail(email.toLowerCase()).orElse(null);
        if(userLogin == null) return;
        if (userLogin.isAccountNonLocked()) {
            if (userLogin.getFailedAttempt() < UserService.MAX_FAILED_ATTEMPTS) {
                userService.increaseFailedAttempts(userLogin);
            } else {
                generalApplicationEventData = new GeneralApplicationEventData(
                        ApplicationEventName.BRUTE_FORCE,
                        email.toLowerCase(),
                        null
                );
                 generalApplicationEvent = new GeneralApplicationEvent(this,generalApplicationEventData);
                publisher.publishEvent(generalApplicationEvent);
                userService.lock(userLogin);
                generalApplicationEventData = new GeneralApplicationEventData(
                        ApplicationEventName.LOCK_USER,
                        email.toLowerCase(),
                        "Lock user "+ email
                );
                generalApplicationEvent = new GeneralApplicationEvent(this,generalApplicationEventData);
                publisher.publishEvent(generalApplicationEvent);
            }
        }
    }
}
