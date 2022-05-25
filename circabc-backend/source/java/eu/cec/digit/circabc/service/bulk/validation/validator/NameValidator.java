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
public class NameValidator extends AbstractIndexValidator {

    private static final String EMPTY_FILE_NAME = "";
    private static final String ERROR_DESCRIPTION = "bulk_upload_mandatory_name_not_provided";
    private static final String ERROR_DESCRIPTION1 =
            "bulk_upload_mandatory_name_not_begin_with_point";

    public NameValidator(final ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    public void validate(final IndexRecord indexRecord, final List<ValidationMessage> messages) {
        if (indexRecord.getName() == null || indexRecord.getName().length() == 0) {
            // Check if it's not an empty translation
            if (indexRecord.getNoContent() == null || (indexRecord.getNoContent().equals("N"))) {
                final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION);
                final ValidationMessage validationMessage =
                        new ValidationMessageImpl(
                                indexRecord.getRowNumber(), EMPTY_FILE_NAME, errorDescription, ErrorType.Fatal);
                messages.add(validationMessage);
            }
        } else {
            // Check it doenst begin with ..
            if (indexRecord.getName().startsWith("..")) {
                final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION1);
                final ValidationMessage validationMessage =
                        new ValidationMessageImpl(
                                indexRecord.getRowNumber(), EMPTY_FILE_NAME, errorDescription, ErrorType.Fatal);
                messages.add(validationMessage);
            }
        }
    }
}
