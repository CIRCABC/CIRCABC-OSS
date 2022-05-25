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
package eu.cec.digit.circabc.business.api;

import org.springframework.extensions.surf.util.I18NUtil;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * @author Yanick Pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC
 */
public class BusinessRuntimeExpection extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -6603382649957220698L;
    private final Object[] parameters;

    /**
     *
     */
    public BusinessRuntimeExpection() {
        super();
        this.parameters = null;
    }

    /**
     * @param messageKey The message key
     * @param parameters The optional paramaters of the I18N message key
     */
    public BusinessRuntimeExpection(final String messageKey, final Object... parameters) {
        this(messageKey, null, parameters);
    }

    /**
     * @param messageKey The message key
     * @param cause      The previous cause of the error
     * @param parameters The optional paramaters of the I18N message key
     */
    public BusinessRuntimeExpection(final String messageKey, final Throwable cause,
                                    final Object... parameters) {
        super(messageKey, cause);
        this.parameters = parameters;
    }

    /**
     * @return The I18N message key
     */
    public String getMessageKey() {
        return getMessage();
    }

    /**
     * @return The validation message translated in the given locale
     */
    public String getLocalizedMessage(final Locale locale) {
        final String message = I18NUtil.getMessage(getMessageKey(), locale);

        if (parameters == null) {
            return message;
        } else {
            return MessageFormat.format(message, parameters);
        }
    }

    @Override
    public String getLocalizedMessage() {
        return getLocalizedMessage(I18NUtil.getLocale());
    }

}
