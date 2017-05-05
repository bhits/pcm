package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason ="Multiple Patients found for given MRN" )
public class MultiplePatientsFoundException extends RuntimeException {
    public MultiplePatientsFoundException() {
    }

    public MultiplePatientsFoundException(String message) {
        super(message);
    }

    public MultiplePatientsFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultiplePatientsFoundException(Throwable cause) {
        super(cause);
    }

    public MultiplePatientsFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
