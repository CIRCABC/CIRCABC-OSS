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
package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.util.List;
import java.util.Map;

/**
 * Contract for validating the entries of a bulk import index against the validation rules declared
 * by their column headers.
 *
 * <p>Implementations iterate over the index records described by an {@link IndexHeaders} definition
 * and apply, for each record, the {@link HeaderValidator}s configured on its matching {@link
 * IndexHeader}. Any validation failures are collected as {@link ValidationMessage}s and grouped by
 * the {@link IndexEntry} that produced them, so callers can report all problems detected during the
 * bulk import.
 */
public interface ValidateHelper {
  /**
   * Validates the entries of the given index and accumulates any validation failures.
   *
   * <p>For each index entry, the validators associated with its header are executed. When a
   * validator fails, its {@link ValidationMessage} is appended to the list of messages mapped to the
   * offending {@link IndexEntry} in {@code indexValidationMessages}.
   *
   * @param index the index header definitions describing the columns and their validators; must not
   *     be {@code null}
   * @param indexValidationMessages a mutable map, keyed by index entry, into which validation
   *     failure messages are collected; must not be {@code null} and is updated in place
   */
  void validate(
    final IndexHeaders index,
    final Map<IndexEntry, List<ValidationMessage>> indexValidationMessages
  );
}
