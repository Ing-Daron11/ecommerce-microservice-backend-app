package com.selimhorri.app.exception.wrapper;

public class InvalidFavouriteDataException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public InvalidFavouriteDataException() {
		super();
	}
	
	public InvalidFavouriteDataException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvalidFavouriteDataException(String message) {
		super(message);
	}
	
	public InvalidFavouriteDataException(Throwable cause) {
		super(cause);
	}
	
}
