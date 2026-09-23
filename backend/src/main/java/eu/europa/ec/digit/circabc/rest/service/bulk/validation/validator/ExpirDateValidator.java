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
 * Index validator that checks the expiration date of a bulk-upload {@link IndexRecord}.
 *
 * <p>During a bulk upload each row of the index file is validated. This validator ensures that,
 * when an expiration date is provided, it conforms to the expected {@code dd/MM/yyyy} format. If the
 * value cannot be parsed, a {@link ValidationMessage} of type {@link ErrorType#Warning} is appended
 * to the collected messages so the upload can proceed while still reporting the problem. Rows with a
 * {@code null} or empty expiration date are considered valid and skipped.
 */
public class ExpirDateValidator extends AbstractIndexValidator {

  /** Date format used to parse the expiration date field (day/month/year). */
  private DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

  /** I18N message key used to look up the localized error description for an invalid date. */
  private static final String ERROR_DESCRIPTION =
    "bulk_upload_expir_date_error";

  /**
   * Creates the validator.
   *
   * @param serviceRegistry the Alfresco {@link ServiceRegistry} passed to the superclass
   */
  public ExpirDateValidator(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
  }

  /**
   * Validates the expiration date of the given index record.
   *
   * <p>If the record carries a non-empty expiration date that cannot be parsed with the {@code
   * dd/MM/yyyy} format, a warning-level {@link ValidationMessage} is added to {@code messages}.
   * Records without an expiration date are left untouched.
   *
   * @param indexRecord the bulk-upload record whose expiration date is checked
   * @param messages the mutable list of validation messages to which any detected error is appended
   */
  public void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    if (
      indexRecord.getExpirationDate() != null &&
      !indexRecord.getExpirationDate().isEmpty()
    ) {
      try {
        @SuppressWarnings("unused")
        final Date parsed = sdf.parse(indexRecord.getExpirationDate());
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
