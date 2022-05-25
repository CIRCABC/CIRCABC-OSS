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
package eu.cec.digit.circabc.service.bulk.indexes;

import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ValidateHelperImpl implements ValidateHelper {

    public void validate(
            final IndexHeaders index,
            final Map<IndexEntry, List<ValidationMessage>> indexValidationMessages) {
        // final List<IndexRecord> indexRecords = index.getIndexRecords();
        final List<IndexEntry> indexRecords = Collections.emptyList();
        List<HeaderValidator> headerValidators;
        for (final IndexEntry indexRecord : indexRecords) {
            headerValidators = index.getHeader(indexRecord.getHeaderName()).getHeaderValidators();
            validate(indexRecord, headerValidators, indexValidationMessages);
        }
    }

    private void validate(
            final IndexEntry indexRecord,
            final List<HeaderValidator> headerValidators,
            final Map<IndexEntry, List<ValidationMessage>> indexValidationMessages) {
        boolean validate;
        for (HeaderValidator headerValidator : headerValidators) {
            validate = headerValidator.validate(indexRecord);
            if (!validate) {
                if (indexValidationMessages.containsKey(indexRecord)) {
                    indexValidationMessages.get(indexRecord).add(headerValidator.getValidationMessage());
                } else {
                    final List<ValidationMessage> validationMessages = new ArrayList<>();
                    validationMessages.add(headerValidator.getValidationMessage());
                    indexValidationMessages.put(indexRecord, validationMessages);
                }
            }
        }
    }
}
