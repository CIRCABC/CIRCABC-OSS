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

import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv;
import eu.cec.digit.circabc.web.Services;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * Convertor for a user id. Display FIRST_NAME LAST_NAME instand of the non user friendly 'userId'.
 *
 * @author yanick pignot
 */
public class UseridConverter implements Converter {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.UseridConverter";

    public Object getAsObject(FacesContext context, UIComponent component, String value)
            throws ConverterException {
        return value;
    }

    @SuppressWarnings("unchecked")
    public String getAsString(FacesContext context, UIComponent component, Object value)
            throws ConverterException {
        final String userName = getString(value);
        if (userName != null) {

            final UserDetailsBusinessSrv userDetServ = Services.getBusinessRegistry(context)
                    .getUserDetailsBusinessSrv();

            String disaplyName = null;

            try {
                final UserDetails details = userDetServ.getUserDetails(userName);
                disaplyName = getTextContent(details);

            } catch (Throwable t) {
                // no problem, user doen'st exist
            }

            if (disaplyName != null) {
                return disaplyName;
            } else {
                return userName;
            }
        } else {
            return "";
        }
    }


    protected String getTextContent(final UserDetails userDetails) {
        return userDetails.getFullName();
    }


    private String getString(Object value) {
        if (value != null && value instanceof String) {
            final String str = ((String) value).trim();

            if (str.length() > 0) {
                return str;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


}
