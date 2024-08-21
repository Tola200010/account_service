package account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class LockedAccountException extends RuntimeException {
    private final String message;

    public LockedAccountException(String message) {
        super(message);
        this.message = message;
    }
}
