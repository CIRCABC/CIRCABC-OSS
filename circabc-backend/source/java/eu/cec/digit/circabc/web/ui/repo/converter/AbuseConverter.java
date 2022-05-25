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
package eu.cec.digit.circabc.web.ui.repo.converter;

import eu.cec.digit.circabc.service.newsgroup.AbuseReport;
import org.alfresco.web.app.Application;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import java.text.MessageFormat;
import java.util.List;

/**
 * Convertor for an abuse
 *
 * @author yanick pignot
 */
public class AbuseConverter implements Converter {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.AbuseConverter";
    private static final String MSG_REPORT = "post_moderation_abuse_html_detail";

    public static String getAsString(final FacesContext fc, final AbuseReport report) {
        return MessageFormat.format(Application.getMessage(fc, MSG_REPORT),
                report.getReporter(),
                report.getReportDate(),
                report.getMessage().replace("\n", " "));
    }

    public Object getAsObject(final FacesContext context, final UIComponent comp, final String value)
            throws ConverterException {
        return value;
    }

    public String getAsString(final FacesContext context, final UIComponent component,
                              final Object object) throws ConverterException {

        if (object == null) {
            return null;
        }

        final StringBuilder buff = new StringBuilder("");

        if (object instanceof List) {
            for (Object obj : (List) object) {
                if (obj instanceof AbuseReport) {
                    buff.append(getAsString(context, (AbuseReport) obj)).append("\n");
                }
            }
        } else if (object instanceof AbuseReport) {
            buff.append(getAsString(context, (AbuseReport) object));
        }

        return buff.toString();
    }

}
