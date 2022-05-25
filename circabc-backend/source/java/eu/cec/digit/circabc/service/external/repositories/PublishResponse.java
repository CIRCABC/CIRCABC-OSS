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
package eu.cec.digit.circabc.service.external.repositories;

/**
 * Response to the publish action. Error or publish description/data for status or logging.
 *
 * @author schwerr
 */
public class PublishResponse {

    private boolean success = true;
    private String message = null;

    /**
     * Gets the value of the success
     *
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the value of the success
     *
     * @param success the success to set.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets the value of the message
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message
     *
     * @param message the message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
