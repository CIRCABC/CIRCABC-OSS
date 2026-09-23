/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.auto.upload;

/**
 * Enumerates the possible outcomes of an auto-upload job execution.
 *
 * <p>Each constant carries a numeric {@code result} code that summarizes the status of a run:
 * positive for success, zero when there was nothing to process, and negative for error conditions.
 * These codes are typically used for logging, reporting or signalling the result of the auto-upload
 * process back to callers.
 *
 * @author beaurpi
 */
public enum AutoUploadJobResult {
  /** The job completed successfully and processed content. */
  JOB_OK(1),
  /** The job ran successfully but found nothing to process. */
  JOB_NOTHING_TO_DO(0),
  /** The job failed due to a generic error during execution. */
  JOB_ERROR(-1),
  /** The job failed because of a problem communicating with the remote FTP server. */
  JOB_REMOTE_FTP_PROBLEM(-2);

  /** The numeric status code associated with this job result. */
  private final Integer result;

  /**
   * Creates a job result constant with its associated numeric status code.
   *
   * @param result the numeric status code for this outcome
   */
  AutoUploadJobResult(Integer result) {
    this.result = result;
  }

  /**
   * Returns the numeric status code associated with this job result.
   *
   * @return the numeric status code for this outcome
   */
  public Integer getResult() {
    return result;
  }
}
