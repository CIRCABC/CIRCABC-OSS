package io.swagger.exception;

/**
 * @author beaurpi
 */
public class NonExistingNodeException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NonExistingNodeException(String message) {
        super(message);
    }
}
