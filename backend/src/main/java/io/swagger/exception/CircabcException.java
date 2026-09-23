/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package io.swagger.exception;

/**
 * Base checked exception for the CIRCABC domain layer.
 *
 * <p>Thrown by business/service logic to signal an application-level error condition. As a checked
 * exception it forces callers to explicitly handle or propagate CIRCABC-specific failures. The
 * message identifier passed to the constructors typically carries a human-readable message or a
 * resource/message key describing the cause of the failure.
 */
public class CircabcException extends Exception {

  /**
   * Creates a new exception with the given message identifier.
   *
   * @param msgId the detail message or message key describing the error condition
   */
  public CircabcException(String msgId) {
    super(msgId);
  }

  /**
   * Creates a new exception with the given message identifier and underlying cause.
   *
   * @param msgId the detail message or message key describing the error condition
   * @param cause the underlying throwable that triggered this exception
   */
  public CircabcException(String msgId, Throwable cause) {
    super(msgId, cause);
  }
}
