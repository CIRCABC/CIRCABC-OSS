/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.cec.digit.circabc.repo.admin.debug;

import org.springframework.extensions.surf.util.I18NUtil;

import java.text.DateFormat;
import java.util.Date;

/** @author Yanick Pignot */
public abstract class ReportUtils {

    public static final String DEFAULT_GROUP_NAME = "DEFAULT";
    private static final ThreadLocal<DateFormat> dateFormat =
            new ThreadLocal<DateFormat>() {
                @Override
                protected DateFormat initialValue() {
                    return DateFormat.getDateTimeInstance(
                            DateFormat.SHORT, DateFormat.SHORT, I18NUtil.getLocale());
                }
            };

    private ReportUtils() {
    }

    public static String getDisplayName(String group, String name) {
        if (DEFAULT_GROUP_NAME.equals(group)) {
            return name;
        } else {
            return group + "." + name;
        }
    }

    public static String getSecuredString(Object object) {
        return getSecuredString(object, "N/A");
    }

    public static String getSecuredString(Object object, String replacementString) {
        if (object == null) {
            return replacementString;
        } else if (object instanceof Date) {
            Date date = (Date) object;

            return dateFormat.get().format(date);
        } else {
            return object.toString();
        }
    }
}
