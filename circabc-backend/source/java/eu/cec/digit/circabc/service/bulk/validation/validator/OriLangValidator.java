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
package eu.cec.digit.circabc.service.bulk.validation.validator;

import eu.cec.digit.circabc.service.bulk.indexes.IndexRecord;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessageImpl;
import eu.cec.digit.circabc.service.bulk.validation.ErrorType;
import org.alfresco.service.ServiceRegistry;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.List;

/**
 * Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC
 */
public class OriLangValidator extends AbstractIndexValidator {

    private static final String ERROR_DESCRIPTION = "bulk_upload_origlang_invalid";
    private static final String ERROR_DESCRIPTION1 = "bulk_upload_origlang_lang_not_specified";

    public OriLangValidator(final ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    public void validate(final IndexRecord indexRecord, final List<ValidationMessage> messages) {
        final String origLang = indexRecord.getOriLang();
        if (origLang != null && origLang.length() > 0) {
            if (!(origLang.equals("Y") || origLang.equals("N"))) {
                final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION);
                final ValidationMessage validationMessage =
                        new ValidationMessageImpl(
                                indexRecord.getRowNumber(),
                                indexRecord.getName(),
                                errorDescription,
                                ErrorType.Fatal);
                messages.add(validationMessage);
            }

            if (indexRecord.getOriLang().equals("Y") && indexRecord.getDocLang() == null) {
                final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION1);
                final ValidationMessage validationMessage =
                        new ValidationMessageImpl(
                                indexRecord.getRowNumber(),
                                indexRecord.getName(),
                                errorDescription,
                                ErrorType.Fatal);
                messages.add(validationMessage);
            }
        }
    }
}
