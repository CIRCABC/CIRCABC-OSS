package io.swagger.exception;

/**
 * @author beaurpi
 */
public class EmptyQueryStringException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public EmptyQueryStringException(String text) {
        super(text);
    }
}
