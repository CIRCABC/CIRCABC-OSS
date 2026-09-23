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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Validates the issue date of a bulk upload {@link IndexRecord}.
 *
 * <p>During bulk import index validation this validator checks that, when an issue date is present
 * on a record, it conforms to the expected {@code dd/MM/yyyy} format. If the value cannot be parsed,
 * a {@link ErrorType#Warning warning} {@link ValidationMessage} is added for the offending row; a
 * missing or empty issue date is considered valid and produces no message.
 */
public class IssueDateValidator extends AbstractIndexValidator {

  /** Date format ({@code dd/MM/yyyy}) that an issue date value is expected to comply with. */
  private DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

  /** I18N message key used to build the warning shown when the issue date is not parseable. */
  private static final String ERROR_DESCRIPTION =
    "bulk_upload_issue_date_error";

  /**
   * Creates an issue date validator bound to the given Alfresco service registry.
   *
   * @param serviceRegistry the Alfresco {@link ServiceRegistry} providing access to the platform
   *     services required for validation
   */
  public IssueDateValidator(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
  }

  /**
   * Validates the issue date of the given record.
   *
   * <p>If the record carries a non-empty issue date that cannot be parsed using the expected
   * {@code dd/MM/yyyy} format, a warning {@link ValidationMessage} referencing the record's row
   * number and name is appended to {@code messages}. Records with no issue date are left untouched.
   *
   * @param indexRecord the bulk upload record whose issue date is validated
   * @param messages the collection to which validation messages are added; a warning is appended
   *     when the issue date is present but malformed
   */
  public void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    if (
      indexRecord.getIssueDate() != null &&
      !indexRecord.getIssueDate().isEmpty()
    ) {
      try {
        @SuppressWarnings("unused")
        final Date parsed = sdf.parse(indexRecord.getIssueDate());
      } catch (final ParseException pe) {
        final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION);
        final ValidationMessage validationMessage = new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          indexRecord.getName(),
          errorDescription,
          ErrorType.Warning
        );
        messages.add(validationMessage);
      }
    }
  }
}
