package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ConsentAttestationTermNotFound extends RuntimeException {
    public ConsentAttestationTermNotFound() {
    }

    public ConsentAttestationTermNotFound(String message) {
        super(message);
    }

    public ConsentAttestationTermNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsentAttestationTermNotFound(Throwable cause) {
        super(cause);
    }

    public ConsentAttestationTermNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
