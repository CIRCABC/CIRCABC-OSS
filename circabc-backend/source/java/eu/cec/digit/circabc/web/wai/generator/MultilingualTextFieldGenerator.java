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


import eu.cec.digit.circabc.web.ui.repo.RepoConstants;
import org.alfresco.web.bean.generator.TextFieldGenerator;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;

/**
 * WAI Generates a multilingual text field component.
 *
 * @author patrice.coppens@trasys.lu
 * @deprecated this generator can be replaced by TranslatePropertyGenerator
 */
@Deprecated
public class MultilingualTextFieldGenerator extends TextFieldGenerator {

    public static final String BEAN_NAME = "CircabcMultilingualTextFieldGenerator";

    @Override
    public UIComponent generateAndAdd(FacesContext context, UIPropertySheet propertySheet,
                                      PropertySheetItem item) {
        UIComponent component = super.generateAndAdd(context, propertySheet, item);

        if (!(component instanceof UISelectOne)) {
            component.setRendererType(RepoConstants.CIRCABC_FACES_MLTEXT_RENDERER);
        }

        return component;
    }
}
