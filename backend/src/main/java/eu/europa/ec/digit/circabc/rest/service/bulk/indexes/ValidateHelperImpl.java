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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ValidateHelper}.
 *
 * <p>This helper drives the validation of bulk-import index records against the {@link
 * HeaderValidator}s configured for their corresponding headers. For each index record it resolves
 * the header definition (via {@link IndexHeaders#getHeader(String)}), retrieves that header's
 * validators, and applies every validator to the record. Any validation failure is recorded by
 * collecting the validator's {@link ValidationMessage} into the caller-supplied map, keyed by the
 * failing {@link IndexEntry}.
 */
public class ValidateHelperImpl implements ValidateHelper {

  /**
   * Validates every record of the given index and accumulates any validation failures.
   *
   * <p>For each index record, the header it belongs to is looked up in {@code index} and its
   * associated {@link HeaderValidator}s are applied to the record. Validation messages for records
   * that fail are added to {@code indexValidationMessages}.
   *
   * @param index the header definitions holding the {@link HeaderValidator}s to apply
   * @param indexValidationMessages a mutable map, keyed by {@link IndexEntry}, into which
   *     validation failure messages are collected; entries are added only for records that fail
   *     at least one validator
   */
  public void validate(
    final IndexHeaders index,
    final Map<IndexEntry, List<ValidationMessage>> indexValidationMessages
  ) {
    final List<IndexEntry> indexRecords = Collections.emptyList();
    List<HeaderValidator> headerValidators;
    for (final IndexEntry indexRecord : indexRecords) {
      headerValidators = index
        .getHeader(indexRecord.getHeaderName())
        .getHeaderValidators();
      validate(indexRecord, headerValidators, indexValidationMessages);
    }
  }

  private void validate(
    final IndexEntry indexRecord,
    final List<HeaderValidator> headerValidators,
    final Map<IndexEntry, List<ValidationMessage>> indexValidationMessages
  ) {
    boolean validate;
    for (HeaderValidator headerValidator : headerValidators) {
      validate = headerValidator.validate(indexRecord);
      if (!validate) {
        if (indexValidationMessages.containsKey(indexRecord)) {
          indexValidationMessages
            .get(indexRecord)
            .add(headerValidator.getValidationMessage());
        } else {
          final List<ValidationMessage> validationMessages = new ArrayList<>();
          validationMessages.add(headerValidator.getValidationMessage());
          indexValidationMessages.put(indexRecord, validationMessages);
        }
      }
    }
  }
}
