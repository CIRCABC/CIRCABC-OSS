/**
 *
 */
package io.swagger.exception;

/** @author beaurpi */
public class MaxFileSizeException extends Exception {
    /** */
    private static final long serialVersionUID = 1L;

    public MaxFileSizeException(String text) {
        super(text);
    }
}
