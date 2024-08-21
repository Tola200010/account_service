package account.config.listeners;

import account.config.GeneralApplicationEvent;
import account.entities.ApplicationEvent;
import account.repository.ApplicationEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class GeneralApplicationListener implements ApplicationListener<GeneralApplicationEvent> {
    private final ApplicationEventRepository applicationEventRepository;

    public GeneralApplicationListener(ApplicationEventRepository applicationEventRepository) {
        this.applicationEventRepository = applicationEventRepository;
    }

    @Override
    public void onApplicationEvent(GeneralApplicationEvent event) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        ApplicationEvent applicationEvent = new ApplicationEvent();
        applicationEvent.setCreatedDate(new Date());
        applicationEvent.setAction(event.getData().getAction().name());
        applicationEvent.setSubject(event.getData().getSubject());
        applicationEvent.setPath(request.getRequestURI());
        applicationEvent.setObject(event.getData().getObject() == null ? request.getRequestURI() : event.getData().getObject());
        applicationEventRepository.save(applicationEvent);
    }
}
