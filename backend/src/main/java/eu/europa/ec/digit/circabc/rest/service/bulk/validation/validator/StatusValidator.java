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
 * Bulk-upload index validator that checks and normalizes the {@code status} column of a single
 * index record before the associated document is imported.
 *
 * <p>The validation applies the following rules:
 *
 * <ul>
 *   <li>If the status is empty and the record is a translation (i.e. it references another
 *       document via {@code relTrans}), the record is left untouched, as a translation is allowed to
 *       inherit its status from the master document.
 *   <li>If the status is empty and the record is not a translation, the status is defaulted to the
 *       first value of {@link DocumentModel#STATUS_VALUES} and a {@link ErrorType#Warning} message
 *       is emitted.
 *   <li>The legacy value {@code "RELEASED"} is normalized to {@link
 *       DocumentModel#STATUS_VALUE_RELEASE}.
 *   <li>Any other value that is not part of {@link DocumentModel#STATUS_VALUES} produces a {@link
 *       ErrorType#Fatal} validation message.
 * </ul>
 *
 * <p>Validation feedback is accumulated into the supplied list of {@link ValidationMessage}
 * instances rather than raised as exceptions.
 */
public class StatusValidator extends AbstractIndexValidator {

  /** I18N message key used to warn that a status was missing and has been defaulted. */
  private static final String ERROR_DESCRIPTION_1 =
    "bulk_upload_status_undefined";
  /** I18N message key used to report a status value that is not a recognized status. */
  private static final String ERROR_DESCRIPTION_2 =
    "bulk_upload_status_invalid";

  /**
   * Creates a status validator bound to the given Alfresco service registry.
   *
   * @param serviceRegistry the Alfresco {@link ServiceRegistry} providing access to the platform
   *     services required for validation
   */
  public StatusValidator(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
  }

  /**
   * Validates and normalizes the status of the given index record, appending any warning or fatal
   * findings to the supplied message list.
   *
   * <p>Empty statuses are handled by {@link #handleEmptyStatus(IndexRecord, List)} and non-empty
   * statuses by {@link #validateNonEmptyStatus(IndexRecord, List)}. The record's status may be
   * mutated as part of the normalization performed by these helpers.
   *
   * @param indexRecord the index record whose status is validated and possibly normalized
   * @param messages the collection to which validation messages are added
   */
  public void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    if (indexRecord.getStatus().isEmpty()) {
      handleEmptyStatus(indexRecord, messages);
      return;
    }
    validateNonEmptyStatus(indexRecord, messages);
  }

  private void handleEmptyStatus(
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if (indexRecord.getRelTrans() != null) {
      return; // Translation without status is normal
    }
    indexRecord.setStatus(DocumentModel.STATUS_VALUES.get(0));
    messages.add(
      new ValidationMessageImpl(
        indexRecord.getRowNumber(),
        indexRecord.getName(),
        I18NUtil.getMessage(ERROR_DESCRIPTION_1),
        ErrorType.Warning
      )
    );
  }

  private void validateNonEmptyStatus(
    IndexRecord indexRecord,
    List<ValidationMessage> messages
  ) {
    if ("RELEASED".contentEquals(indexRecord.getStatus())) {
      indexRecord.setStatus(DocumentModel.STATUS_VALUE_RELEASE);
      return;
    }
    if (!DocumentModel.STATUS_VALUES.contains(indexRecord.getStatus())) {
      messages.add(
        new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          indexRecord.getName(),
          I18NUtil.getMessage(ERROR_DESCRIPTION_2),
          ErrorType.Fatal
        )
      );
    }
  }
}
