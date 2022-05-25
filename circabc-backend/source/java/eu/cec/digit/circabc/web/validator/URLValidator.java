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

import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;

/**
 * Validator for the URL property
 *
 * @author David Ferraz
 * @author Slobodan Filipovic
 */
public class URLValidator {

    /**
     * Logger
     */
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(URLValidator.class);


    /**
     * Check if the object contains a valid URL
     *
     * @param url The string to test
     * @throws Exception Launch an exception with the corresponding message
     */
    public static void evaluate(String url) throws ValidatorException {

        url = url.trim();
        if (url == null || url.length() < 1) {
            throw new ValidatorException(new FacesMessage("library_add_url_url_empty"));
        }

        org.apache.commons.validator.routines.UrlValidator validator = new org.apache.commons.validator.routines.UrlValidator();
        if (!validator.isValid(url)) {
            if (logger.isErrorEnabled()) {
                logger.error("Error validationg: " + url);
            }
            throw new ValidatorException(new FacesMessage("library_add_url_url_invalid"));
        }
    }
}
