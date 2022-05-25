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
package eu.cec.digit.circabc.web.ui.repo.renderer;

import org.alfresco.web.app.Application;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

/**
 * Renders a WAI multilingual text field.
 * <p>
 * Renders the default output followed by an icon to represent multilingual properties.
 * </p>
 *
 * @author patrice.coppens@trasys.lu
 */
public class MultilingualTextRenderer extends HtmlTextRenderer {

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException {
        super.encodeEnd(facesContext, component);

        String tooltip = Application.getMessage(facesContext, "marker_tooltip");
        ResponseWriter out = facesContext.getResponseWriter();
        out.write("<img src='");
        out.write(facesContext.getExternalContext().getRequestContextPath());
        out.write("/images/icons/multilingual_marker.gif' title='");
        out.write(tooltip);
        out.write("' alt='");
        out.write(tooltip);
        out.write("' style='margin-left: 6px; vertical-align: -4px; _vertical-align: -2px;' />");
    }
}
