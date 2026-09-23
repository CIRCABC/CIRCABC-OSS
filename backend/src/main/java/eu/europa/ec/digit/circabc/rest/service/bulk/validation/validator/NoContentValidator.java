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
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessageImpl;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Bulk-import index validator that checks the optional "no content" flag of an index record.
 *
 * <p>During a bulk upload each row of the index describes a node to be created. The {@code
 * noContent} column indicates whether the row represents a metadata-only entry (no binary content).
 * The only accepted values are {@code "Y"} and {@code "N"}. This validator ensures that, when the
 * flag is supplied, it holds one of those two values; an empty or absent value is considered valid
 * and left untouched.
 *
 * <p>When an unexpected value is found a {@link ErrorType#Fatal fatal} {@link ValidationMessage} is
 * appended to the collected messages, using the localised description keyed by {@code
 * bulk_upload_no_content}.
 */
public class NoContentValidator extends AbstractIndexValidator {

  /** I18N message key used to describe the error raised for an invalid "no content" value. */
  private static final String ERROR_DESCRIPTION = "bulk_upload_no_content";

  /**
   * Creates a validator bound to the given Alfresco service registry.
   *
   * @param serviceRegistry the Alfresco {@link ServiceRegistry} providing access to the platform
   *     services available to the validator
   */
  public NoContentValidator(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
  }

  /**
   * Validates the "no content" flag of the supplied index record.
   *
   * <p>If the flag is present and non-empty but is neither {@code "Y"} nor {@code "N"}, a {@link
   * ErrorType#Fatal fatal} {@link ValidationMessage} carrying the record's row number, name and a
   * localised error description is added to {@code messages}. A {@code null} or empty flag is
   * treated as valid and produces no message.
   *
   * @param indexRecord the index record whose {@code noContent} value is checked
   * @param messages the mutable list of validation messages to which any detected error is appended
   */
  public void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    final String noContent = indexRecord.getNoContent();
    if (
      noContent != null &&
      !noContent.isEmpty() &&
      !(noContent.equals("Y") || noContent.equals("N"))
    ) {
      final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION);
      final ValidationMessage validationMessage = new ValidationMessageImpl(
        indexRecord.getRowNumber(),
        indexRecord.getName(),
        errorDescription,
        ErrorType.Fatal
      );
      messages.add(validationMessage);
    }
  }
}
