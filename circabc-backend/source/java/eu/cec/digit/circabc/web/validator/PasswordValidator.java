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

import org.alfresco.web.app.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;


/**
 * Validate password Can also generate password
 *
 * @author Clinckart Stephane
 * @author Guillaume
 */
public class PasswordValidator {

    private static final Log logger = LogFactory.getLog(PasswordValidator.class);


    private static final String VALID_CHARS = "!\"#$%&'()*+?-./:;<=>?@[\\]^_`{|}~";

    /**
     * The minimum size for a password
     */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Size for a generated password
     */
    private static final int GENERATED_PASSWORD_LENGTH = 15;

    /**
     * Default generator to use for the secure random sead
     */
    private static final String DEFAULT_ALGORITHM_RANDOM = "SHA1PRNG";
    private static final String ERR_VALID_PASS_1 = "err_valid_pass_1";
    private static final String ERR_VALID_PASS_2 = "err_valid_pass_2";
    private static final String ERR_VALID_PASS_3 = "err_valid_pass_3";
    private ArrayList<Integer> characterRange = new ArrayList<>();
    private ArrayList<Integer> lowerCaseRange = new ArrayList<>();
    private ArrayList<Integer> upperCaseRange = new ArrayList<>();
    private ArrayList<Integer> numericRange = new ArrayList<>();
    private ArrayList<Integer> specialCharactersRange = new ArrayList<>();
    private String err_valid_pass_1 = null;

    private String err_valid_pass_2 = null;

    private String err_valid_pass_3 = null;

    public PasswordValidator() {
        initializeRanges();
    }

    public void evaluate(String password) {
        password = password.trim();

        // Password length
        verifyPasswordLength(password);
        // Password Characters range
        verifyPasswordCharatersRange(password);
        // Respect 3 of 4 group of characters
        respectMinimumGroupRules(password);

    }

    private void verifyPasswordLength(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            if (logger.isTraceEnabled()) {
                logger.trace("Password is to short:" + password.length()
                        + " when minimum is:" + MIN_PASSWORD_LENGTH);
            }
            if (err_valid_pass_1 == null) {
                err_valid_pass_1 = getMessage(ERR_VALID_PASS_1,
                        new Object[]{MIN_PASSWORD_LENGTH});
            }

            throw new ValidatorException(new FacesMessage(err_valid_pass_1));
        }
    }

    private void verifyPasswordCharatersRange(String password) {
        for (int i = 0; i < password.length(); i++) {
            // Verify if all letters are in the valid charset
            if (inCharset(password.charAt(i)) == false) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Password contains invalid character:"
                            + password.charAt(i) + " at position:" + i);
                }
                if (err_valid_pass_2 == null) {
                    err_valid_pass_2 = getMessage(ERR_VALID_PASS_2,
                            new Object[]{VALID_CHARS});
                }

                throw new ValidatorException(new FacesMessage(err_valid_pass_2));
            }
        }
    }

    /**
     * Helper to get I18N messages
     *
     * @param key    Key to find in property translation file
     * @param params Dynamic parameters for the message
     * @return String that contains the translated message
     */

    private String getMessage(String key, Object[] params) {
        if (params == null) {
            return Application.getMessage(FacesContext.getCurrentInstance(),
                    key);
        } else {
            return MessageFormat.format(Application.getMessage(FacesContext
                    .getCurrentInstance(), key), params);
        }
    }

    /**
     * Method that initialize the valid characters range
     */
    private void initializeRanges() {
        // Variables created for debug facilities
        char min;
        char max;

        min = 'a';
        max = 'z';
        for (int iLoop = min; iLoop <= max; iLoop++) {
            lowerCaseRange.add(iLoop);
            characterRange.add(iLoop);
        }

        min = 'A';
        max = 'Z';
        for (int iLoop = min; iLoop <= max; iLoop++) {
            upperCaseRange.add(iLoop);
            characterRange.add(iLoop);
        }

        min = '0';
        max = '9';
        for (int iLoop = min; iLoop <= max; iLoop++) {
            numericRange.add(iLoop);
            characterRange.add(iLoop);
        }

        // Add the white space
        min = ' ';
        max = ' ';
        for (int iLoop = min; iLoop <= max; iLoop++) {
            characterRange.add(iLoop);
        }

        char charToAdd;
        for (int iLoop = 0; iLoop < VALID_CHARS.length(); iLoop++) {
            charToAdd = VALID_CHARS.charAt(iLoop);
            specialCharactersRange.add((int) charToAdd);
            characterRange.add((int) charToAdd);
        }
    }

    /**
     * Method that validate that the character is include in the valid character range
     *
     * @param c character to validate
     * @return true if valid
     */
    private boolean inCharset(int c) {
        return characterRange.contains(c);
    }

    /**
     * Password must contains at least three of the following four character groups: Group1: Upper
     * Case(A to Z); Group2: Lower Case(a to z); Group3: Numeric(0 to 9); Group4: Special
     * Characters()
     */
    private void respectMinimumGroupRules(String password) {
        Character c;
        int hasLowerCase = 0;
        int hasUpperCase = 0;
        int hasNumeric = 0;
        int hasSpecialChars = 0;
        int result = 0;
        for (int i = 0; i < password.length(); i++) {
            c = password.charAt(i);

            Integer charInteger = Integer.valueOf(c);
            if (lowerCaseRange.contains(charInteger)) {
                hasLowerCase = 1;
            } else if (upperCaseRange.contains(charInteger)) {
                hasUpperCase = 1;
            } else if (numericRange.contains(charInteger)) {
                hasNumeric = 1;
            } else if (specialCharactersRange.contains(charInteger)) {
                hasSpecialChars = 1;
            }
            result = hasLowerCase + hasUpperCase + hasNumeric + hasSpecialChars;
            if (result >= 3) {
                // If you are here, that means the password is compliant to the
                // rule
                return;
            }
        }

        // If you are here, that means the password is not compliant to the rule
        if (logger.isTraceEnabled()) {
            if (hasLowerCase == 0) {
                logger.trace("Password has no LowerCase character.");
            }
            if (hasUpperCase == 0) {
                logger.trace("Password has no UpperCase character.");
            }
            if (hasNumeric == 0) {
                logger.trace("Password has no Numerical character.");
            }
            if (hasSpecialChars == 0) {
                logger.trace("Password has no Special character.");
            }
        }
        if (err_valid_pass_3 == null) {
            err_valid_pass_3 = getMessage(ERR_VALID_PASS_3,
                    new Object[]{VALID_CHARS});
        }
        throw new ValidatorException(new FacesMessage(err_valid_pass_3));
    }

    /**
     * Method to generate a new valid password
     *
     * @return The password generated
     * @author Guillaume
     */
    public char[] generate() throws NoSuchAlgorithmException {
        SecureRandom rand = SecureRandom.getInstance(DEFAULT_ALGORITHM_RANDOM);
        int characterRangeLength = characterRange.size();
        char[] password = new char[GENERATED_PASSWORD_LENGTH];
        boolean exceptionFound = false;

        // Start work
        do {
            exceptionFound = false;
            for (int x = 0; x < GENERATED_PASSWORD_LENGTH; x++) {
                password[x] = (char) characterRange.get(rand.nextInt(characterRangeLength)).intValue();
            }
            try {
                evaluate(String.valueOf(password));
            } catch (Exception e) {
                // Generated password is not good - retry
                exceptionFound = true;
            }
        } while (exceptionFound);

        return password;
    }
}
