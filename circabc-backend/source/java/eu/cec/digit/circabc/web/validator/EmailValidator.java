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

import eu.cec.digit.circabc.service.user.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.internet.AddressException;


/**
 * email validator based on RFC 2822.
 * <p>
 * original code by: http://www.leshazlewood.com/ "Les"
 *
 * @author patrice.coppens@trasys.lu
 */
public class EmailValidator {

    /**
     * The message if the email is duplicate
     */
    public static final String err_duplicate = "err_duplicate";
    /**
     * The message if the email is invalidg
     */
    protected static final String err_invalid = "err_invalid";
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(EmailValidator.class);

    private EmailValidator() {

    }

    /**
     * Check if the object contains a valid email address
     *
     * @param userEnteredEmailString The string to test
     * @throws Exception Launch an exception with the corresponding message
     */
    public static void evaluate(String userEnteredEmailString) throws Exception {
        // Test the minimum length
        if (!isValid(userEnteredEmailString)) {
            throw new Exception(err_invalid);
        }

        // All is good
        if (logger.isInfoEnabled()) {
            logger.info("All clear");
        }
    }

    /**
     * Check if the email is unique before its creation
     *
     * @param UserService            the circabc user service reference
     * @param userEnteredEmailString the email that shoulb be unique
     * @throws Exception Launch an exception with the corresponding message
     */
    public static void evaluateUnicity(UserService userService, String userEnteredEmailString)
            throws Exception {
        // Test the minimum length
        if (isEmailExists(userService, userEnteredEmailString)) {
            throw new Exception(err_duplicate);
        }

        // All is good
        if (logger.isInfoEnabled()) {
            logger.info("All clear");
        }
    }

    public static boolean isEmailExists(UserService userService, String newEmail) {
        return userService.isEmailExists(newEmail, false);
    }

    public static boolean isValid(String userEnteredEmailString) {
        try {
            new javax.mail.internet.InternetAddress(userEnteredEmailString.trim()).validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }

    public static String getErrorInvalid() {
        return err_invalid;
    }

    public static String getErrorDuplicate() {
        return err_duplicate;
    }


}
