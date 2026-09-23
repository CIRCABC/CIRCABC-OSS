/**
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
package eu.europa.ec.digit.circabc.rest.service.iam;

/**
 * Runtime exception raised when an interaction with the IAM (Identity and Access Management) web
 * service fails.
 *
 * <p>This unchecked exception is thrown by the IAM service layer to signal errors that occur while
 * calling the external IAM web service (for example, communication failures or unexpected
 * responses). Being a {@link RuntimeException}, it does not need to be declared or caught
 * explicitly.
 *
 * @author Slobodan Filipovic
 */
public class IamWSEception extends RuntimeException {

  /** Serialization version identifier for this exception class. */
  private static final long serialVersionUID = 6313277738682278125L;

  /**
   * Creates a new exception with the given detail message.
   *
   * @param message the detail message describing the IAM web service failure
   */
  public IamWSEception(String message) {
    super(message);
  }

  /**
   * Creates a new exception with the given detail message and underlying cause.
   *
   * @param message the detail message describing the IAM web service failure
   * @param cause the underlying cause of this exception
   */
  public IamWSEception(String message, Throwable cause) {
    super(message, cause);
  }
}
