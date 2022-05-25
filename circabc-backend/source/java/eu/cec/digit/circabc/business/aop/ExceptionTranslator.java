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

import eu.cec.digit.circabc.business.api.BusinessRuntimeExpection;
import eu.cec.digit.circabc.business.api.BusinessStackError;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yanick Pignot
 */
public class ExceptionTranslator implements MethodInterceptor {

    private final Log logger = LogFactory.getLog(ExceptionTranslator.class);
    private final String DEFAULT_MESSAGE = "";
    public Map<Class, ExceptionHandler> exceptionHandlers = new HashMap<>();

    public void registerHandler(final ExceptionHandler handler, Class clazz) {
        if (clazz == null) {
            logger.error(
                    "Handler not registered beacause it doens't provide managed error type: " + handler
                            .getClass());
        } else {
            if (BusinessStackError.class.equals(clazz) == false) {
                exceptionHandlers.put(clazz, handler);
            } else if (logger.isWarnEnabled()) {
                logger.warn("BusinessStackError error type is managed by default. ");
            }
        }
    }

    public Object invoke(MethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        } catch (final BusinessStackError stack) {
            throw stack;
        } catch (final Throwable error) {
            final BusinessStackError stack;
            final Class<? extends Throwable> errorClass = error.getClass();

            final ExceptionHandler handler = getHandler(errorClass);

            if (handler != null) {
                stack = new BusinessStackError(new BusinessRuntimeExpection(handler.getMessageKey(error),
                        handler.getMessageParameters(error)));

                try {
                    handler.onThrows(mi, error);
                } catch (final Throwable t) {
                    logger.error("Impossible to perform " + handler.getClass() + ".onThrows()", t);
                }

            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "No catched / intercepted error occurs in the business layer. Please to correct or translate it! "
                                    + "\n\tClass        : " + errorClass
                                    + "\n\tMessage      : " + error.getMessage()
                                    + "\n\tCause type   : " + error.getCause() == null ? "N/A"
                                    : error.getCause().getClass()
                                    + "\n\tCause message: " + error.getCause() == null ? "N/A"
                                    : error.getCause().getMessage()
                    );
                }

                stack = new BusinessStackError(new BusinessRuntimeExpection(DEFAULT_MESSAGE));
            }

            throw stack;
        }

    }

    private ExceptionHandler getHandler(final Class<? extends Throwable> errorClass) {
        ExceptionHandler handler = exceptionHandlers.get(errorClass);
        if (handler == null) {
            for (final Class<? extends Throwable> clazz : exceptionHandlers.keySet()) {
                if (clazz.isAssignableFrom(errorClass)) {
                    handler = exceptionHandlers.get(clazz);
                    break;
                }
            }
        }
        return handler;
    }

}
