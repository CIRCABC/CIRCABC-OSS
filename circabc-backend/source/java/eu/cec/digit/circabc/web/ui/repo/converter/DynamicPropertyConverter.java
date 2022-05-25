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

import eu.cec.digit.circabc.web.WebClientHelper;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.converter.XMLDateConverter;
import org.alfresco.web.ui.repo.RepoConstants;
import org.owasp.esapi.ESAPI;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import java.util.Date;

/**
 * Convertor for dynamic properties.
 *
 * @author yanick pignot
 */
public class DynamicPropertyConverter implements Converter {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.DynamicPropertyConverter";

    public Object getAsObject(FacesContext context, UIComponent component, String value)
            throws ConverterException {
        return value;
    }

    @SuppressWarnings("unchecked")
    public String getAsString(FacesContext context, UIComponent component, Object value)
            throws ConverterException {
        boolean isDate = false;
        if (value instanceof Date) {
            isDate = true;
        } else if (value instanceof String) {
            try {
                ISO8601DateFormat.parse((String) value);
                isDate = true;
            } catch (Exception ex) {
                isDate = false;
            }
        }

        if (isDate) {
            return getDateConverter(context).getAsString(context, component, value.toString());
        } else {
            final Boolean escape = (Boolean) component.getAttributes().get("escape");
            if (escape != null && escape) {
                return value == null ? "" : value.toString();
            } else {
                return value == null ? "" : ESAPI.encoder().encodeForHTML(value.toString());
            }

        }
    }

    /**
     * Retrieves the default converter for the date component
     *
     * @param context FacesContext
     * @return XMLDateConverter
     */
    protected Converter getDateConverter(FacesContext context) {
        XMLDateConverter converter = (XMLDateConverter) context.getApplication().
                createConverter(RepoConstants.ALFRESCO_FACES_XMLDATE_CONVERTER);
        converter.setType("date");
        converter.setPattern(Application.getMessage(context, WebClientHelper.MSG_DATE_PATTERN));
        return converter;
    }
}
