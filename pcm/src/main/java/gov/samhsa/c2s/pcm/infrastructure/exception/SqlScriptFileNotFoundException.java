package gov.samhsa.c2s.pcm.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SqlScriptFileNotFoundException extends RuntimeException {

    public SqlScriptFileNotFoundException() {
    }

    public SqlScriptFileNotFoundException(String message) {
        super(message);
    }

    public SqlScriptFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlScriptFileNotFoundException(Throwable cause) {
        super(cause);
    }

    public SqlScriptFileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
