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
import io.swagger.model.alfresco.DocumentModel;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Bulk-import index validator that checks the security ranking declared for a record.
 *
 * <p>During a bulk upload, each row/record may specify a security ranking. This validator ensures
 * that any provided value matches one of the allowed rankings defined by
 * {@link DocumentModel#SECURITY_RANKINGS}. When the value is present but not recognised, a
 * {@link ErrorType#Fatal} {@link ValidationMessage} is produced (using the localised
 * {@code bulk_upload_secrank_invalid} message) so the offending row is reported to the user. An
 * empty or {@code null} security ranking is considered acceptable and is skipped.
 */
public class SecurityRankingValidator extends AbstractIndexValidator {

  /**
   * I18N message key resolved via {@link I18NUtil} to build the human-readable description attached
   * to a validation error when the security ranking is invalid.
   */
  private static final String ERROR_DESCRIPTION = "bulk_upload_secrank_invalid";

  /**
   * Creates a security ranking validator bound to the given Alfresco service registry.
   *
   * @param serviceRegistry the Alfresco {@link ServiceRegistry} providing access to the platform
   *     services, forwarded to {@link AbstractIndexValidator}
   */
  public SecurityRankingValidator(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
  }

  /**
   * Validates the security ranking of a single bulk-import record.
   *
   * <p>If the record declares a non-empty security ranking that does not match any of the values in
   * {@link DocumentModel#SECURITY_RANKINGS}, a {@link ErrorType#Fatal} {@link ValidationMessage} is
   * appended to {@code messages}, carrying the record's row number, name and the localised error
   * description. Records with a {@code null} or empty ranking are left untouched.
   *
   * @param indexRecord the bulk-import record whose security ranking is being checked
   * @param messages the mutable list of validation messages to which any validation error is added
   */
  public void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    final String currentSecurityRanking = indexRecord.getSecurityRanking();
    if (currentSecurityRanking != null && !currentSecurityRanking.isEmpty()) {
      boolean valid = false;
      for (final String securityRanking : DocumentModel.SECURITY_RANKINGS) {
        if (securityRanking.equals(currentSecurityRanking)) {
          valid = true;
          break;
        }
      }
      if (!valid) {
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
}
