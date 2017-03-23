package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PatientProviderNotFoundException extends RuntimeException {
    public PatientProviderNotFoundException() {
    }

    public PatientProviderNotFoundException(String message) {
        super(message);
    }

    public PatientProviderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PatientProviderNotFoundException(Throwable cause) {
        super(cause);
    }

    public PatientProviderNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
