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

import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import java.util.Locale;

/**
 * Convertor for any locale.
 *
 * @author yanick pignot
 */
public class LocaleConverter implements Converter {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.LocaleConverter";


    public Object getAsObject(final FacesContext context, final UIComponent comp, final String value)
            throws ConverterException {
        return I18NUtil.parseLocale(value);
    }

    public String getAsString(final FacesContext context, final UIComponent component,
                              final Object object) throws ConverterException {

        if (object == null) {
            return null;
        } else if (object instanceof String) {
            // allow to hard code the locale
            return (String) object;
        } else if (object instanceof Locale) {
            return ((Locale) object).getLanguage();
        } else {
            return object.toString();
        }
    }
}
