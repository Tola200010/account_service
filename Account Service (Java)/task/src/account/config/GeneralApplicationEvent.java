package account.config;


import account.dto.GeneralApplicationEventData;
import org.springframework.context.ApplicationEvent;

public class GeneralApplicationEvent extends ApplicationEvent {
    private final GeneralApplicationEventData data;
    public GeneralApplicationEvent(Object source, GeneralApplicationEventData data) {
        super(source);
        this.data = data;
    }

    public GeneralApplicationEventData getData() {
        return data;
    }
}
