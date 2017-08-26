package gov.samhsa.c2s.pcm.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PdfGenerateException extends RuntimeException {

	public PdfGenerateException() {
	}

	public PdfGenerateException(String message) {
		super(message);
	}

	public PdfGenerateException(String message, Throwable cause) {
		super(message, cause);
	}

	public PdfGenerateException(Throwable cause) {
		super(cause);
	}

	public PdfGenerateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
