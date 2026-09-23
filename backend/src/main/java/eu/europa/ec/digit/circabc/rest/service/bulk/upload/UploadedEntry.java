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
package eu.europa.ec.digit.circabc.rest.service.bulk.upload;

/**
 * Represents a single file entry processed during a bulk upload operation.
 *
 * <p>Each entry captures the location and name of the uploaded file together with the outcome of
 * its processing (a status and any associated remarks). Implementations are typically used to
 * report the per-file result of a bulk upload back to the caller.
 */
public interface UploadedEntry {
  /**
   * Returns the path of the uploaded file within the upload structure.
   *
   * @return the file path, or {@code null} if not set
   */
  String getFilePath();

  /**
   * Sets the path of the uploaded file within the upload structure.
   *
   * @param filePath the file path to assign to this entry
   */
  void setFilePath(final String filePath);

  /**
   * Returns the name of the uploaded file.
   *
   * @return the file name, or {@code null} if not set
   */
  String getFileName();

  /**
   * Sets the name of the uploaded file.
   *
   * @param fileName the file name to assign to this entry
   */
  void setFileName(final String fileName);

  /**
   * Returns the processing status of this uploaded file (for example, whether the upload
   * succeeded or failed).
   *
   * @return the status, or {@code null} if not set
   */
  String getStatus();

  /**
   * Sets the processing status of this uploaded file.
   *
   * @param status the status to assign to this entry
   */
  void setStatus(final String status);

  /**
   * Returns any remarks describing the outcome of processing this file, such as an error or
   * informational message.
   *
   * @return the remarks, or {@code null} if not set
   */
  String getRemarks();

  /**
   * Sets the remarks describing the outcome of processing this file.
   *
   * @param remarks the remarks to assign to this entry
   */
  void setRemarks(final String remarks);
}
