package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code=HttpStatus.NOT_FOUND, reason="Patient Not found in given  FHIR Server")
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
