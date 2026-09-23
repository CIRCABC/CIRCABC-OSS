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
package io.swagger.exception;

/**
 * Checked exception thrown when a bulk user import file does not conform to the expected format.
 *
 * <p>This exception signals that the uploaded Bulk User Import file is structurally invalid, for
 * example when the expected column labels are missing or do not match the required layout. It is
 * used by the bulk import processing logic to abort the operation and report a meaningful error to
 * the caller.
 *
 * @author beaurpi
 */
public class InvalidBulkImportFileFormatException extends Exception {

  /** Serialization version identifier for this exception class. */
  private static final long serialVersionUID = 8673796840049092712L;

  /**
   * Creates the exception with a default message indicating that the bulk import file does not
   * match the required format and prompting verification of the column labels.
   */
  public InvalidBulkImportFileFormatException() {
    super(
      "The Bulk User Import file does not match required format. Please verify column labels."
    );
  }

  /**
   * Creates the exception with a custom detail message.
   *
   * @param message the detail message describing why the bulk import file format is invalid
   */
  public InvalidBulkImportFileFormatException(String message) {
    super(message);
  }
}
