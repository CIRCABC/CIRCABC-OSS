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

import eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv;
import eu.cec.digit.circabc.service.mail.MailService;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yanick Pignot
 */
public class GenericExceptionHandler implements ExceptionHandler {

    private final Log logger = LogFactory.getLog(GenericExceptionHandler.class);
    private ExceptionTranslator exceptionTranslator;
    private MailService mailService;
    private NavigationBusinessSrv navigationBusinessSrv;
    private String message;
    private boolean printStack;
    private boolean appendCause;
    private List<String> managedClasses;
    private MailDestinator mailDestinator;

    public void init() {
        for (final String className : getManagedClasses()) {
            try {
                exceptionTranslator.registerHandler(this, Class.forName(className));
            } catch (ClassNotFoundException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "Impossible to register hndler for class " + className + " since it doens't exists");
                }
            }

        }
    }

    public String getMessageKey(Throwable error) {
        String msg = getMessage();
        if (msg == null) {
            msg = error.getMessage();
        }
        return msg;
    }

    public Object[] getMessageParameters(final Throwable error) {
        if (isAppendCause()) {
            return new Object[]{mailService.getHelpdeskAddress(),
                    ExceptionHelper.getFriendlyCause(error)};
        } else {
            return new Object[]{mailService.getHelpdeskAddress()};
        }
    }

    public void onThrows(final MethodInvocation methodInvocation, final Throwable error) {
        final String loggerMessage = ExceptionHelper
                .getFullLoggerText(error, methodInvocation, navigationBusinessSrv);

        if (isPrintStack()) {
            logger.error(loggerMessage, error);
        } else {
            logger.debug(loggerMessage, error);
        }

        if (mailDestinator != null && MailDestinator.NOBODY.equals(mailDestinator) == false) {
            final List<String> to = new ArrayList<>();

            switch (mailDestinator) {
                case DEV_TEAM:
                    to.add(mailService.getDevTeamEmailAddress());
                    break;
                case SUPPORT_TEAM:
                    to.add(mailService.getSupportEmailAddress());
                    break;
                case SUPPORT_DEV_TEAM:
                    to.add(mailService.getDevTeamEmailAddress());
                    to.add(mailService.getSupportEmailAddress());
                    break;
                default:
                    break;
            }

            if (to.size() > 0) {
                try {
                    mailService.send(mailService.getNoReplyEmailAddress(),
                            to, null,
                            "[CIRCABC] crash report",
                            loggerMessage
                                    + "\n\n_______________________\n\n"
                                    + ExceptionHelper.exceptionAsString(error),
                            false, false);
                } catch (final Throwable t) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Impossible to send crash report to " + to.toString()
                                + ". The mail server is probably down.", t);
                    }
                }
            }
        }
    }

    /**
     * @param exceptionTranslator the exceptionTranslator to set
     */
    public final void setExceptionTranslator(ExceptionTranslator exceptionTranslator) {
        this.exceptionTranslator = exceptionTranslator;
    }

    // ----------
    // -- private helpers

    // ----------
    // -- Abstract

    // ----------
    // -- IOC

    /**
     * @return the message
     */
    public final String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public final void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the printStack
     */
    public final boolean isPrintStack() {
        return printStack;
    }

    /**
     * @param printStack the printStack to set
     */
    public final void setPrintStack(boolean printStack) {
        this.printStack = printStack;
    }

    /**
     * @return the managedClasses
     */
    public final List<String> getManagedClasses() {
        return managedClasses;
    }

    /**
     * @param managedClasses the managedClasses to set
     */
    public final void setManagedClasses(List<String> managedClasses) {
        this.managedClasses = managedClasses;
    }

    /**
     * @return the appendCause
     */
    public final boolean isAppendCause() {
        return appendCause;
    }

    /**
     * @param appendCause the appendCause to set
     */
    public final void setAppendCause(boolean appendCause) {
        this.appendCause = appendCause;
    }

    /**
     * @return the mailDestinator
     */
    public final String getMailDestinator() {
        return mailDestinator.toString();
    }

    /**
     * @param mailDestinator the mailDestinator to set
     */
    public final void setMailDestinator(String mailDestinator) {
        this.mailDestinator = MailDestinator.valueOf(mailDestinator);
    }

    /**
     * @param mailService the mailService to set
     */
    public final void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * @param navigationBusinessSrv the navigationBusinessSrv to set
     */
    public final void setNavigationBusinessSrv(NavigationBusinessSrv navigationBusinessSrv) {
        this.navigationBusinessSrv = navigationBusinessSrv;
    }

    private enum MailDestinator {
        SUPPORT_TEAM,
        DEV_TEAM,
        SUPPORT_DEV_TEAM,
        NOBODY
    }

}
