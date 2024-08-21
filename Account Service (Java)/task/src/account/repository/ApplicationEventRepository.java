package account.repository;

import account.entities.ApplicationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationEventRepository extends JpaRepository<ApplicationEvent,Integer> {
}
