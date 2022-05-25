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
package eu.cec.digit.circabc.web.ui.renderer;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.ui.common.WebResourcesCircabc;
import eu.cec.digit.circabc.web.ui.component.UINavigationList;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.renderer.BaseRenderer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.util.FormInfo;
import org.owasp.esapi.ESAPI;

import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Renderer a navigation list with clickable element
 *
 * @author Guillaume
 */
public class NavigationListRenderer extends BaseRenderer {

    /**
     * The delfault separator is not specified
     */
    public static final String DEFAULT_SEPARATOR = " > ";

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(NavigationListRenderer.class);

    /**
     * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    public void decode(final FacesContext context, final UIComponent component) {

        final String clientId = component.getId();
        final FormInfo formInfo = RendererUtils.findNestingForm(component, context);

        if (formInfo == null) {
            return;
        }

        final String reqValue = (String) context.getExternalContext().getRequestParameterMap()
                .get(HtmlRendererUtils.getHiddenCommandLinkFieldName(formInfo));
        if (logger.isInfoEnabled()) {
            logger.info("decodeActionLink " + HtmlRendererUtils.getHiddenCommandLinkFieldName(formInfo));
        }
        if (reqValue != null && reqValue.equals(clientId)) {

            // Get all the params for this actionlink, see if any values have
            // been set on the request which match our params and set them into
            // the component
            final UINavigationList navigationList = (UINavigationList) component;
            final Map requestMap = context.getExternalContext().getRequestParameterMap();
            final String paramValue = (String) requestMap.get("id");
            // We use the attribute id to transmit data
            navigationList.setId("n" + paramValue);

            component.queueEvent(new ActionEvent(component));
            RendererUtils.initPartialValidationAndModelUpdate(component, context);
        }
    }

    /**
     * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    public void encodeBegin(final FacesContext context, final UIComponent component)
            throws IOException {
        if (component.isRendered() == false) {
            return;
        }

        String userName =
                AuthenticationUtil.getRunAsUser() == null ? AuthenticationUtil.getGuestUserName()
                        : AuthenticationUtil.getRunAsUser();

        try {
            AuthenticationUtil.setRunAsUserSystem();

            final ResponseWriter out = context.getResponseWriter();
            final UINavigationList navigationList = (UINavigationList) component;

            final String id = navigationList.getId();
            final String finalSeparator = navigationList.getSeparator();
            final String separatorClass = navigationList.getSeparatorClass();
            final String styleClass = navigationList.getStyleClass();
            final boolean separatorFirst = navigationList.getSeparatorFirst();
            final String onclick = navigationList.getOnclick();
            final String bannerStyle = navigationList.getBannerStyle();
            final String renderPropertyName = navigationList.getRenderPropertyName();

            try {
                final RetryingTransactionHelper txnHelper = Repository
                        .getRetryingTransactionHelper(FacesContext.getCurrentInstance());
                final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        final List value = navigationList.getValue();

                        String separator = finalSeparator;

                        // Get the default separator if not specified
                        if (separator == null) {
                            separator = DEFAULT_SEPARATOR;
                        }

                        if (separatorClass != null && bannerStyle.equals("normal")) {
                            separator = "<span class=\"" + separatorClass + "\">" + separator + "</span>";
                        }

                        // this parameter to hide the navigation list in the IGAdmin Console.
                        Map<String, String> requestParams = context.getExternalContext()
                                .getRequestParameterMap();
                        Boolean isInIgAdminPanel = false;
                        if (requestParams != null) {
                            if (requestParams.get("FormPrincipal:_idcl") != null) {
                                isInIgAdminPanel = requestParams.get("FormPrincipal:_idcl")
                                        .contains("link-ig-admin");
                            }
                        }

                        boolean firsttime = true;

                        if ((value != null && bannerStyle.equals("normal") && !isInIgAdminPanel)
                                || (value != null && bannerStyle.equals("banner"))) {
                            for (Object object : value) {
                                final Node node = (Node) object;

                                if (node != null) {

                                    String name;

                                    if (node.hasAspect(CircabcModel.ASPECT_LIBRARY_ROOT)) {
                                        name = WebClientHelper.translate("library_menu");
                                    } else if (node.hasAspect(CircabcModel.ASPECT_EVENT_ROOT)) {
                                        name = WebClientHelper.translate("event_menu");
                                    } else if (node.hasAspect(CircabcModel.ASPECT_INFORMATION_ROOT)) {
                                        name = WebClientHelper.translate("information_menu");
                                    } else if (node.hasAspect(CircabcModel.ASPECT_NEWSGROUP_ROOT)) {
                                        name = WebClientHelper.translate("newsgroup_menu");
                                    } else {
                                        if (renderPropertyName != null && renderPropertyName.equals("title")) {
                                            String title = (String) node.getProperties()
                                                    .get(ContentModel.PROP_TITLE.toString());
                                            name = (title != null && title.length() > 1) ? title
                                                    : node.getName(); /*if title type and not null value*/
                                        } else /*  shortname or null value */ {
                                            name = node.getName();
                                        }
                                    }

                                    if (bannerStyle.equals("normal")) {
                                        if (firsttime && !separatorFirst) {
                                            // No output ahead
                                            firsttime = false;
                                        } else {
                                            out.write(separator);
                                        }
                                    } else if (bannerStyle.equals("banner")) {
                                        out.write("&nbsp;&gt;&nbsp;");
                                        out.write("<li>");
                                    }
                                    out.write("<a");
                                    if (bannerStyle.equals("normal")) {
                                        outputAttribute(out, styleClass, "class");
                                    }
                                    out.write(" href=\"");
                                    renderNonJavaScriptAnchorStart(context, out, component, id);
                                    out.write("&amp;id=");
                                    out.write(node.getId());

                                    //makz/13.01.2011: added viewstate to the link (bug DIGIT-CIRCABC-1722)

                                    if (requestParams != null) {
                                        String viewStateKey = "javax.faces.ViewState";
                                        String viewStateValue = requestParams.get(viewStateKey);
                                        if (viewStateValue != null) {
                                            out.write("&amp;" + viewStateKey + "=");
                                            out.write(URLEncoder.encode(viewStateValue, "UTF-8"));
                                        }
                                    }

                                    out.write("\"");
                                    if (onclick != null) {
                                        out.write(" onclick=\"");
                                        out.write(onclick);
                                        out.write("\"");
                                    }
                                    out.write(">");
                                    /*
                                     * Code pour le title try { out.write((String) node.getProperties().get( ContentModel.PROP_TITLE)); } catch (NullPointerException e) { // The property is not set out.write("&nbsp;"); }
                                     */

                                    // truncate the name to 100 car if needed...
                                    if (name.length() >= 100) {
                                        name = name.substring(0, 99);
                                    }

                                    out.write(ESAPI.encoder().encodeForHTML(name));
                                    out.write("</a>");

                                    if (bannerStyle.equals("banner")) {
                                        out.write("</li>");
                                    }
                                    if (separatorClass != null && bannerStyle.equals("normal")) {
                                        out.write("</span>");
                                    }
                                } else {
                                    out.write("&nbsp;");
                                }
                            }
                        }

                        return null;
                    }

                };
                txnHelper.doInTransaction(callback);
            } catch (final Throwable e) {
                Utils.addErrorMessage(MessageFormat.format(
                        Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC),
                        e.getMessage()), e);
            }

        } finally {
            if (userName != null) {
                AuthenticationUtil.setRunAsUser(userName);
            }
        }
    }

    /**
     * Render the href part of the tag
     *
     * @param context   The FacesContext
     * @param component The component
     * @param clientId  The clientId of the component
     */
    protected void renderNonJavaScriptAnchorStart(final FacesContext facesContext,
                                                  final ResponseWriter writer, final UIComponent component, final String clientId)
            throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("NavigationListRenderer : encodeBegin - renderNonJavaScriptAnchorStart - Start");
        }
        final ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        final String viewId = facesContext.getViewRoot().getViewId();
        final String path = viewHandler.getActionURL(facesContext, viewId);

        String href = "";
        final StringBuilder hrefBuf = new StringBuilder(path);
        if (logger.isInfoEnabled()) {
            logger.info("hrefBuf -> path |" + hrefBuf + "|");
        }

        // add clientId parameter for decode

        if (path.indexOf('?') == -1) {
            hrefBuf.append('?');
        } else {
            hrefBuf.append("&amp;");
        }

        final FormInfo forInfo = RendererUtils.findNestingForm(component, facesContext);

        final String hiddenFieldName = HtmlRendererUtils.getHiddenCommandLinkFieldName(forInfo);
        if (logger.isInfoEnabled()) {
            logger.info("NavigationListRenderer : hiddenFieldName |" + hiddenFieldName + "|");
        }
        hrefBuf.append(hiddenFieldName);
        hrefBuf.append('=');
        if (logger.isInfoEnabled()) {
            logger.info("NavigationListRenderer : clientId |" + clientId + "|");
        }
        hrefBuf.append(clientId);

        hrefBuf.append("&amp;");
        hrefBuf.append(RendererUtils.findNestingForm(component, facesContext).getFormName());
        hrefBuf.append("_SUBMIT");
        hrefBuf.append('=');
        hrefBuf.append(1);

        final StateManager stateManager = facesContext.getApplication().getStateManager();

        if (stateManager.isSavingStateInClient(facesContext)) {
            hrefBuf.append("&amp;");
            if (logger.isInfoEnabled()) {
                logger.info("NavigationListRenderer : isSavingStateInClient |"
                        + WebResourcesCircabc.URL_STATE_MARKER + "|");
            }
            hrefBuf.append(WebResourcesCircabc.URL_STATE_MARKER);
        }

        // Migration 3.1 -> 3.4.6 - 12/01/2012
        // Adding this line to correct the improvement "isPostBack" from the newer version of alfresco (3.4.1)
        // Between the 3.1.2 to 3.4.1 of alfresco version, a test has been changed in the class RestoreViewExecutor.java (line 93),
        // uses the isPostBack method
        hrefBuf.append("&amp;").append(WebResourcesCircabc.URL_FIX_ISPOSTBACK);

        href = hrefBuf.toString();
        if (logger.isInfoEnabled()) {
            logger.info("href |" + href + "|");
        }
        href = facesContext.getExternalContext().encodeActionURL(href);
        if (logger.isInfoEnabled()) {
            logger.info("href encode |" + href + "|");
        }
        writer.write(href);
        if (logger.isInfoEnabled()) {
            logger.info("NavigationListRenderer : encodeBegin - renderNonJavaScriptAnchorStart - End");
        }
    }

    /**
     * @see javax.faces.render.Renderer#getRendersChildren()
     */
    public boolean getRendersChildren() {
        return false;
    }
}
