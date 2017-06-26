package gov.samhsa.c2s.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ShareSensitivityCategoriesException extends RuntimeException {

    /**
     * Instantiates a new DocumentSegmentation exception.
     */
    public ShareSensitivityCategoriesException() {
        super();
    }

    /**
     * Instantiates a new DocumentSegmentation exception.
     *
     * @param arg0 the arg0
     * @param arg1 the arg1
     */
    public ShareSensitivityCategoriesException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Instantiates a new DocumentSegmentation exception.
     *
     * @param arg0 the arg0
     */
    public ShareSensitivityCategoriesException(String arg0) {
        super(arg0);
    }

    /**
     * Instantiates a new DocumentSegmentation exception.
     *
     * @param arg0 the arg0
     */
    public ShareSensitivityCategoriesException(Throwable arg0) {
        super(arg0);
    }
}
