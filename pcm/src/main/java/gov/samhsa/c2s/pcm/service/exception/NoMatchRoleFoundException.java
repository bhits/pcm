package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class NoMatchRoleFoundException extends RuntimeException {
    public NoMatchRoleFoundException() {
    }

    public NoMatchRoleFoundException(String message) {
        super(message);
    }

    public NoMatchRoleFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMatchRoleFoundException(Throwable cause) {
        super(cause);
    }

    public NoMatchRoleFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
