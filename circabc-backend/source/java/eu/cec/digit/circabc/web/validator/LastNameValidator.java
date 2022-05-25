/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Validator for the lastName
 *
 * @author Guillaume
 */
public class LastNameValidator {

    /**
     * The minimum permit length (not tested if value equals -1)
     */
    protected static final int minLength = 1;
    /**
     * The maximum permit length (not tested if value equals -1)
     */
    protected static final int maxLength = 50;
    /**
     * The message if the minimum length is wrong
     */
    protected static final String err_minimum_length = "err_minimum_length";
    /**
     * The message if the maximum length is wrong
     */
    protected static final String err_maximum_length = "err_maximum_length";
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(LastNameValidator.class);

    private LastNameValidator() {

    }

    /**
     * Check if the object contains a valid phone number
     *
     * @param lastName The string to test
     * @throws Exception Launch an exception with the corresponding message
     */
    public static void evaluate(String lastName) throws Exception {
        lastName = lastName.trim();

        // Test the minimum length
        if ((minLength > -1) && (lastName.length() < minLength)) {
            throw new Exception(err_minimum_length);
        }

        // Test the minimum length
        if ((maxLength > -1) && (lastName.length() > maxLength)) {
            throw new Exception(err_maximum_length);
        }

        // All is good
        if (logger.isInfoEnabled()) {
            logger.info("All clear");
        }
    }

    public static String getErrorMinimumLength() {
        return err_minimum_length;
    }

    public static String getErrorMaximumLength() {
        return err_maximum_length;
    }

    /**
     * @return the maxLength
     */
    public static int getMaxLength() {
        return maxLength;
    }

    /**
     * @return the minLength
     */
    public static int getMinLength() {
        return minLength;
    }
}
