package io.swagger.exception;

public class SwaggerRuntimeException extends RuntimeException {

    public SwaggerRuntimeException(String s) {
        super(s);
    }

    public SwaggerRuntimeException(Exception e) {
        super(e);
    }

    public SwaggerRuntimeException(String s, Exception e) {
        super(s, e);
    }
}
