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
package eu.cec.digit.circabc.web.ui.repo.renderer.property;


import eu.cec.digit.circabc.web.ui.common.UtilsCircabc;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * WAI-A Renderer for a PropertySheetItem component.
 *
 * @author patrice.coppens@trasys.lu
 */
public class PropertySheetItemRenderer extends
        org.alfresco.web.ui.repo.renderer.property.PropertySheetItemRenderer {

    public static final String MANDATORY_MARKER = "/images/icons/required_field.gif";

    /**
     * @see javax.faces.render.Renderer#encodeChildren(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered()) {
            return;
        }

        ResponseWriter out = context.getResponseWriter();

        // make sure there are 2 or 3 child components
        int count = component.getChildCount();

        if (count == 2 || count == 3) {
            // get the label and the control
            List<UIComponent> children = component.getChildren();
            UIComponent label = children.get(0);
            UIComponent control = children.get(1);

            // place a style class on the label column if necessary
            String labelStylceClass = (String) component.getParent().getAttributes()
                    .get("labelStyleClass");
            out.write("<th ");
            if (labelStylceClass != null) {
                outputAttribute(out, labelStylceClass, "class");
            }

            // close the <td>
            out.write(">");

            // encode the mandatory marker component if present
            if (count == 3) {
                ResourceBundle bundle = (ResourceBundle) Application.getBundle(context);

                out.write(
                        UtilsCircabc.buildImageTag(context, MANDATORY_MARKER, bundle.getString("mandatory")));
                out.write(" ");
            }

            // encode the label
            Utils.encodeRecursive(context, label);
            // encode the control
            out.write("</th><td align=\"left\" >");

            if (control instanceof UIOutput || control instanceof UIInput) {
                control.getAttributes().put("escape", Boolean.TRUE);
            }

            Utils.encodeRecursive(context, control);

            // NOTE: we'll allow the property sheet's grid renderer close off the last <td>
        }
    }
}
