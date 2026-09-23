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
 * Validator that checks the {@code name} field of a bulk-upload {@link IndexRecord}.
 *
 * <p>As part of the bulk-upload validation chain, this validator enforces two rules for each
 * index record:
 *
 * <ul>
 *   <li>The name must be provided (non-{@code null} and non-empty), unless the record represents
 *       an empty translation (i.e. it has no content).
 *   <li>The name must not start with {@code ".."}, to prevent path-traversal style names.
 * </ul>
 *
 * When a rule is violated, a {@link ValidationMessage} of {@link ErrorType#Fatal} is added to the
 * supplied message list. Error descriptions are resolved to localized text through {@link
 * I18NUtil}.
 *
 * <p>Migration 3.1 -&gt; 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC.
 */
public class NameValidator extends AbstractIndexValidator {

  /** Placeholder file name used when reporting a name-related validation message. */
  private static final String EMPTY_FILE_NAME = "";

  /** I18N message key used when the mandatory name is missing. */
  private static final String ERROR_DESCRIPTION =
    "bulk_upload_mandatory_name_not_provided";

  /** I18N message key used when the name illegally begins with {@code ".."}. */
  private static final String ERROR_DESCRIPTION1 =
    "bulk_upload_mandatory_name_not_begin_with_point";

  /**
   * Creates a new {@code NameValidator}.
   *
   * @param serviceRegistry the Alfresco service registry made available to the validator through
   *     the superclass
   */
  public NameValidator(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
  }

  /**
   * Validates the name of the given index record and appends a fatal {@link ValidationMessage} to
   * {@code messages} for each rule that is violated.
   *
   * <p>If the name is {@code null} or empty, a "name not provided" error is reported unless the
   * record has no content (an empty translation). Otherwise, if the name starts with {@code ".."},
   * a "name must not begin with point" error is reported.
   *
   * @param indexRecord the bulk-upload record whose name is validated
   * @param messages the list to which any resulting validation messages are added
   */
  public void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    if (indexRecord.getName() == null || indexRecord.getName().isEmpty()) {
      // Check if it's not an empty translation
      if (
        indexRecord.getNoContent() == null ||
        (indexRecord.getNoContent().equals("N"))
      ) {
        final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION);
        final ValidationMessage validationMessage = new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          EMPTY_FILE_NAME,
          errorDescription,
          ErrorType.Fatal
        );
        messages.add(validationMessage);
      }
    } else {
      // Check it doenst begin with ..
      if (indexRecord.getName().startsWith("..")) {
        final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION1);
        final ValidationMessage validationMessage = new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          EMPTY_FILE_NAME,
          errorDescription,
          ErrorType.Fatal
        );
        messages.add(validationMessage);
      }
    }
  }
}
