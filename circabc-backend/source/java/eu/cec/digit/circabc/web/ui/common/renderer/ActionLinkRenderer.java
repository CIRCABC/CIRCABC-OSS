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

import eu.cec.digit.circabc.web.ui.common.UtilsCircabc;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.renderer.BaseRenderer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.renderkit.JSFAttr;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.*;
import org.apache.myfaces.shared_impl.renderkit.html.util.FormInfo;
import org.owasp.esapi.ESAPI;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author Guillaume
 */
public class ActionLinkRenderer extends BaseRenderer {
    // ------------------------------------------------------------------------------
    // Renderer implementation

    private static final Log logger = LogFactory.getLog(ActionLinkRenderer.class);

    /**
     * Build the |&lt;a href [class=""] id="" &gt;[img and/or text]|
     *
     * @param context   The FacesContext
     * @param component The component
     * @param clientId  The clientId of the component
     */
    public static void renderCommandLinkStart(FacesContext context, UIComponent component,
                                              String clientId) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("ActionLink : encodeBegin - renderCommandLinkStart - Start");
        }
        final UIActionLink link = (UIActionLink) component;
        final Map attrs = link.getAttributes();

        // To handle the no output tag
        ResponseWriter writer;
        if (link.getNoDisplay()) {
            writer = new HtmlResponseWriterImpl(new StringWriter(), null, null);
        } else {
            writer = context.getResponseWriter();
        }

        writer.startElement(HTML.ANCHOR_ELEM, component);
        // Build the href part
        renderNonJavaScriptAnchorStart(context, writer, component, clientId);
        // Render other attributes
        if (attrs.get("id") != null) {
            writer.writeAttribute(HTML.ID_ATTR, attrs.get("id"), null);
        } else {
            writer.writeAttribute(HTML.ID_ATTR, clientId, null);
        }
        if (attrs.get("styleClass") != null) {
            writer.writeAttribute(HTML.CLASS_ATTR, attrs.get("styleClass"), null);
        }
        if (attrs.get("target") != null) {
            writer.writeAttribute(HTML.TARGET_ATTR, attrs.get("target"), null);
        }
        if (attrs.get("onclick") != null) {
            writer.writeAttribute(HTML.ONCLICK_ATTR, attrs.get("onclick"), null);
        }
        writer.writeAttribute(HTML.TITLE_ATTR, attrs.get("tooltip"), null);

        if (link.getImage() != null) {
            String imageName = UtilsCircabc.buildImageNameFromHref(getParameterComponents(link));

            if (imageName == null) {
                imageName = link.getId();
            }

            String image = link.getImage();
            String tooltip = (String) attrs.get("tooltip");
            writer.write(UtilsCircabc.buildImageTag(context, image, tooltip, tooltip, null, imageName));
            if (link.getShowLink()) {
                for (int x = 0; x < link.getPadding(); ++x) {
                    writer.write("&nbsp;");
                }

                // Image + text -> Append the value
                writer.writeText(link.getValue(), JSFAttr.VALUE_ATTR);
            }
        } else {
            if (link.getValue() == null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("The value of a link can't be null. Id: " + link.getId());
                }
                //throw new NullPointerException("The value of a link can't be null. Id: " + link.getId());
                writer.write("null");
            } else {
                // writer.writeText(link.getValue(), JSFAttr.VALUE_ATTR);
                if (link.getEscape() != null) {
                    if (!link.getEscape()) {
                        writer.write(link.getValue().toString());
                    } else {
                        writer.write(ESAPI.encoder().encodeForHTML(link.getValue().toString()));
                    }
                } else {
                    writer.write(link.getValue().toString());
                }

            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("ActionLink : encodeBegin - renderCommandLinkStart - End");
        }
    }

    /**
     * Render the href part of the tag
     *
     * @param facesContext The FacesContext
     * @param writer       The Response writer
     * @param component    The component
     * @param clientId     The clientId of the component
     * @return href part of tag
     */
    protected static String renderNonJavaScriptAnchorStart(FacesContext facesContext,
                                                           ResponseWriter writer, UIComponent component, String clientId) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info(
                    "ActionLink : encodeBegin - renderCommandLinkStart - renderNonJavaScriptAnchorStart - Start");
        }
        final ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        final String viewId = facesContext.getViewRoot().getViewId();
        final String path = viewHandler.getActionURL(facesContext, viewId);

        final UIActionLink link = (UIActionLink) component;
        final Map attrs = link.getAttributes();

        String href;

        /*
         * if href's attribute exists, threre is three cases : - #location -> Don't touch the href - /location -> Add request location - location -> Don't touch the href
         */
        href = (String) attrs.get("href");
        if (href != null && href.length() > 0) {
            if (href.charAt(0) == '/') {
                // Add request context path
                href = facesContext.getExternalContext().getRequestContextPath() + '/' + href.substring(1);
            }
        } else {
            StringBuffer hrefBuf = new StringBuffer(path);
            logger.info("hrefBuf -> path |" + hrefBuf + "|");

            // add clientId parameter for decode

            if (path.indexOf('?') == -1) {
                hrefBuf.append('?');
            } else {
                hrefBuf.append("&amp;");
            }
            final FormInfo formInfo = RendererUtils.findNestingForm(component, facesContext);
            String hiddenFieldName = HtmlRendererUtils.getHiddenCommandLinkFieldName(formInfo);
            if (logger.isInfoEnabled()) {
                logger.info("hiddenFieldName |" + hiddenFieldName + "|");
            }
            hrefBuf.append(hiddenFieldName);
            hrefBuf.append('=');
            if (logger.isInfoEnabled()) {
                logger.info("clientId |" + clientId + "|");
            }
            hrefBuf.append(clientId);

            hrefBuf.append("&amp;");
            hrefBuf.append(formInfo.getFormName());
            hrefBuf.append("_SUBMIT");
            hrefBuf.append('=');
            hrefBuf.append(1);

            Map childParams = getParameterComponents(component);
            if (childParams != null) {
                addChildParametersToHref(component, hrefBuf, writer.getCharacterEncoding());
            }

            /*
             * do this always - even with server-side-state saving if ( stateManager.isSavingStateInClient(facesContext) ) {
             */

            hrefBuf.append("&amp;");
            if (logger.isInfoEnabled()) {
                logger.info("isSavingStateInClient |" + HtmlLinkRendererBase.URL_STATE_MARKER + "|");
            }
            hrefBuf.append(HtmlLinkRendererBase.URL_STATE_MARKER);

            // end of if ( stateManager.isSavingStateInClient(facesContext) )
            // not clear why they are doing this

            if (attrs.get("anchor") != null) {
                // Add an "n" to respect spec which anchor don't have to start with an number
                hrefBuf.append("#n");
                hrefBuf.append(attrs.get("anchor"));
            }

            href = hrefBuf.toString();
        }
        if (logger.isInfoEnabled()) {
            logger.info("href |" + href + "|");
        }
        href = facesContext.getExternalContext().encodeActionURL(href);
        if (logger.isInfoEnabled()) {
            logger.info("href encode |" + href + "|");
        }

//		 writeURIAttribute in version 1.1.5 was not writing encoded value
//		 but in version 1.1.8 is writing encoded value
//		 as download URLs are already encoded we decode them so filename is OK
//		 see https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2224
        String root = facesContext.getExternalContext().getRequestContextPath();
		/*if (href.startsWith(root + "/d/") || href.startsWith(root +"/sd/"))
		{
			href = URLDecoder.decode(href,"UTF-8");
		}*/

        writer.writeURIAttribute(HTML.HREF_ATTR, href, null);
        if (logger.isInfoEnabled()) {
            logger.info(
                    "ActionLink : encodeBegin - renderCommandLinkStart - renderNonJavaScriptAnchorStart - End");
        }

        return href;
    }

    /**
     * Add the childs parameters &lt;f:param&gt; to url
     *
     * @param linkComponent Parent Component
     * @param hrefBuf       StringBuffer to fill
     * @param charEncoding  Character encoding
     */
    private static void addChildParametersToHref(UIComponent linkComponent, StringBuffer hrefBuf,
                                                 String charEncoding) throws IOException {
        Map paramMap = getParameterComponents(linkComponent);
        for (Object o : paramMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            addParameterToHref(name, value, hrefBuf, charEncoding);
        }
    }

    /**
     * Add the specified child parameter &lt;f:param&gt; to url
     *
     * @param name         Name of the parameter
     * @param value        Value of the parameter
     * @param hrefBuf      StringBuffer to fill
     * @param charEncoding Character encoding
     */
    private static void addParameterToHref(String name, Object value, StringBuffer hrefBuf,
                                           String charEncoding) throws UnsupportedEncodingException {
        if (name == null) {
            throw new IllegalArgumentException(
                    "Unnamed parameter value not allowed within command link.");
        }

        hrefBuf.append("&amp;");
        hrefBuf.append(URLEncoder.encode(name, charEncoding));
        hrefBuf.append('=');
        if (value != null) {
            // UIParameter is no ConvertibleValueHolder, so no conversion possible
            hrefBuf.append(URLEncoder.encode(value.toString(), charEncoding));
        }
    }

    /**
     * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    @Override
    public void decode(FacesContext context, UIComponent component) {

        String clientId = component.getClientId(context);
        if (clientId.contains("_________")) {
            // Sub link -> feinte power
            clientId = clientId.substring(0, clientId.indexOf("_________"));
        }
        FormInfo formInfo = RendererUtils.findNestingForm(component, context);
        String reqValue = (String) context.getExternalContext().getRequestParameterMap()
                .get(HtmlRendererUtils.getHiddenCommandLinkFieldName(formInfo));
        if ((reqValue != null) && (reqValue.contains("_________"))) {
            // Sub link -> feinte power
            reqValue = reqValue.substring(0, reqValue.indexOf("_________"));
        }
        if (logger.isInfoEnabled()) {
            logger.info("decodeActionLink " + HtmlRendererUtils.getHiddenCommandLinkFieldName(formInfo));
        }
        if (reqValue != null && reqValue.equals(clientId)) {
            // Get all the params for this actionlink, see if any values have been set
            // on the request which match our params and set them into the component
            UIActionLink link = (UIActionLink) component;
            Map<String, String> childParams = getParameterComponents(component);
            Map<String, String> destParams = link.getParameterMap();
            Map<String, String> actionParams = getParameterComponents(link);
            // Add the param from myfaces tree
            if (childParams != null) {
                for (Map.Entry<String, String> entry : childParams.entrySet()) {
                    destParams.put(entry.getKey(), entry.getValue());
                }
            }
            // Add param from url - Has priority upon the one from myfaces tree
            if (actionParams != null) {
                for (String name : actionParams.keySet()) {
                    Map requestMap = context.getExternalContext().getRequestParameterMap();
                    String paramValue = (String) requestMap.get(name);
                    // If null, we don't overwrite the previous one
                    if (paramValue != null) {
                        destParams.put(name, paramValue);
                    }
                }
            }

            component.queueEvent(new ActionEvent(component));
            RendererUtils.initPartialValidationAndModelUpdate(component, context);
        }
    }

    /**
     * Launch the rendering of the part of the component (not the end tag)
     */
    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent component) throws IOException {
        // always check for this flag - as per the spec
        if (!component.isRendered()) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("ActionLink : encodeBegin - Start");
        }
        renderCommandLinkStart(facesContext, component, component.getClientId(facesContext));
        if (logger.isInfoEnabled()) {
            logger.info("ActionLink : encodeBegin - End");
        }
    }

    /**
     * Add the &lt;/a&gt; and the hidden required field if necessary
     *
     * @see javax.faces.render.Renderer#encodeEnd(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        // always check for this flag - as per the spec
        if (!component.isRendered()) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("ActionLink : encodeEnd - Start");
        }

        UIActionLink link = (UIActionLink) component;
        if (!link.getNoDisplay()) {
            ResponseWriter writer = context.getResponseWriter();
            writer.writeText("", null);
            writer.endElement(HTML.ANCHOR_ELEM);
        }
        // force separate end tag

        HtmlFormRendererBase.renderScrollHiddenInputIfNecessary(
                RendererUtils.findNestingForm(component, context).getForm(), context,
                context.getResponseWriter());
        if (logger.isInfoEnabled()) {
            logger.info("ActionLink : encodeEnd - End");
        }
    }
}
