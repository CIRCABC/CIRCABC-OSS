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
package eu.cec.digit.circabc.service.bulk.indexes.message;

import eu.cec.digit.circabc.service.bulk.validation.ErrorType;

public class ValidationMessageImpl implements ValidationMessage {

    private int rowNumber;
    private String fileName;
    private String errorDescription;
    private ErrorType errorType;

    public ValidationMessageImpl(
            final int rowNumber,
            final String fileName,
            final String errorDescription,
            final ErrorType errorType) {
        this.rowNumber = rowNumber;
        this.fileName = fileName;
        this.errorDescription = errorDescription;
        this.errorType = errorType;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
