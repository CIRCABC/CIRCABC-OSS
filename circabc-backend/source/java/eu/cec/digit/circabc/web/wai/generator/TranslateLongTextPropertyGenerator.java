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

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import java.util.List;

/**
 * Generates a picker to add translation to a property in a TextArea instand of a simple text panel
 *
 * @author Yanick Pignot
 */
public class TranslateLongTextPropertyGenerator extends TranslatePropertyGenerator {

    public static final String PARAM_PROPERTY_TEXT_AREA = "textArea";
    private static final int ROWS = 3;
    private static final int COLUMNS = 32;
    private static final String ATTRIBUTE_ROWS = "rows";
    private static final String ATTRIBUTE_COLUMNS = "cols";

    @SuppressWarnings("unchecked")
    @Override
    protected UIComponent buildTextField(final FacesContext context, final String id,
                                         final String propAsString) {
        final UIComponent component = super.buildTextField(context, id, propAsString);

        component.getAttributes().put(ATTRIBUTE_ROWS, ROWS);
        component.getAttributes().put(ATTRIBUTE_COLUMNS, COLUMNS);
        component.setRendererType(ComponentConstants.JAVAX_FACES_TEXTAREA);

        return component;

    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.generator.BaseDialogLauncherGenerator#getActionParameters(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    protected List<UIParameter> getActionParameters(final FacesContext context, final String id) {
        final List<UIParameter> parameters = super.getActionParameters(context, id);

        //add the parameter id to the dialog
        UIParameter parmeterTextArea = (UIParameter) context.getApplication()
                .createComponent(UIParameter.COMPONENT_TYPE);
        FacesHelper.setupComponentId(context, parmeterTextArea, id + "-id-textArea");
        parmeterTextArea.setName(PARAM_PROPERTY_TEXT_AREA);
        parmeterTextArea.setValue(Boolean.TRUE);

        parameters.add(parmeterTextArea);

        return parameters;
    }

}
