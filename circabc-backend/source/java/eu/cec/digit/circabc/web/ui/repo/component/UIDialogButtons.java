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
package eu.cec.digit.circabc.web.ui.repo.component;


import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.Utils;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

/**
 * WAI. Component that displays the buttons for a dialog.
 * <p>
 * The standard <code>OK</code> and <code>Cancel</code> buttons are always generated. Any additional
 * buttons, either configured or generated dynamically by the dialog, are generated in between the
 * standard buttons.
 *
 * @author patrice.coppens@trasys.lu
 */
public class UIDialogButtons extends
        org.alfresco.web.ui.repo.component.UIDialogButtons {


    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }

        if (this.getChildCount() == 0) {
            // generate all the required buttons the first time
            generateAdditionalButtons(context);
        }

        ResponseWriter out = context.getResponseWriter();
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }

        ResponseWriter out = context.getResponseWriter();

        // render the buttons
        for (Object o : getChildren()) {

            UIComponent child = (UIComponent) o;
            Utils.encodeRecursive(context, child);
        }
    }

    @SuppressWarnings("unchecked")
    protected void addSpacingRow(FacesContext context) {
        UIOutput spacingRow = (UIOutput) context.getApplication().createComponent(
                ComponentConstants.JAVAX_FACES_OUTPUT);
        spacingRow.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
        FacesHelper.setupComponentId(context, spacingRow, null);
        spacingRow.setValue("<br>");
        spacingRow.getAttributes().put("escape", Boolean.FALSE);
        this.getChildren().add(spacingRow);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }

        ResponseWriter out = context.getResponseWriter();
    }

}
