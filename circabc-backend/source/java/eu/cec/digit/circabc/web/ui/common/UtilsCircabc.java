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
package eu.cec.digit.circabc.web.ui.common;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.ui.common.Utils;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRendererUtils;

import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * Class containing misc helper methods and variables used by the JSF components.
 *
 * @author Guillaume
 */
public final class UtilsCircabc {

    private static final String PARAMETER_IMAGE_NAME = "imageName";

    /**
     * Les classes utilitaires ne doivent pas avoir de constructeur par defaut ou public.
     */
    private UtilsCircabc() {

    }

    /**
     * Build a context path safe image tag for the supplied image path. Image path should be supplied
     * with a leading slash '/'.
     *
     * @param context    FacesContext
     * @param image      The local image path from the web folder with leading slash '/'
     * @param alt        Required alt text
     * @param title      Required title text
     * @param styleClass class of the compoment
     * @return Populated <code>img</code> tag
     */
    public static String buildImageTag(FacesContext context, String image, String alt, String title,
                                       String styleClass, String imageName) {
        StringBuilder buf = new StringBuilder(128);

        buf.append("<img src=\"").append(context.getExternalContext().getRequestContextPath())
                .append(image).append("\"");

        if (alt != null) {
            alt = Utils.encode(alt);
            buf.append(" alt=\"").append(alt).append("\" title=\"").append(title).append('"');
        }

        if (styleClass != null) {
            styleClass = Utils.encode(styleClass);
            buf.append(" class=\"").append(styleClass).append('"');
        }

        if (imageName != null) {
            imageName = Utils.encode(imageName);
            buf.append(" name=\"").append(imageName).append('"');
        }

        buf.append(" />");

        return buf.toString();
    }

    /**
     * Build a context path safe image tag for the supplied image path. Image path should be supplied
     * with a leading slash '/'.
     *
     * @param context    FacesContext
     * @param image      The local image path from the web folder with leading slash '/'
     * @param alt        Required alt text
     * @param title      Required title text
     * @param styleClass class of the compoment
     * @return Populated <code>img</code> tag
     */
    public static String buildImageTag(FacesContext context, String image, String alt, String title,
                                       String styleClass) {
        return buildImageTag(context, image, alt, title, styleClass, null);
    }

    /**
     * Build a context path safe image tag for the supplied image path. Image path should be supplied
     * with a leading slash '/'.
     *
     * @param context FacesContext
     * @param image   The local image path from the web folder with leading slash '/'
     * @param alt     Required alt/title text
     * @param title   Optional title text
     * @return Populated <code>img</code> tag
     */
    public static String buildImageTag(FacesContext context, String image, String alt, String title) {
        return buildImageTag(context, image, alt, title, null, null);
    }

    /**
     * Build a context path safe image tag for the supplied image path. Image path should be supplied
     * with a leading slash '/'.
     *
     * @param context FacesContext
     * @param image   The local image path from the web folder with leading slash '/'
     * @param alt     Required alt/title text
     * @return Populated <code>img</code> tag
     */
    public static String buildImageTag(FacesContext context, String image, String alt) {
        return buildImageTag(context, image, alt, alt, null, null);
    }

    /**
     * This method create the href part to use in the jsp page.
     *
     * @param context    FacesContext
     * @param component  The component owning the link
     * @param id         The id of the composant too use for identification at the decode process
     * @param attributes The map of the attribute to add to the link as &lt;Attribute, Value&gt;
     * @return the href part to use for the link
     */
    public static String generateHrefPart(FacesContext context, UIComponent component, String id,
                                          Map<String, String> attributes) {

        ViewHandler viewHandler = context.getApplication().getViewHandler();
        String viewId = context.getViewRoot().getViewId();
        String path = viewHandler.getActionURL(context, viewId);

        StringBuilder hrefBuf = new StringBuilder(path);

        // add clientId parameter for decode
        if (path.indexOf('?') == -1) {
            hrefBuf.append('?');
        } else {
            hrefBuf.append("&amp;");
        }
        String hiddenFieldName = HtmlRendererUtils
                .getHiddenCommandLinkFieldName(RendererUtils.findNestingForm(component, context));
        hrefBuf.append(hiddenFieldName);
        hrefBuf.append('=');
        hrefBuf.append(id);

        // handle the paramater list
        for (Map.Entry<String, String> stringStringEntry : attributes.entrySet()) {
            Map.Entry entry = (Map.Entry) stringStringEntry;
            hrefBuf.append("&amp;");
            hrefBuf.append((String) entry.getKey());
            hrefBuf.append('=');
            hrefBuf.append((String) entry.getValue());
        }

        hrefBuf.append("&amp;");
        hrefBuf.append(RendererUtils.findNestingForm(component, context).getFormName());
        hrefBuf.append("_SUBMIT");
        hrefBuf.append('=');
        hrefBuf.append(1);

        StateManager stateManager = context.getApplication().getStateManager();

        if (stateManager.isSavingStateInClient(context)) {
            hrefBuf.append("&amp;");
            hrefBuf.append(WebResourcesCircabc.URL_STATE_MARKER);
        }

        // Migration 3.1 -> 3.4.6 - 06/01/2012
        // Adding this line to correct the improvement "isPostBack" from the newer version of alfresco (3.4.1)
        // Between the 3.1.2 to 3.4.1 of alfresco version, a test has been changed in the class RestoreViewExecutor.java (line 93),
        // uses the isPostBack method
        hrefBuf.append("&amp;").append(WebResourcesCircabc.URL_FIX_ISPOSTBACK);

        return context.getExternalContext().encodeActionURL(hrefBuf.toString());
    }

    /**
     * Crop and encode a label without a SPAN element. The text will only tbe trunckated if too long
     *
     * @param text   to crop and encode
     * @param length length of string to crop
     * @return encoded and cropped text
     */
    public static String cropEncodeLight(String text, int length) {
        if (text.length() > length) {
            String label = text.substring(0, length - 3) + "...";
            return Utils.encode(label);
        } else {
            return Utils.encode(text);
        }
    }

    /**
     * Crop a label without a SPAN element. The text will only tbe trunckated if too long
     *
     * @param text   to crop and encode
     * @param length length of string to crop
     * @return encoded and cropped text
     */
    public static String cropLight(String text, int length) {
        if (text.length() > length) {
            String label = text.substring(0, length - 3) + "...";
            return label;
        } else {
            return text;
        }
    }

    /**
     * Get the first number lines of a String.
     *
     * @param text      The String
     * @param separator The line break
     * @param lineNumer The number of line to return
     */
    public static String getFirstLines(final String text, final String separator,
                                       final int lineNumer) {
        final StringBuilder buff = new StringBuilder(text);

        boolean substring = false;
        final int separatorSize = separator.length();

        int pos = 0;
        int count = 1;
        while (true) {
            pos = buff.indexOf(separator, pos + separatorSize);

            if (pos < 0) {
                break;
            }

            count++;

            if (count >= lineNumer) {
                substring = true;
                break;
            }

        }

        if (substring) {
            return buff.substring(0, pos).concat(" ...");
        } else {
            return text;
        }
    }

    /**
     * For the debug and automatisation purposes, this method generate a name value for an action from
     * a param map.
     */
    public static String buildImageNameFromHref(Map<String, String> linkParams) {
        if (linkParams == null) {
            return null;
        } else {
            return linkParams.get(PARAMETER_IMAGE_NAME);
        }
    }

    /**
     * Get the type of content property for each content type.
     */
    public static QName getPropContent(QName typeQName) {

        return CircabcModel.TYPE_CUSTOMIZATION_CONTENT.equals(typeQName) ?
                CircabcModel.PROP_CONTENT
                : (DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT.equals(typeQName) ?
                DocumentModel.PROP_CONTENT : ContentModel.PROP_CONTENT);
    }
}
