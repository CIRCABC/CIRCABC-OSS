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
 * Runtime exception raised when an operation involving a user profile cannot be completed.
 *
 * <p>The exception carries the name of the profile that caused the failure together with a
 * human-readable explanation of what went wrong. Both pieces of information are combined into the
 * exception message (formatted as {@code "<profileName>: <explanation>"}) and are also individually
 * accessible through {@link #getProfileName()} and {@link #getExplanation()}.
 *
 * @author Clinckart Stephane
 */
public class ProfileException extends RuntimeException {

  /** Name of the profile associated with the failure. */
  private final String profileName;

  /** Human-readable description of the reason the profile operation failed. */
  private final String explanation;

  /**
   * Creates a new {@code ProfileException}.
   *
   * @param profileName the name of the profile that caused the failure
   * @param explain a human-readable explanation of what went wrong
   */
  public ProfileException(String profileName, String explain) {
    super(profileName + ": " + explain);
    this.profileName = profileName;
    this.explanation = explain;
  }

  /**
   * Returns the name of the profile associated with this failure.
   *
   * @return the profile name
   */
  public String getProfileName() {
    return profileName;
  }

  /**
   * Returns the human-readable explanation of why the profile operation failed.
   *
   * @return the explanation message
   */
  public String getExplanation() {
    return explanation;
  }
}
