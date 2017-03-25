package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ConsentNotFoundException extends RuntimeException {
    public ConsentNotFoundException() {
    }

    public ConsentNotFoundException(String message) {
        super(message);
    }

    public ConsentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsentNotFoundException(Throwable cause) {
        super(cause);
    }

    public ConsentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
