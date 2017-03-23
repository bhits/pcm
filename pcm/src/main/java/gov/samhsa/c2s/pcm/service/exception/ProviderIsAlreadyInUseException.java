package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProviderIsAlreadyInUseException extends RuntimeException {
    public ProviderIsAlreadyInUseException() {
    }

    public ProviderIsAlreadyInUseException(String message) {
        super(message);
    }

    public ProviderIsAlreadyInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProviderIsAlreadyInUseException(Throwable cause) {
        super(cause);
    }

    public ProviderIsAlreadyInUseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
