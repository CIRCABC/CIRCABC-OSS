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
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Validator used during bulk upload to ensure that the document language declared for an index
 * record is a valid, supported content filter language.
 *
 * <p>The validator reads the {@code docLang} value from an {@link IndexRecord} and checks it against
 * the set of languages provided by Alfresco's {@link ContentFilterLanguagesService}. When the value
 * is present but does not match any of the supported languages, a {@link ErrorType#Fatal} validation
 * message is produced and appended to the collected messages.
 */
public class LangValidator extends AbstractIndexValidator {

  /** I18N message key used to describe an invalid document language error. */
  private static final String ERROR_DESCRIPTION = "bulk_upload_param_doc_lang";

  /**
   * Creates a new language validator.
   *
   * @param serviceRegistry the Alfresco service registry used to resolve the {@link
   *     ContentFilterLanguagesService}
   */
  public LangValidator(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
  }

  /**
   * Validates the document language of the given index record.
   *
   * <p>If the record declares a non-empty {@code docLang} that is not part of the languages returned
   * by the {@link ContentFilterLanguagesService}, a {@link ErrorType#Fatal} {@link ValidationMessage}
   * is added to {@code messages}. Records with a {@code null} or empty language are considered valid
   * and produce no message.
   *
   * @param indexRecord the index record whose document language is validated
   * @param messages the list to which any validation message is appended
   */
  public void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    if (indexRecord.getDocLang() != null) {
      @SuppressWarnings("deprecation")
      final ContentFilterLanguagesService contentFilterLanguagesService =
        this.serviceRegistry.getContentFilterLanguagesService();
      if (
        indexRecord.getDocLang() != null &&
        !indexRecord.getDocLang().isEmpty() &&
        !contentFilterLanguagesService
          .getFilterLanguages()
          .contains(indexRecord.getDocLang())
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
}
