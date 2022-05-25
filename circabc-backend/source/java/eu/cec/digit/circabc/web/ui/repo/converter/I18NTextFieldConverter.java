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

import org.alfresco.web.app.Application;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * Convertor for a String property that the value can be translated via the message bundle.
 *
 * @author yanick pignot
 */
public class I18NTextFieldConverter implements Converter {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.I18NTextFieldConverter";

    public Object getAsObject(FacesContext context, UIComponent component, String value)
            throws ConverterException {
        return value;
    }

    @SuppressWarnings("unchecked")
    public String getAsString(FacesContext context, UIComponent component, Object value)
            throws ConverterException {
        if (value instanceof String) {
            String result = (String) value;

            if (!(component instanceof UISelectOne)) {
                final String translation = Application.getMessage(context, ((String) value).toLowerCase());
                if (translation != null && !translation.startsWith("$$")) {
                    result = translation;
                }
            }

            return result;
        } else {
            return "";
        }
    }
}
