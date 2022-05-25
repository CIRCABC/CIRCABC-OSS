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
package eu.cec.digit.circabc.business.aop;

import org.aopalliance.intercept.MethodInvocation;

/**
 * An exception handler is used to manage any kind of error occurend in the business layer or
 * below.
 *
 * @author Yanick Pignot
 */
public interface ExceptionHandler {

    /**
     * Get a user friendly message to be display to the target client.
     *
     * @param error The relevant error
     * @return a I18N message key
     */
    String getMessageKey(final Throwable error);


    /**
     * Get message key parameters needed for translation of the message key.
     *
     * @param error The relevant error
     * @return a I18N message key parameters
     */
    Object[] getMessageParameters(final Throwable error);

    /**
     * Do someting when the managed kind error is throwed. (ie: send an email, ...)
     *
     * @param methodInvocation The aop method call
     * @param error            The relevant error
     */
    void onThrows(final MethodInvocation methodInvocation, final Throwable error);


}
