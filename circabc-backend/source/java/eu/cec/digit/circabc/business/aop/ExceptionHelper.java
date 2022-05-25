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

import eu.cec.digit.circabc.business.api.nav.NavNode;
import eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.aopalliance.intercept.MethodInvocation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yanick Pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 ChildAssoc changed to ChildAssociationRef.
 */
public abstract class ExceptionHelper {

    private static final Pattern SPLIT_UPPER_LOWER_CASE = Pattern.compile("[A-Z]+[a-z]*");

    private ExceptionHelper() {
    }

    /**
     * Print the stack trace of an error in a String
     *
     * @param throwable The error to print
     */
    public static String exceptionAsString(final Throwable throwable) {
        final StringWriter stringWritter = new StringWriter();
        final PrintWriter printWritter = new PrintWriter(stringWritter);
        throwable.printStackTrace(printWritter);

        return printWritter.toString();
    }

    /**
     * Compute un system admin friendly message to log with the error
     * <p>
     * Print:
     * <ul>
     * <li>The authenticated user name</li>
     * <li>The Error class and message</li>
     * <li>The Error cause class and message (if provided)</li>
     * </ul>
     *
     * @param throwable The error to print
     */
    public static String getLoggerText(final Throwable throwable) {
        return getFullLoggerText(throwable, null, null);
    }

    /**
     * Compute un system admin friendly detailled message to log with the error.
     * <p>
     * Print:
     * <ul>
     * <li>The authenticated user name</li>
     * <li>The Error class and message</li>
     * <li>The Error cause class and message (if provided)</li>
     * <li>The method name and its class (if methodInvocation provided)</li>
     * <li>The method first argument (if navigationBusinessSrv And methodInvocation provided with at
     * least one argument)</li>
     * </ul>
     *
     * @param throwable        The error to print
     * @param methodInvocation The method
     */
    public static String getFullLoggerText(final Throwable throwable,
                                           final MethodInvocation methodInvocation, final NavigationBusinessSrv navigationBusinessSrv) {
        final StringBuilder builder = new StringBuilder("Circabc Crash Report.");

        builder
                .append("\n\t For user: ")
                .append(AuthenticationUtil.getFullyAuthenticatedUser())
                .append("\n\t Error: ")
                .append(throwable.getClass())
                .append(" (Message: ")
                .append(throwable.getMessage())
                .append(')')
                .append("\n\t Cause: ");

        if (throwable.getCause() != null) {
            builder
                    .append(throwable.getCause().getClass())
                    .append(" (Message: ")
                    .append(throwable.getCause().getMessage())
                    .append(')');
        } else {
            builder.append("UNDEFINED");
        }

        if (methodInvocation != null) {
            // compute the name of the invocated method

            final Method method = methodInvocation.getMethod();
            builder
                    .append("\n\t For Method: ")
                    .append(method.getDeclaringClass())
                    .append(".")
                    .append(method.getName());

            if (navigationBusinessSrv != null) {

                final Object[] args = methodInvocation.getArguments();
                if (args != null && args.length > 0 && args[0] != null) {
                    // try to display the path of the argument if
                    final String relevantArgString;

                    final Object firstArg = args[0];

                    if (firstArg instanceof NodeRef) {
                        relevantArgString = navigationBusinessSrv.getNodePathString((NodeRef) firstArg);
                    } else if (firstArg instanceof ChildAssociationRef) {
                        relevantArgString = navigationBusinessSrv
                                .getNodePathString(((ChildAssociationRef) firstArg).getChildRef());
                    } else if (firstArg instanceof NavNode) {
                        relevantArgString = navigationBusinessSrv
                                .getNodePathString(((NavNode) firstArg).getNodeRef());
                    } else {
                        relevantArgString = firstArg.toString();
                    }

                    builder
                            .append("\n\t For Arg 1: ")
                            .append(relevantArgString)
                            .append(".");
                }
            }

        }

        return builder.toString();
    }

    /**
     * Generate a user friendly text to inform the details of the error
     *
     * @param error The relevant error
     */
    public static String getFriendlyCause(final Throwable error) {
        final StringBuilder builder = new StringBuilder("");

        final Matcher matcher = SPLIT_UPPER_LOWER_CASE.matcher(error.getClass().getSimpleName());

        while (matcher.find()) {
            builder.append(matcher.group());
        }

        final String cause = error.getCause() == null ? null : error.getCause().getMessage();

        if (cause != null && cause.trim().length() < 1) {
            builder.append(": ").append(cause);
        }

        return builder.toString();
    }

}
