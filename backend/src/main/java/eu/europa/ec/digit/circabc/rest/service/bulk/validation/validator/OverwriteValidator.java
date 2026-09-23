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
 * Validates the "overwrite" flag of a bulk import index record.
 *
 * <p>During a bulk upload each row of the index may specify whether an already existing item should
 * be overwritten. This validator ensures that, when the overwrite value is supplied, it holds one
 * of the only accepted flags: {@code "Y"} (overwrite) or {@code "N"} (do not overwrite). An empty
 * or absent value is considered valid and left untouched. Any other value produces a
 * {@link ErrorType#Fatal} {@link ValidationMessage} that is appended to the collected validation
 * results, preventing the record from being processed.
 */
public class OverwriteValidator extends AbstractIndexValidator {

  /**
   * Resource bundle key used to look up the localised error message reported when the overwrite
   * flag holds an unsupported value.
   */
  private static final String ERROR_DESCRIPTION = "bulk_upload_overwrite";

  /**
   * Creates an overwrite validator bound to the given Alfresco service registry.
   *
   * @param serviceRegistry the Alfresco {@link ServiceRegistry} providing access to the platform
   *     services required for validation
   */
  public OverwriteValidator(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
  }

  /**
   * Validates the overwrite flag carried by the given index record.
   *
   * <p>If the record's overwrite value is non-null, non-empty and different from the accepted flags
   * {@code "Y"} and {@code "N"}, a {@link ErrorType#Fatal} {@link ValidationMessage} (localised via
   * {@link #ERROR_DESCRIPTION}) is added to {@code messages}. Empty or {@code null} values pass
   * validation and leave {@code messages} unchanged.
   *
   * @param indexRecord the bulk import index record whose overwrite flag is being validated
   * @param messages the mutable list of validation messages to which any detected error is appended
   */
  public void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    final String overwrite = indexRecord.getOverwrite();
    if (
      overwrite != null &&
      !overwrite.isEmpty() &&
      !(overwrite.equals("Y") || overwrite.equals("N"))
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
