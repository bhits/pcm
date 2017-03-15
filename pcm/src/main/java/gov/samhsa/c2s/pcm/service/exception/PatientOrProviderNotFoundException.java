package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PatientOrProviderNotFoundException extends RuntimeException {
    public PatientOrProviderNotFoundException() {
    }

    public PatientOrProviderNotFoundException(String message) {
        super(message);
    }

    public PatientOrProviderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PatientOrProviderNotFoundException(Throwable cause) {
        super(cause);
    }

    public PatientOrProviderNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
