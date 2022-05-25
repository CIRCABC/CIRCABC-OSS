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
package eu.cec.digit.circabc.myfaces.taglib.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.jsp.JspViewHandlerImpl;
import org.apache.myfaces.renderkit.html.HtmlResponseStateManager;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlLinkRendererBase;
import org.apache.myfaces.shared_impl.util.LocaleUtils;
import org.apache.myfaces.shared_impl.util.StateUtils;

import javax.faces.application.StateManager;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentBodyTag;
import javax.faces.webapp.UIComponentTag;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Circabc ViewTag implementation for use when views are defined via tags in JSP pages. We need
 * other action when a state is not found in session Clone from orignal but with the attend behavior
 * (why original is not good ????)
 *
 * @author Guillaume
 */
public class CircabcViewTag extends UIComponentBodyTag {

    private static final String LOCALE = "locale";
    private static final int TREE_PARAM = 0;
    private static final int VIEWID_PARAM = 2;
    private static final Log logger = LogFactory.getLog(CircabcViewTag.class);
    private String _locale;

    @Override
    public String getComponentType() {
        return UIViewRoot.COMPONENT_TYPE;
    }

    @Override
    public String getRendererType() {
        return null;
    }

    public void setLocale(final String locale) {
        _locale = locale;
    }

    @Override
    public int doStartTag() throws JspException {
        if (logger.isTraceEnabled()) {
            logger.trace("entering ViewTag.doStartTag");
        }
        super.doStartTag();
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        final ResponseWriter responseWriter = facesContext.getResponseWriter();
        try {
            responseWriter.startDocument();
        } catch (final IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error writing startDocument", e);
            }
            throw new JspException(e);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("leaving ViewTag.doStartTag");
        }
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    @Override
    protected boolean isSuppressed() {
        return true;
    }

    @Override
    public int doEndTag() throws JspException {
        if (logger.isTraceEnabled()) {
            logger.trace("entering ViewTag.doEndTag");
        }
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        final ResponseWriter responseWriter = facesContext.getResponseWriter();

        try {
            responseWriter.endDocument();
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error writing endDocument", e);
            }
            throw new JspException(e);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("leaving ViewTag.doEndTag");
        }
        return super.doEndTag();
    }

    @Override
    public int doAfterBody() throws JspException {
        if (logger.isTraceEnabled()) {
            logger.trace("entering ViewTag.doAfterBody");
        }
        try {
            final BodyContent bodyContent = getBodyContent();
            if (bodyContent != null) {
                final FacesContext facesContext = FacesContext.getCurrentInstance();
                final StateManager stateManager = facesContext.getApplication().getStateManager();
                final StateManager.SerializedView serializedView = stateManager
                        .saveSerializedView(facesContext);

                // until now we have written to a buffer
                final ResponseWriter bufferWriter = facesContext.getResponseWriter();
                bufferWriter.flush();
                // now we switch to real output
                final ResponseWriter realWriter = bufferWriter.cloneWithWriter(getPreviousOut());
                facesContext.setResponseWriter(realWriter);

                final String bodyStr = bodyContent.getString();
                /*
                 * do this always - even with server-side-state saving if ( stateManager.isSavingStateInClient(facesContext) ) {
                 */

                int form_marker = bodyStr.indexOf(JspViewHandlerImpl.FORM_STATE_MARKER);
                int url_marker = bodyStr.indexOf(HtmlLinkRendererBase.URL_STATE_MARKER);
                int lastMarkerEnd = 0;

                // set view state
                final Object[] savedState = new Object[3];
                final Object treeStruct = serializedView.getStructure();
                if (treeStruct != null) {
                    if (treeStruct instanceof String) {
                        savedState[TREE_PARAM] = treeStruct;
                    }
                }
                savedState[VIEWID_PARAM] = facesContext.getViewRoot().getViewId();
                final String viewState = StateUtils
                        .construct(savedState, facesContext.getExternalContext());

                while (form_marker != -1 || url_marker != -1) {
                    if (url_marker == -1 || (form_marker != -1 && form_marker < url_marker)) {
                        // replace form_marker
                        realWriter.write(bodyStr, lastMarkerEnd, form_marker - lastMarkerEnd);
                        stateManager.writeState(facesContext, serializedView);
                        lastMarkerEnd = form_marker + JspViewHandlerImpl.FORM_STATE_MARKER_LEN;
                        form_marker = bodyStr.indexOf(JspViewHandlerImpl.FORM_STATE_MARKER, lastMarkerEnd);
                    } else {
                        // replace url_marker
                        realWriter.write(bodyStr, lastMarkerEnd, url_marker - lastMarkerEnd);
                        //
                        realWriter.write(HtmlResponseStateManager.STANDARD_STATE_SAVING_PARAM);
                        realWriter.write("=");
                        realWriter.write(URLEncoder.encode(viewState, "UTF-8"));
                        lastMarkerEnd = url_marker + HtmlLinkRendererBase.URL_STATE_MARKER_LEN;
                        url_marker = bodyStr.indexOf(HtmlLinkRendererBase.URL_STATE_MARKER, lastMarkerEnd);
                    }
                }
                realWriter.write(bodyStr, lastMarkerEnd, bodyStr.length() - lastMarkerEnd);

            }
        } catch (final IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error writing body content", e);
            }
            throw new JspException(e);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("leaving ViewTag.doAfterBody");
        }
        return super.doAfterBody();
    }

    @Override
    protected void setProperties(final UIComponent component) {
        super.setProperties(component);

        if (_locale != null) {
            Locale locale;
            if (UIComponentTag.isValueReference(_locale)) {
                final FacesContext context = FacesContext.getCurrentInstance();
                final ValueBinding vb = context.getApplication().createValueBinding(_locale);
                final Object localeValue = vb.getValue(context);
                if (localeValue instanceof Locale) {
                    locale = (Locale) localeValue;
                } else if (localeValue instanceof String) {
                    locale = LocaleUtils.toLocale((String) localeValue);
                } else {
                    if (localeValue != null) {
                        throw new IllegalArgumentException(
                                "Locale or String class expected. Expression: " + _locale + ". Return class: "
                                        + localeValue.getClass().getName());
                    } else {
                        throw new IllegalArgumentException(
                                "Locale or String class expected. Expression: " + _locale + ". Return value null");
                    }
                }
            } else {
                locale = LocaleUtils.toLocale(_locale);
            }
            ((UIViewRoot) component).setLocale(locale);
            Config.set((ServletRequest) getFacesContext().getExternalContext().getRequest(),
                    Config.FMT_LOCALE, locale);
        } else {
            final FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                if (facesContext.getExternalContext().getSessionMap().containsKey(LOCALE)) {
                    Locale locale = LocaleUtils
                            .toLocale(facesContext.getExternalContext().getSessionMap().get(LOCALE).toString());
                    ((UIViewRoot) component).setLocale(locale);
                }
            }
        }
    }
}
