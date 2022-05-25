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
package eu.cec.digit.circabc.web.wai.generator;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.generator.TextFieldGenerator;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.util.List;

/**
 * Generates a specify keyword on a document generator.
 *
 * @author Yanick Pignot
 */
public class I18NTextFieldGenerator extends TextFieldGenerator {

    @SuppressWarnings("unchecked")
    @Override
    protected UIComponent createComponent(FacesContext context, UIPropertySheet propertySheet,
                                          PropertySheetItem item) {
        final UIComponent component = super.createComponent(context, propertySheet, item);

        if (component instanceof UISelectOne) {
            final UISelectOne select = (UISelectOne) component;
            final UISelectItems items = (UISelectItems) select.getChildren().get(0);
            final List<SelectItem> selectItems = (List<SelectItem>) items.getValue();
            String translation = null;
            for (SelectItem selectItem : selectItems) {
                translation = Application.getMessage(context, selectItem.getLabel().toLowerCase());
                if (translation != null && !translation.startsWith("$$")) {
                    selectItem.setLabel(translation);
                }
            }
        }

        return component;
    }
}
