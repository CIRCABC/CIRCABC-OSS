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
 * Represents a single validation message produced while validating a bulk import index file.
 *
 * <p>Each message describes one issue detected during the parsing or validation of a bulk index
 * entry, tying the problem to a specific row and file and classifying its severity. Implementations
 * expose the location of the issue (row number and file name), a human-readable description and the
 * {@link ErrorType} severity so that callers can decide whether the problem is blocking (fatal) or
 * merely advisory (warning).
 */
public interface ValidationMessage {
  /**
   * Returns the row number, within the processed index file, where the issue was detected.
   *
   * @return the one-based (or implementation-defined) row number the message refers to
   */
  int getRowNumber();

  /**
   * Returns the name of the file associated with this validation issue.
   *
   * @return the name of the file the message refers to
   */
  String getFileName();

  /**
   * Returns a human-readable description of the validation issue.
   *
   * @return the textual description of the detected problem
   */
  String getErrorDescription();

  /**
   * Returns the severity classification of this validation issue.
   *
   * @return the {@link ErrorType} indicating whether the issue is fatal or a warning
   */
  ErrorType getErrorType();
}
