/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.cec.digit.circabc.service.config.auto.upload;

/** @author beaurpi */
public enum AutoUploadJobResult {
    JOB_OK(1),
    JOB_NOTHING_TO_DO(0),
    JOB_ERROR(-1),
    JOB_REMOTE_FTP_PROBLEM(-2);

    private final Integer result;

    AutoUploadJobResult(Integer result) {
        this.result = result;
    }

    /** @return the result */
    public Integer getResult() {
        return result;
    }
}
