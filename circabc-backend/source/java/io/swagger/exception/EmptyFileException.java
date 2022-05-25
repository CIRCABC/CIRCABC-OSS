/**
 *
 */
package io.swagger.exception;

/** @author beaurpi */
public class EmptyFileException extends Exception {
    /** */
    private static final long serialVersionUID = 1L;

    public EmptyFileException(String text) {
        super(text);
    }
}
