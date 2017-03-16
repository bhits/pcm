package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPurposeException extends RuntimeException {
    public InvalidPurposeException() {
    }

    public InvalidPurposeException(String message) {
        super(message);
    }

    public InvalidPurposeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPurposeException(Throwable cause) {
        super(cause);
    }

    public InvalidPurposeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
