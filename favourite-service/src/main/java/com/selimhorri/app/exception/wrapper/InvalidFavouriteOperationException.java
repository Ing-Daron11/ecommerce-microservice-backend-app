package com.selimhorri.app.exception.wrapper;

public class InvalidFavouriteOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidFavouriteOperationException() {
        super();
    }

    public InvalidFavouriteOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFavouriteOperationException(String message) {
        super(message);
    }

    public InvalidFavouriteOperationException(Throwable cause) {
        super(cause);
    }

}
