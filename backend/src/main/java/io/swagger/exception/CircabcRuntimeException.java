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

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Unchecked runtime exception used throughout the CIRCABC backend to signal
 * application-specific error conditions.
 *
 * <p>It extends Alfresco's {@link AlfrescoRuntimeException} so that CIRCABC
 * errors integrate with the platform's error handling and message
 * (I18N) resolution: the message identifier passed to the constructors may be
 * either a literal message or a resource bundle key that Alfresco resolves.
 * Being unchecked, it can be thrown from service and REST layers without being
 * declared in method signatures.
 */
public class CircabcRuntimeException extends AlfrescoRuntimeException {

  /**
   * Creates a new exception with the given message.
   *
   * @param msgId the message identifier; either a literal message or an
   *              Alfresco message bundle key to be resolved
   */
  public CircabcRuntimeException(String msgId) {
    super(msgId);
  }

  /**
   * Creates a new exception with the given message and underlying cause.
   *
   * @param msgId the message identifier; either a literal message or an
   *              Alfresco message bundle key to be resolved
   * @param cause the underlying throwable that triggered this exception
   */
  public CircabcRuntimeException(String msgId, Throwable cause) {
    super(msgId, cause);
  }
}
