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

import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.generator.BaseComponentGenerator;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;
import org.springframework.extensions.webscripts.ui.common.ConstantMethodBinding;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Base generator can be used in the edit properties dialog to launch a dialog. The property will be
 * read only.
 * <p>
 * The dialog called will be setted with the current node id as parameter.
 *
 * @author Yanick Pignot
 */
public abstract class BaseDialogLauncherGenerator extends BaseComponentGenerator {

    public static final String PARAM_PROPERTY_KEY = "propertyKey";
    protected final static String ACTION_LISTENER = "#{WaiDialogManager.setupParameters}";
    protected static final String ATTRIBUTE_STYLE_CLASS = "styleClass";
    final static Class ACTION_CLASS_ARGS[] = {javax.faces.event.ActionEvent.class};
    private Node node;

    @SuppressWarnings("unchecked")
    public UIComponent generate(FacesContext context, String id) {
        // create a panel component
        UIComponent panel = context.getApplication()
                .createComponent("eu.cec.digit.circabc.faces.Panel");
        FacesHelper.setupComponentId(context, panel, id + "-panel");

        String propAsString = getTextContent(context, id);
        if (propAsString == null) {
            propAsString = "";
        }

        panel.getChildren().add(buildTextField(context, id, propAsString));

        UICommand picker = buildActionCommand(context, id);

        final List<UIParameter> parameters = getActionParameters(context, id);

        for (final UIParameter parameter : parameters) {
            picker.getChildren().add(parameter);
        }

        panel.getChildren().add(picker);

        return panel;
    }

    protected List<UIParameter> getActionParameters(final FacesContext context, final String id) {
        //	add the parameter id to the dialog
        UIParameter parmeterNodeId = (UIParameter) context.getApplication()
                .createComponent(UIParameter.COMPONENT_TYPE);
        FacesHelper.setupComponentId(context, parmeterNodeId, id + "-id-parameter");
        parmeterNodeId.setName(BaseWaiDialog.NODE_ID_PARAMETER);
        parmeterNodeId.setValue(getNode().getId());

        //add the parameter id to the dialog
        UIParameter parmeterPropId = (UIParameter) context.getApplication()
                .createComponent(UIParameter.COMPONENT_TYPE);
        FacesHelper.setupComponentId(context, parmeterPropId, id + "-id-property");
        parmeterPropId.setName(PARAM_PROPERTY_KEY);
        parmeterPropId.setValue(id);

        final List<UIParameter> parameters = new ArrayList<>(4);
        parameters.add(parmeterNodeId);
        parameters.add(parmeterPropId);

        return parameters;
    }

    protected UICommand buildActionCommand(final FacesContext context, final String id) {
        //add it a launch dialog button
        HtmlCommandButton picker = (HtmlCommandButton) context.getApplication()
                .createComponent(HtmlCommandButton.COMPONENT_TYPE);
        picker.setRendererType(ComponentConstants.JAVAX_FACES_BUTTON);
        FacesHelper.setupComponentId(context, picker, id + "-launcher");
        picker.setValue(getActionValue(context, id));
        picker.setTitle(getActionTooltip(context, id));
        picker.setAction(new ConstantMethodBinding(getAction(context, id)));
        picker.setActionListener(
                context.getApplication().createMethodBinding(ACTION_LISTENER, ACTION_CLASS_ARGS));

        return picker;
    }

    protected UIComponent buildTextField(final FacesContext context, final String id,
                                         final String propAsString) {
        // add it a text read only text field
        UIOutput textField = (UIOutput) context.getApplication().createComponent(
                ComponentConstants.JAVAX_FACES_OUTPUT);
        textField.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);

        textField.setValue(propAsString);
        FacesHelper.setupComponentId(context, textField, id + "-value");

        return textField;
    }

    protected abstract String getTextContent(FacesContext context, String id);

    protected abstract String getAction(FacesContext context, String id);

    protected abstract String getActionTooltip(FacesContext context, String id);

    protected abstract String getActionValue(FacesContext context, String id);

    @Override
    protected UIComponent setupMultiValuePropertyIfNecessary(FacesContext context,
                                                             UIPropertySheet propertySheet, PropertySheetItem property, PropertyDefinition propertyDef,
                                                             UIComponent component) {
        // don't set this component being multivalued
        return component;
    }

    @Override
    protected UIComponent createComponent(FacesContext context, UIPropertySheet propertySheet,
                                          PropertySheetItem item) {

        this.node = propertySheet.getNode();
        return super.createComponent(context, propertySheet, item);
    }

    protected Node getNode() {
        return node;
    }

    protected void setNode(Node node) {
        this.node = node;
    }
}
