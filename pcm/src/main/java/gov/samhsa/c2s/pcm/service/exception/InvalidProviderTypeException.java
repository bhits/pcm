package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidProviderTypeException extends RuntimeException {
    public InvalidProviderTypeException() {
    }

    public InvalidProviderTypeException(String message) {
        super(message);
    }

    public InvalidProviderTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidProviderTypeException(Throwable cause) {
        super(cause);
    }

    public InvalidProviderTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
