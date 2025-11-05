package com.selimhorri.app.exception.wrapper;

public class InvalidPaymentStatusException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public InvalidPaymentStatusException() {
		super();
	}
	
	public InvalidPaymentStatusException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvalidPaymentStatusException(String message) {
		super(message);
	}
	
	public InvalidPaymentStatusException(Throwable cause) {
		super(cause);
	}
}
