package account.config;

import account.dto.GeneralApplicationEventData;
import account.utiles.ApplicationEventName;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.security.Principal;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ApplicationEventPublisher applicationEventPublisher;

    public CustomAccessDeniedHandler(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Principal userPrincipal = request.getUserPrincipal();
        GeneralApplicationEventData generalApplicationEventData =
                new GeneralApplicationEventData(
                        ApplicationEventName.ACCESS_DENIED,
                        userPrincipal.getName().toLowerCase(),
                        request.getRequestURI()
                );
        GeneralApplicationEvent generalApplicationEvent = new GeneralApplicationEvent(this,generalApplicationEventData);
        applicationEventPublisher.publishEvent(generalApplicationEvent);
        response.sendError(HttpStatus.FORBIDDEN.value(),"Access Denied!");
    }
}
