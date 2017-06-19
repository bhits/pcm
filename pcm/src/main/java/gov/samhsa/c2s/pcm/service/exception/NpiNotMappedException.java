package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason ="Patient Not Found for Given MRN")
public class NpiNotMappedException extends RuntimeException {
    public NpiNotMappedException() {
    }

    public NpiNotMappedException(String message) {
        super(message);
    }

    public NpiNotMappedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NpiNotMappedException(Throwable cause) {
        super(cause);
    }

    public NpiNotMappedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
