package account.dto;

import account.utiles.ApplicationEventName;

public class GeneralApplicationEventData {
    private final ApplicationEventName action;
    private final String subject;
    private final String object;
    public GeneralApplicationEventData(ApplicationEventName action, String subject, String object) {
        this.action = action;
        this.subject = subject;
        this.object = object;
    }
    public static GeneralApplicationEventData sendAnonymousData(ApplicationEventName action,String object){
        return new GeneralApplicationEventData(
                action,
                "Anonymous",
                object
        );
    }
    public ApplicationEventName getAction() {
        return action;
    }

    public String getSubject() {
        return subject;
    }

    public String getObject() {
        return object;
    }

}
