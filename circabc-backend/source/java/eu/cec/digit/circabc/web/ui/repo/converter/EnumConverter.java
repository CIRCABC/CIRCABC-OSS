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
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Convertor for any enumeration.
 *
 * @author yanick pignot
 */
public class EnumConverter implements Converter {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.EnumConverter";

    /**
     * Util method that convert a list of enumeration in a translated list of selectable item.
     * <b>
     * Don't forget to set the converter to the component:
     * <code>
     * <h:selectOneMenu value="#{Bean.myEnumeration}" converter="eu.cec.digit.circabc.faces.EnumConverter">
     * <f:selectItems value="#{Bean.myEnumerations}"/> </h:selectOneMenu>
     * </code>
     * </b>
     *
     * @param context       The faces context
     * @param enums         All enumeration to convert into SelectItem Enum.values()
     * @param messagePrefix The I18N message prefix. Can be empty. If null no message will appears to
     *                      screen
     * @return The list of Select item redy to be inserted to a Select Item JSF Component.
     */
    public static List<SelectItem> convertEnumToSelectItem(final FacesContext context,
                                                           final Enum[] enums, final String messagePrefix) {
        final List<SelectItem> items = new ArrayList<>(enums.length);
        String message = null;

        for (Enum enumItem : enums) {
            if (messagePrefix == null) {
                message = "";
            } else {
                message = Application.getMessage(context, messagePrefix + enumItem.name().toLowerCase());
            }

            items.add(new SelectItem(enumItem, message));
        }

        return items;
    }

    @SuppressWarnings("unchecked")
    public Object getAsObject(final FacesContext context, final UIComponent comp, final String value)
            throws ConverterException {
        final Class enumType = comp.getValueBinding("value").getType(context);
        return Enum.valueOf(enumType, value);
    }

    public String getAsString(final FacesContext context, final UIComponent component,
                              final Object object) throws ConverterException {

        if (object == null) {
            return null;
        }

        // allow to hard code the name of the enumeration
        if (object instanceof String) {
            return (String) object;
        }

        final Enum type = (Enum) object;
        return type.toString();
    }

}
