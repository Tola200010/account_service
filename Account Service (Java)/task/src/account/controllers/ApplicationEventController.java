package account.controllers;

import account.entities.ApplicationEvent;
import account.repository.ApplicationEventRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApplicationEventController {
    private final ApplicationEventRepository applicationEventRepository;

    public ApplicationEventController(ApplicationEventRepository applicationEventRepository) {
        this.applicationEventRepository = applicationEventRepository;
    }
    @GetMapping("/security/events/")
    public ResponseEntity<List<ApplicationEvent>> getAllEventsLog(){
        List<ApplicationEvent> applicationEventsList = applicationEventRepository.findAll(Sort.by("id"));
        return new ResponseEntity<>(applicationEventsList, HttpStatus.OK);
    }
}
