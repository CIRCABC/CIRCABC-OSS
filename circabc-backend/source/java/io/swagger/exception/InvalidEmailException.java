package io.swagger.exception;

/**
 * @author beaurpi
 */
public class InvalidEmailException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public InvalidEmailException(String message) {
        super(message);
    }
}
