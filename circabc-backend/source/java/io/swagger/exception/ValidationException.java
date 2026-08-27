package io.swagger.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when FAQ import validation fails.
 * Contains a list of validation errors for detailed error reporting.
 */
public class ValidationException extends SwaggerRuntimeException {

  private final List<String> validationErrors;

  public ValidationException(String message) {
    super(message);
    this.validationErrors = new ArrayList<>();
  }

  public ValidationException(String message, List<String> validationErrors) {
    super(message);
    this.validationErrors = new ArrayList<>(validationErrors);
  }

  public List<String> getValidationErrors() {
    return new ArrayList<>(validationErrors);
  }

  public void addValidationError(String error) {
    this.validationErrors.add(error);
  }
}
