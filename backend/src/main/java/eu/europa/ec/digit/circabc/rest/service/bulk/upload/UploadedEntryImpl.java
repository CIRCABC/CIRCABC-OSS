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

import java.io.File;

/**
 * Default implementation of {@link UploadedEntry}.
 *
 * <p>Holds the state of a single file processed during a bulk upload operation: its name, its path
 * within the upload structure, the processing status and any associated remarks. This is a simple
 * mutable data holder used to report the per-file outcome of a bulk upload.
 */
public class UploadedEntryImpl implements UploadedEntry {

  /** The path of the uploaded file within the upload structure. */
  private String filePath;

  /** The name of the uploaded file. */
  private String fileName;

  /** Remarks describing the outcome of processing the file (e.g. an error or informational message). */
  private String remarks;

  /** The processing status of the uploaded file (e.g. success or failure). */
  private String status;

  /** Creates an empty entry with no file name or path set. */
  public UploadedEntryImpl() {}

  /**
   * Creates an entry for the given file.
   *
   * @param fileName the name of the uploaded file
   * @param filePath the path of the uploaded file within the upload structure
   */
  public UploadedEntryImpl(String fileName, String filePath) {
    this.fileName = fileName;
    this.filePath = filePath;
  }

  /**
   * {@inheritDoc}
   *
   * @return the file path, or {@code null} if not set
   */
  public String getFilePath() {
    return filePath;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The supplied path is normalised to use the current platform's file separator: any separator
   * character that does not match {@link File#separatorChar} is replaced accordingly before being
   * stored.
   *
   * @param filePath the file path to assign to this entry
   */
  public void setFilePath(final String filePath) {
    String name;
    if (File.separatorChar == '/') {
      name = filePath.replace('\\', File.separatorChar);
    } else {
      name = filePath.replace('/', File.separatorChar);
    }
    this.filePath = name;
  }

  /**
   * {@inheritDoc}
   *
   * @return the file name, or {@code null} if not set
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * {@inheritDoc}
   *
   * @param fileName the file name to assign to this entry
   */
  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  /**
   * {@inheritDoc}
   *
   * @return the remarks, or {@code null} if not set
   */
  public String getRemarks() {
    return remarks;
  }

  /**
   * {@inheritDoc}
   *
   * @param remarks the remarks to assign to this entry
   */
  public void setRemarks(final String remarks) {
    this.remarks = remarks;
  }

  /**
   * {@inheritDoc}
   *
   * @return the status, or {@code null} if not set
   */
  public String getStatus() {
    return status;
  }

  /**
   * {@inheritDoc}
   *
   * @param status the status to assign to this entry
   */
  public void setStatus(final String status) {
    this.status = status;
  }
}
