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
package eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.util.List;

/**
 * Contract for validators that check a single {@link IndexRecord} parsed from a bulk import index
 * file.
 *
 * <p>Implementations inspect one aspect of an index record (for example a mandatory field, a value
 * format or a cross-reference) and report any problems by appending {@link ValidationMessage}
 * instances to the shared message list. This allows several validators to be chained together while
 * accumulating all validation feedback for a record.
 */
public interface IndexValidator {
  /**
   * Validates the given index record and records any issues found.
   *
   * <p>Validation errors or warnings are not thrown but appended to the supplied {@code messages}
   * list, so that all validators contributing to a record can collect their feedback together.
   *
   * @param indexRecord the bulk import index record to validate
   * @param messages the mutable list to which validation messages are appended; implementations add
   *     to this list rather than replacing it
   */
  void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  );
}
