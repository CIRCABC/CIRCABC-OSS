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
 * Validates the "original language" flag ({@code oriLang}) of a bulk-upload index record.
 *
 * <p>During a bulk upload each row of the index describes a document to be imported. This validator
 * enforces two rules on the {@code oriLang} column:
 *
 * <ul>
 *   <li>If provided, the value must be either {@code "Y"} or {@code "N"}; any other value produces a
 *       {@link ErrorType#Fatal} validation message.
 *   <li>If the value is {@code "Y"} (this document carries the original language), the document
 *       language ({@code docLang}) must also be specified; otherwise a {@link ErrorType#Fatal}
 *       validation message is produced.
 * </ul>
 *
 * <p>An empty or {@code null} {@code oriLang} value is treated as "not provided" and passes without
 * any message.
 *
 * <p>Migration 3.1 -&gt; 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC.
 */
public class OriLangValidator extends AbstractIndexValidator {

  /** I18N message key used when {@code oriLang} holds a value other than "Y" or "N". */
  private static final String ERROR_DESCRIPTION =
    "bulk_upload_origlang_invalid";
  /** I18N message key used when {@code oriLang} is "Y" but no document language is specified. */
  private static final String ERROR_DESCRIPTION1 =
    "bulk_upload_origlang_lang_not_specified";

  /**
   * Creates the validator.
   *
   * @param serviceRegistry the Alfresco service registry passed to the superclass
   */
  public OriLangValidator(final ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
  }

  /**
   * Validates the original-language flag of the given index record and appends a validation message
   * for each rule that is violated.
   *
   * <p>When {@code oriLang} is set to a value other than "Y" or "N", or when it is "Y" while the
   * document language is missing, a {@link ErrorType#Fatal} {@link ValidationMessage} referencing
   * the record's row number and name is added to {@code messages}. An empty or {@code null}
   * {@code oriLang} adds no message.
   *
   * @param indexRecord the bulk-upload index record to validate
   * @param messages the collection to which any validation messages are appended
   */
  public void validate(
    final IndexRecord indexRecord,
    final List<ValidationMessage> messages
  ) {
    final String origLang = indexRecord.getOriLang();
    if (origLang != null && !origLang.isEmpty()) {
      if (!(origLang.equals("Y") || origLang.equals("N"))) {
        final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION);
        final ValidationMessage validationMessage = new ValidationMessageImpl(
          indexRecord.getRowNumber(),
          indexRecord.getName(),
          errorDescription,
          ErrorType.Fatal
        );
        messages.add(validationMessage);
      }

      if (
        indexRecord.getOriLang().equals("Y") && indexRecord.getDocLang() == null
      ) {
        final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION1);
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
