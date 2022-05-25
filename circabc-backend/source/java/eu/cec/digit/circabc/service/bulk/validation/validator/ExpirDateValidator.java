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

import eu.cec.digit.circabc.service.bulk.BulkService;
import eu.cec.digit.circabc.service.bulk.indexes.IndexRecord;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessageImpl;
import eu.cec.digit.circabc.service.bulk.validation.ErrorType;
import org.alfresco.service.ServiceRegistry;
import org.springframework.extensions.surf.util.I18NUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExpirDateValidator extends AbstractIndexValidator {

    private static final ThreadLocal<DateFormat> sdf =
            new ThreadLocal<DateFormat>() {
                @Override
                protected DateFormat initialValue() {
                    return new SimpleDateFormat(BulkService.INDEX_DATE_FORMAT);
                }
            };
    private static final String ERROR_DESCRIPTION = "bulk_upload_expir_date_error";

    public ExpirDateValidator(final ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    public void validate(final IndexRecord indexRecord, final List<ValidationMessage> messages) {

        if (indexRecord.getExpirationDate() != null && indexRecord.getExpirationDate().length() > 0) {
            try {
                @SuppressWarnings("unused") final Date parsed = sdf.get().parse(indexRecord.getExpirationDate());
            } catch (final ParseException pe) {
                final String errorDescription = I18NUtil.getMessage(ERROR_DESCRIPTION);
                final ValidationMessage validationMessage =
                        new ValidationMessageImpl(
                                indexRecord.getRowNumber(),
                                indexRecord.getName(),
                                errorDescription,
                                ErrorType.Warning);
                messages.add(validationMessage);
            }
        }
    }
}
