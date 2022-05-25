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
package eu.cec.digit.circabc.web.ui.common.component.data;

import eu.cec.digit.circabc.web.ui.common.UtilsCircabc;
import org.alfresco.web.app.Application;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.util.FormInfo;

import javax.faces.component.NamingContainer;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Guillaume
 * @author makz
 */
public class UIDataPager extends UICommand {

    private static final String LAST_PAGE = "last_page";

    private static final String NEXT_PAGE = "next_page";

    private static final String PREVIOUS_PAGE = "prev_page";

    private static final String FIRST_PAGE = "first_page";

    private static final String ACTION_DESACTIVATED = "action_desactivated";

    private static final String MSG_PAGEINFO = "page_info";

    private static final String MSG_PAGEGOTO = "page_goto";

    private static final Log logger = LogFactory.getLog(UIDataPager.class);

    // ------------------------------------------------------------------------------
    // Construction

    /**
     * Default constructor
     */
    public UIDataPager() {
        setRendererType(null);
    }

    // ------------------------------------------------------------------------------
    // Component implementation

    /**
     * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
     */
    public void encodeBegin(FacesContext context) throws IOException {
        IDataContainer dataContainer = getDataContainer();
        if (dataContainer == null) {
            throw new IllegalStateException(
                    "Must nest UISortLink inside component implementing IDataContainer!");
        }

        // this component will only render itself if the parent DataContainer is setup
        // with a valid "pageSize" property
        if (isRendered() == false || dataContainer.getPageSize() == -1) {
            return;
        }
        logger.info("UIDataPager : encodeBegin - Start");

        String id = getHiddenFieldName();
        HashMap<String, String> attributes = new HashMap<>();

        ResponseWriter out = context.getResponseWriter();
        ResourceBundle bundle = Application.getBundle(context);
        StringBuilder buf = new StringBuilder(512);

        int currentPage = dataContainer.getCurrentPage();
        int pageCount = dataContainer.getPageCount();

        buf.append("<div");
        if (getAttributes().get("styleClass") != null) {
            buf.append(" class=").append(getAttributes().get("styleClass"));
        }
        buf.append('>');

        // output Page X of Y text
        buf.append(MessageFormat.format(bundle.getString(MSG_PAGEINFO), new Object[]
                {Integer.toString(currentPage + 1), // current page can be zero if no data present
                        Integer.toString(pageCount)}));

        buf.append("&nbsp;&nbsp;");

        // output HTML links or labels to render the paging controls
        // first page
        if (currentPage != 0) {
            attributes = new HashMap<>();
            attributes.put("page", "0");
            buf.append("<a href=\"");
            buf.append(UtilsCircabc.generateHrefPart(context, this, id, attributes));
            buf.append("\" title=\"");
            buf.append(bundle.getString(FIRST_PAGE));
            buf.append("\">");
            buf.append(UtilsCircabc
                    .buildImageTag(context, WebResources.IMAGE_FIRSTPAGE, bundle.getString(FIRST_PAGE)));
            buf.append("</a>");
        } else {
            buf.append(UtilsCircabc.buildImageTag(context, WebResources.IMAGE_FIRSTPAGE_NONE,
                    bundle.getString(ACTION_DESACTIVATED)));
        }

        buf.append("&nbsp;");

        // previous page
        if (currentPage != 0) {
            attributes = new HashMap<>();
            attributes.put("page", Integer.valueOf(currentPage - 1).toString());
            buf.append("<a href=\"");
            buf.append(UtilsCircabc.generateHrefPart(context, this, id, attributes));
            buf.append("\" title=\"");
            buf.append(bundle.getString(PREVIOUS_PAGE));
            buf.append("\">");
            buf.append(UtilsCircabc.buildImageTag(context, WebResources.IMAGE_PREVIOUSPAGE,
                    bundle.getString(PREVIOUS_PAGE)));
            buf.append("</a>");
        } else {
            buf.append(UtilsCircabc.buildImageTag(context, WebResources.IMAGE_PREVIOUSPAGE_NONE,
                    bundle.getString(ACTION_DESACTIVATED)));
        }

        buf.append("&nbsp;");

        // clickable digits for pages 1 to 10
        int totalIndex = (pageCount < 10 ? pageCount : 10);
        for (int i = 0; i < totalIndex; i++) {
            if (i != currentPage) {
                attributes = new HashMap<>();
                attributes.put("page", Integer.valueOf(i).toString());
                buf.append("<a href=\"");
                buf.append(UtilsCircabc.generateHrefPart(context, this, id, attributes));
                buf.append("\" title=\"");
                // output Go to Page X
                buf.append(MessageFormat
                        .format(bundle.getString(MSG_PAGEGOTO), new Object[]{Integer.toString(i + 1)}));
                buf.append("\">");
                buf.append(i + 1);
                buf.append("</a>&nbsp;");
            } else {
                buf.append("<b>").append(i + 1).append("</b>&nbsp;");
            }
        }
        // clickable digits for pages 20 to 100 (in jumps of 10)
        if (pageCount >= 20) {
            buf.append("...&nbsp;");
            totalIndex = (pageCount / 10) * 10;
            totalIndex = (totalIndex < 100 ? totalIndex : 100);
            for (int i = 19; i < totalIndex; i += 10) {
                if (i != currentPage) {
                    attributes = new HashMap<>();
                    attributes.put("page", Integer.valueOf(i).toString());
                    buf.append("<a href=\"");
                    buf.append(UtilsCircabc.generateHrefPart(context, this, id, attributes));
                    buf.append("\" title=\"");
                    // output Go to Page X
                    buf.append(MessageFormat
                            .format(bundle.getString(MSG_PAGEGOTO), new Object[]{Integer.toString(i + 1)}));
                    buf.append("\">");
                    buf.append(i + 1);
                    buf.append("</a>&nbsp;");
                } else {
                    buf.append("<b>").append(i + 1).append("</b>&nbsp;");
                }
            }
        }
        // clickable digits for last page if > 10 and not already shown
        if ((pageCount > 10) && (pageCount % 10 != 0)) {
            if (pageCount - 1 != currentPage) {
                if (pageCount < 20) {
                    buf.append("...&nbsp;");
                }
                attributes = new HashMap<>();
                attributes.put("page", Integer.valueOf(pageCount - 1).toString());
                buf.append("<a href=\"");
                buf.append(UtilsCircabc.generateHrefPart(context, this, id, attributes));
                buf.append("\" title=\"");
                // output Go to Page X
                buf.append(MessageFormat
                        .format(bundle.getString(MSG_PAGEGOTO), new Object[]{Integer.toString(pageCount)}));
                buf.append("\">");
                buf.append(pageCount);
                buf.append("</a>&nbsp;");
            } else {
                if (pageCount < 20) {
                    buf.append("...&nbsp;");
                }
                buf.append("<b>").append(pageCount).append("</b>&nbsp;");
            }
        }

        // next page
        //if ((dataContainer.getCurrentPage() < dataContainer.getPageCount() - 1) == true)
        if ((currentPage < dataContainer.getPageCount() - 1) == true) {
            attributes = new HashMap<>();
            attributes.put("page", Integer.valueOf(currentPage + 1).toString());
            buf.append("<a href=\"");
            buf.append(UtilsCircabc.generateHrefPart(context, this, id, attributes));
            buf.append("\" title=\"");
            buf.append(bundle.getString(NEXT_PAGE));
            buf.append("\">");
            buf.append(UtilsCircabc
                    .buildImageTag(context, WebResources.IMAGE_NEXTPAGE, bundle.getString(NEXT_PAGE)));
            buf.append("</a>");
        } else {
            buf.append(UtilsCircabc.buildImageTag(context, WebResources.IMAGE_NEXTPAGE_NONE,
                    bundle.getString(ACTION_DESACTIVATED)));
        }

        buf.append("&nbsp;");

        // last page
        //if ((dataContainer.getCurrentPage() < dataContainer.getPageCount() - 1) == true)
        if ((currentPage < dataContainer.getPageCount() - 1) == true) {
            attributes = new HashMap<>();
            attributes.put("page", Integer.valueOf(dataContainer.getPageCount() - 1).toString());
            buf.append("<a href=\"");
            buf.append(UtilsCircabc.generateHrefPart(context, this, id, attributes));
            buf.append("\" title=\"");
            buf.append(bundle.getString(LAST_PAGE));
            buf.append("\">");
            buf.append(UtilsCircabc
                    .buildImageTag(context, WebResources.IMAGE_LASTPAGE, bundle.getString(LAST_PAGE)));
            buf.append("</a>");
        } else {
            buf.append(UtilsCircabc.buildImageTag(context, WebResources.IMAGE_LASTPAGE_NONE,
                    bundle.getString(ACTION_DESACTIVATED)));
        }

        buf.append("</div>");

        out.write(buf.toString());

        logger.info("UIDataPager : encodeBegin - End");
    }

    /**
     * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
     */
    public void decode(FacesContext context) {
        String fieldId = getHiddenFieldName();
        FormInfo formInfo = RendererUtils.findNestingForm(this, context);
        String reqValue = (String) context.getExternalContext().getRequestParameterMap()
                .get(HtmlRendererUtils.getHiddenCommandLinkFieldName(formInfo));
        logger.info("decodeActionLink " + HtmlRendererUtils.getHiddenCommandLinkFieldName(formInfo));

        if (reqValue != null && reqValue.equals(fieldId)) {
            // Get param 'page from url
            Map requestMap = context.getExternalContext().getRequestParameterMap();
            String paramValue = (String) requestMap.get("page");

            // we were clicked - queue an event to represent the click
            // cannot handle the event here as other components etc. have not had
            // a chance to decode() - we queue an event to be processed later
            PageEvent actionEvent = new PageEvent(this, Integer.valueOf(paramValue));
            this.queueEvent(actionEvent);
            RendererUtils.initPartialValidationAndModelUpdate(this, context);
        }
    }

    /**
     * @see javax.faces.component.UICommand#broadcast(javax.faces.event.FacesEvent)
     */
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        if (event instanceof PageEvent == false) {
            // let the super class handle events which we know nothing about
            super.broadcast(event);
        } else {
            // found a sort event for us!
            logger.debug("Handling paging event to index: " + ((PageEvent) event).Page);

            getDataContainer().setCurrentPage(((PageEvent) event).Page);
        }
    }

    // ------------------------------------------------------------------------------
    // Private helpers

    /**
     * Return the parent data container for this component
     */
    private IDataContainer getDataContainer() {
        return Utils.getParentDataContainer(getFacesContext(), this);
    }

    /**
     * We use a hidden field name based on the parent data container component Id and the string
     * "pager" to give a field name that can be shared by all pager links within a single data
     * container component.
     *
     * @return hidden field name
     */
    private String getHiddenFieldName() {
        UIComponent dataContainer = (UIComponent) Utils.getParentDataContainer(getFacesContext(), this);
        return dataContainer.getClientId(getFacesContext()) + NamingContainer.SEPARATOR_CHAR + "pager";
    }

    // ------------------------------------------------------------------------------
    // Inner classes

    /**
     * Class representing the clicking of a sortable column.
     */
    private static class PageEvent extends ActionEvent {

        private static final long serialVersionUID = 7823578070492158599L;
        public int Page = 0;

        public PageEvent(UIComponent component, int page) {
            super(component);
            Page = page;
        }
    }
}
