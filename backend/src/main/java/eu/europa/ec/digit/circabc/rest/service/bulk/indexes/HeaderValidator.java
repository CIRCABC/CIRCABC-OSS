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

/**
 * Defines a validation rule applied to the header (metadata/definition row) of a bulk import index.
 *
 * <p>Implementations inspect a single {@link IndexEntry} that represents a header record and decide
 * whether it satisfies the rule. When validation fails, {@link #getValidationMessage()} exposes a
 * {@link ValidationMessage} describing the problem so it can be reported back to the caller.
 */
public interface HeaderValidator {
  /**
   * Validates the given header index record against this rule.
   *
   * @param indexRecord the header {@link IndexEntry} to validate
   * @return {@code true} if the record satisfies the rule, {@code false} otherwise
   */
  boolean validate(final IndexEntry indexRecord);

  /**
   * Returns the validation message produced by the most recent {@link #validate(IndexEntry)} call.
   *
   * @return the {@link ValidationMessage} describing the validation outcome, or the message
   *     associated with a failed validation
   */
  ValidationMessage getValidationMessage();
}
