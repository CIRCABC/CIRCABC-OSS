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
package eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message;

import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;

/**
 * Default implementation of {@link ValidationMessage}.
 *
 * <p>Represents a single validation feedback entry produced while validating a bulk import (e.g. an
 * index file used by the bulk upload feature). Each message ties an {@link ErrorType} to the
 * specific file and row that triggered it, together with a human-readable description. Instances are
 * immutable value objects: all state is provided at construction time and only exposed through
 * getters.
 */
public class ValidationMessageImpl implements ValidationMessage {

  /** One-based number of the row within the source file that this message refers to. */
  private int rowNumber;

  /** Name of the file being validated to which this message applies. */
  private String fileName;

  /** Human-readable description explaining the validation issue. */
  private String errorDescription;

  /** Category of the validation issue, used to classify the message. */
  private ErrorType errorType;

  /**
   * Creates a validation message describing a single validation issue.
   *
   * @param rowNumber the row number within the source file that the issue refers to
   * @param fileName the name of the file being validated
   * @param errorDescription a human-readable description of the validation issue
   * @param errorType the category of the validation issue
   */
  public ValidationMessageImpl(
    final int rowNumber,
    final String fileName,
    final String errorDescription,
    final ErrorType errorType
  ) {
    this.rowNumber = rowNumber;
    this.fileName = fileName;
    this.errorDescription = errorDescription;
    this.errorType = errorType;
  }

  /**
   * Returns the row number within the source file that this message refers to.
   *
   * @return the row number
   */
  public int getRowNumber() {
    return rowNumber;
  }

  /**
   * Returns the name of the file being validated to which this message applies.
   *
   * @return the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Returns the human-readable description of the validation issue.
   *
   * @return the error description
   */
  public String getErrorDescription() {
    return errorDescription;
  }

  /**
   * Returns the category of the validation issue.
   *
   * @return the error type
   */
  public ErrorType getErrorType() {
    return errorType;
  }
}
