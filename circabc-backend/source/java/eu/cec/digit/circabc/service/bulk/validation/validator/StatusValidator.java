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

import eu.cec.digit.circabc.model.DocumentModel;
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
public class StatusValidator extends AbstractIndexValidator {

    private static final String ERROR_DESCRIPTION_1 = "bulk_upload_status_undefined";
    private static final String ERROR_DESCRIPTION_2 = "bulk_upload_status_invalid";

    public StatusValidator(final ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    public void validate(final IndexRecord indexRecord, final List<ValidationMessage> messages) {
        boolean valid = false;
        if (indexRecord.getStatus().length() == 0) {
            if (indexRecord.getRelTrans() != null) {
                // Traduction... no Status provided for a translation (not pivot)
                // This is normal: Do nothing
            } else {
                // Set default value and raise a warning
                indexRecord.setStatus(DocumentModel.STATUS_VALUES.get(0));
                final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION_1);
                final ValidationMessage validationMessage =
                        new ValidationMessageImpl(
                                indexRecord.getRowNumber(),
                                indexRecord.getName(),
                                errorDescription,
                                ErrorType.Warning);
                messages.add(validationMessage);
            }
        } else {
        	//FIX by ALMO.
        	//In the functional analysis, possible values are DRAFT, FINAL and RELEASED.
        	//In the model we use "RELEASE" instead of "RELEASED".
        	//To avoid updating all the model, if the user used RELEASED, we change the value to RELEASE
        	if("RELEASED".contentEquals(indexRecord.getStatus())) {
        		indexRecord.setStatus(DocumentModel.STATUS_VALUE_RELEASE);
        		valid=true;
        	} else {
        			for (final String status : DocumentModel.STATUS_VALUES) {
        				if (status.equals(indexRecord.getStatus())) {
        					valid = true;
        					break;
        				}
        			}
            }
            if (!valid) {
                final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION_2);
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