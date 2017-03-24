package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateConsentException extends RuntimeException {
    public DuplicateConsentException() {
    }

    public DuplicateConsentException(String message) {
        super(message);
    }

    public DuplicateConsentException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateConsentException(Throwable cause) {
        super(cause);
    }

    public DuplicateConsentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
