package gov.samhsa.c2s.pcm.service.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSignDateException extends RuntimeException{
    public InvalidSignDateException(){
    }
    public InvalidSignDateException(String message) {
        super(message);
    }

    public InvalidSignDateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSignDateException(Throwable cause) {
        super(cause);
    }

    public InvalidSignDateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
