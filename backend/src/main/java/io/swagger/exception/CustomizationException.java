package io.swagger.exception;

/*
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */

/**
 * Checked exception raised when a CIRCABC customization operation fails.
 *
 * <p>This exception signals errors that occur while applying or processing customizations (for
 * example, look-and-feel, branding or configuration overrides). Being a checked exception, callers
 * are expected to handle or propagate it explicitly.
 *
 * @author Yanick Pignot
 */
public class CustomizationException extends Exception {

  /** Serialization version identifier for this exception type. */
  private static final long serialVersionUID = 733672122793854579L;

  /**
   * Creates a new exception with the given detail message.
   *
   * @param message the detail message describing the customization failure
   */
  public CustomizationException(String message) {
    super(message);
  }

  /**
   * Creates a new exception wrapping the underlying cause.
   *
   * @param cause the underlying cause of the customization failure
   */
  public CustomizationException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new exception with the given detail message and underlying cause.
   *
   * @param message the detail message describing the customization failure
   * @param cause the underlying cause of the customization failure
   */
  public CustomizationException(String message, Throwable cause) {
    super(message, cause);
  }
}
