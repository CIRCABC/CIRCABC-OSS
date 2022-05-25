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
package eu.cec.digit.circabc.web.ui.common.renderer;

import eu.cec.digit.circabc.web.ui.common.WebResourcesCircabc;
import eu.cec.digit.circabc.web.ui.common.component.UIErrors;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.renderer.BaseRenderer;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Renderer that displays any errors that occurred in the previous lifecycle processing
 *
 * @author Guillaume
 */
public class ErrorsRenderer extends BaseRenderer {

    protected static final String FORCED_MESSAGES = "_Circabc__forced_messages";

    @SuppressWarnings("unchecked")
    public static void addForcedMessage(FacesMessage message) {
        if (message == null) {
            throw new NullPointerException("A message is mandatory");
        }

        List<FacesMessage> messagesAsList = getForcedMessages();
        if (messagesAsList == null) {
            messagesAsList = new ArrayList<>(3);
        }

        messagesAsList.add(message);

        FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                .put(FORCED_MESSAGES, messagesAsList);
    }

    @SuppressWarnings("unchecked")
    public static void addForcedMessage(Iterator<FacesMessage> messages) {
        if (messages == null) {
            throw new NullPointerException("At least a message is mandatory");
        }

        List<FacesMessage> messagesAsList = getForcedMessages();
        if (messagesAsList == null) {
            messagesAsList = new ArrayList<>(3);
        }

        while (messages.hasNext()) {
            messagesAsList.add(messages.next());
        }

        FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                .put(FORCED_MESSAGES, messagesAsList);
    }

    @SuppressWarnings("unchecked")
    protected static List<FacesMessage> getForcedMessages() {
        return (List<FacesMessage>) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get(FORCED_MESSAGES);
    }

    protected static void removeForcedMessages() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(FORCED_MESSAGES);
    }

    /**
     * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException {
        if (component.isRendered() == false) {
            return;
        }

        Iterator messages = null;

        List<FacesMessage> forcedMessages = getForcedMessages();
        if (forcedMessages != null && forcedMessages.size() > 0) {
            messages = forcedMessages.iterator();
        }

        if (messages == null) {
            messages = context.getMessages();
        }

        if (messages.hasNext()) {
            ResponseWriter out = context.getResponseWriter();
            ResourceBundle bundle = (ResourceBundle) Application.getBundle(context);
            UIErrors errors = (UIErrors) component;
            String styleClass = errors.getStyleClass();
            String errorClass = errors.getErrorClass();
            String infoClass = errors.getInfoClass();
            String warnClass = errors.getWarnClass();
            String path = context.getExternalContext().getRequestContextPath();
            // For perf tune
            if (errorClass == null) {
                errorClass = styleClass;
            }
            if (infoClass == null) {
                infoClass = styleClass;
            }
            if (warnClass == null) {
                warnClass = styleClass;
            }

            out.write("<div");
            outputAttribute(out, styleClass, "class");
            out.write(">");

            while (messages.hasNext()) {
                FacesMessage fm = (FacesMessage) messages.next();
                out.write("<div");
                if (fm.getSeverity() == FacesMessage.SEVERITY_INFO) {
                    outputAttribute(out, infoClass, "class");
                    out.write("><img src=\"");
                    out.write(path + WebResourcesCircabc.IMAGE_MESSAGE_INFO);
                    out.write("\"");
                    outputAttribute(out, bundle.getString("message_info_tooltip"), "alt");
                    outputAttribute(out, infoClass, "class");
                    out.write(" /><span");
                    outputAttribute(out, infoClass, "class");
                } else if (fm.getSeverity() == FacesMessage.SEVERITY_WARN) {
                    outputAttribute(out, warnClass, "class");
                    out.write("><img src=\"");
                    out.write(path + WebResourcesCircabc.IMAGE_MESSAGE_WARN);
                    out.write("\"");
                    outputAttribute(out, bundle.getString("message_warn_tooltip"), "alt");
                    outputAttribute(out, warnClass, "class");
                    out.write(" /><span");
                    outputAttribute(out, warnClass, "class");
                } else if (fm.getSeverity() == FacesMessage.SEVERITY_ERROR
                        || fm.getSeverity() == FacesMessage.SEVERITY_FATAL) {
                    outputAttribute(out, errorClass, "class");
                    out.write("><img src=\"");
                    out.write(path + WebResourcesCircabc.IMAGE_MESSAGE_ERROR);
                    out.write("\"");
                    outputAttribute(out, bundle.getString("message_error_tooltip"), "alt");
                    outputAttribute(out, errorClass, "class");
                    out.write(" /><span");
                    outputAttribute(out, errorClass, "class");
                } else {
                    out.write("><span");
                    outputAttribute(out, styleClass, "class");
                }
                out.write("> ");

                if (errors.getEscape()) {
                    out.write(Utils.encode(fm.getDetail()));
                } else {
                    out.write(fm.getDetail());
                }

                out.write("</span></div>");
            }

            out.write("</div>");
        }

        // ensure to empty the map
        removeForcedMessages();
    }

    /**
     * @see javax.faces.render.Renderer#getRendersChildren()
     */
    public boolean getRendersChildren() {
        return false;
    }
}
