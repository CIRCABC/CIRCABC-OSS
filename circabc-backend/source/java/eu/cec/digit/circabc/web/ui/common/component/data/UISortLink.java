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
import eu.cec.digit.circabc.web.ui.common.WebResourcesCircabc;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.renderkit.JSFAttr;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HTML;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlFormRendererBase;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.util.FormInfo;

import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import java.io.IOException;
import java.util.Map;

/**
 * @author Guillaume
 */
public class UISortLink extends UICommand {

    private static final Log s_logger = LogFactory.getLog(IDataContainer.class);
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(UISortLink.class);
    /**
     * sorting mode
     */
    private String mode = IDataContainer.SORT_CASEINSENSITIVE;
    private String label;
    private String tooltip;
    private String tooltipAscending;
    private String tooltipDescending;
    /**
     * true for descending sort, false for ascending
     */
    private boolean descending = false;

    /**
     * Default Constructor
     */
    public UISortLink() {
        setRendererType(null);
    }

    /**
     * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
     */
    public void encodeBegin(FacesContext context) throws IOException {
        if (isRendered() == false) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("SortLink : encodeBegin - Start");
        }

        ResponseWriter writer = context.getResponseWriter();
        Map attrs = this.getAttributes();
        String clientId = getClientId(context);

        IDataContainer dataContainer = getDataContainer();
        if (dataContainer == null) {
            throw new IllegalStateException(
                    "Must nest UISortLink inside component implementing IDataContainer!");
        }

        // swap sort direction if we were last sorted column
        boolean bPreviouslySorted = false;
        boolean descending = true;
        String lastSortedColumn = dataContainer.getCurrentSortColumn();
        if (lastSortedColumn != null && lastSortedColumn.equals(getValue())) {
            descending = !dataContainer.isCurrentSortDescending();
            bPreviouslySorted = true;
        }

        writer.startElement(HTML.ANCHOR_ELEM, this);
        // Build the href part
        renderNonJavaScriptAnchorStart(context, writer, this, clientId);

        // Render other attributes
        if (attrs.get("id") != null) {
            writer.writeAttribute(HTML.ID_ATTR, attrs.get("id"), null);
        } else {
            writer.writeAttribute(HTML.ID_ATTR, clientId, null);
        }
        if (attrs.get("styleClass") != null) {
            writer.writeAttribute(HTML.STYLE_CLASS_ATTR, attrs
                    .get("styleClass"), null);
        }

        // Get the value of the tooltip
        Object tooltip = null;
        String image = "";
        if (descending == true) {
            // A -> Z
            tooltip = attrs.get("tooltipDescending");
            image = WebResourcesCircabc.IMAGE_SORTDOWN;
        } else {
            // Z -> A
            tooltip = attrs.get("tooltipAscending");
            image = WebResourcesCircabc.IMAGE_SORTUP;
        }
        // If the sort is not currently on this column
        if (bPreviouslySorted == false) {
            image = WebResourcesCircabc.IMAGE_SORTNONE;
        }
        // Output the inner
        writer.writeAttribute(HTML.TITLE_ATTR, tooltip, null);
        writer.writeText(attrs.get("label") + " ", JSFAttr.VALUE_ATTR);
        writer
                .write(UtilsCircabc.buildImageTag(context, image,
                        (String) tooltip, (String) tooltip));

        writer.writeText("", null);
        writer.endElement(HTML.ANCHOR_ELEM);
        HtmlFormRendererBase.renderScrollHiddenInputIfNecessary(RendererUtils
                .findNestingForm(this, context).getForm(), context, context
                .getResponseWriter());
        if (logger.isInfoEnabled()) {
            logger.info("SortLink : encodeBegin - End");
        }
    }

    /**
     * Render the href part of the tag
     *
     * @param facesContext The FacesContext
     * @param component    The component
     * @param clientId     The clientId of the component
     */
    protected void renderNonJavaScriptAnchorStart(FacesContext facesContext,
                                                  ResponseWriter writer, UIComponent component, String clientId)
            throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info(
                    "SortLink : encodeBegin - renderCommandLinkStart - renderNonJavaScriptAnchorStart - Start");
        }
        ViewHandler viewHandler = facesContext.getApplication()
                .getViewHandler();
        String viewId = facesContext.getViewRoot().getViewId();
        String path = viewHandler.getActionURL(facesContext, viewId);

        UISortLink link = (UISortLink) component;
        Map attrs = link.getAttributes();

        String href = "";
        StringBuilder hrefBuf = new StringBuilder(path);
        if (logger.isInfoEnabled()) {
            logger.info("hrefBuf -> path |" + hrefBuf + "|");
        }

        // add clientId parameter for decode

        if (path.indexOf('?') == -1) {
            hrefBuf.append('?');
        } else {
            hrefBuf.append("&amp;");
        }
        String hiddenFieldName = HtmlRendererUtils
                .getHiddenCommandLinkFieldName(RendererUtils.findNestingForm(
                        component, facesContext));
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
        hrefBuf.append(RendererUtils.findNestingForm(component, facesContext)
                .getFormName());
        hrefBuf.append("_SUBMIT");
        hrefBuf.append('=');
        hrefBuf.append(1);

        StateManager stateManager = facesContext.getApplication()
                .getStateManager();

        if (stateManager.isSavingStateInClient(facesContext)) {
            hrefBuf.append("&amp;");
            if (logger.isInfoEnabled()) {
                logger.info("isSavingStateInClient |" + WebResourcesCircabc.URL_STATE_MARKER + "|");
            }
            hrefBuf.append(WebResourcesCircabc.URL_STATE_MARKER);
        }

        if (attrs.get("anchor") != null) {
            // Add an "n" to respect spec which anchor don't have to start
            // with an number
            hrefBuf.append("#n");
            hrefBuf.append(attrs.get("anchor"));
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
        writer.writeURIAttribute(HTML.HREF_ATTR, href, null);
        if (logger.isInfoEnabled()) {
            logger.info(
                    "SortLink : encodeBegin - renderCommandLinkStart - renderNonJavaScriptAnchorStart - End");
        }
    }

    /**
     * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    public void decode(FacesContext context) {
        String clientId = this.getClientId(context);
        FormInfo formInfo = RendererUtils.findNestingForm(this, context);
        String reqValue = (String) context.getExternalContext()
                .getRequestParameterMap().get(
                        HtmlRendererUtils
                                .getHiddenCommandLinkFieldName(formInfo));
        if (logger.isInfoEnabled()) {
            logger.info("decodeSortLink "
                    + HtmlRendererUtils.getHiddenCommandLinkFieldName(formInfo));
        }
        if (reqValue != null && reqValue.equals(clientId)) {

            SortEvent actionEvent = new SortEvent(this, (String) this
                    .getValue());
            this.queueEvent(actionEvent);
        }
    }

    /**
     * @see javax.faces.component.UIComponent#broadcast(javax.faces.event.FacesEvent)
     */
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        if (event instanceof SortEvent == false) {
            // let the super class handle events which we know nothing about
            super.broadcast(event);
        } else if (((SortEvent) event).Column.equals(getColumn())) {
            // found a sort event for us!
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Handling sort event for column: "
                        + ((SortEvent) event).Column);
            }

            if (getColumn().equals(getDataContainer().getCurrentSortColumn()) == true) {
                // reverse sort direction
                this.descending = !this.descending;
            } else {
                // revert to default sort direction
                this.descending = true;
            }
            getDataContainer().sort(getColumn(), this.descending, getMode());
        }
    }

    /**
     * Column name referenced by this link
     *
     * @return column name
     */
    public String getColumn() {
        return (String) getValue();
    }

    /**
     * Sorting mode - see IDataContainer constants
     *
     * @return sorting mode - see IDataContainer constants
     */
    public String getMode() {
        return this.mode;
    }

    /**
     * Set the sorting mode - see IDataContainer constants
     *
     * @param sortMode the sorting mode- see IDataContainer constants
     */
    public void setMode(String sortMode) {
        this.mode = sortMode;
    }

    /**
     * Returns true for descending sort, false for ascending
     *
     * @return true for descending sort, false for ascending
     */
    public boolean isDescending() {
        return this.descending;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        ValueBinding vb = getValueBinding("label");
        if (vb != null) {
            this.label = (String) vb.getValue(getFacesContext());
        }
        return this.label;
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Returns the tooltip.
     */
    public String getTooltip() {
        ValueBinding vb = getValueBinding("tooltip");
        if (vb != null) {
            this.tooltip = (String) vb.getValue(getFacesContext());
        }
        return this.tooltip;
    }

    // ------------------------------------------------------------------------------
    // Inner classes

    /**
     * @param tooltip The tooltip to set.
     */
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    // ------------------------------------------------------------------------------
    // Constants

    /**
     * @return Returns the tooltipAscending.
     */
    public String getTooltipAscending() {
        ValueBinding vb = getValueBinding("tooltipAscending");
        if (vb != null) {
            this.tooltipAscending = (String) vb.getValue(getFacesContext());
        }
        return this.tooltipAscending;
    }

    /**
     * @param tooltipAscending The tooltipAscending to set.
     */
    public void setTooltipAscending(String tooltipAscending) {
        this.tooltipAscending = tooltipAscending;
    }

    /**
     * @return Returns the tooltipDescending.
     */
    public String getTooltipDescending() {
        ValueBinding vb = getValueBinding("tooltipDescending");
        if (vb != null) {
            this.tooltipDescending = (String) vb.getValue(getFacesContext());
        }
        return this.tooltipDescending;
    }

    /**
     * @param tooltipDescending The tooltipDescending to set.
     */
    public void setTooltipDescending(String tooltipDescending) {
        this.tooltipDescending = tooltipDescending;
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.descending = (Boolean) values[1];
        this.mode = (String) values[2];
        this.label = (String) values[3];
        this.tooltip = (String) values[4];
        this.tooltipAscending = (String) values[5];
        this.tooltipDescending = (String) values[6];
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[7];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = (this.descending ? Boolean.TRUE : Boolean.FALSE);
        values[2] = this.mode;
        values[3] = this.label;
        values[4] = this.tooltip;
        values[5] = this.tooltipAscending;
        values[6] = this.tooltipDescending;
        return values;
    }

    /**
     * Return the parent data container for this component
     */
    private IDataContainer getDataContainer() {
        return Utils.getParentDataContainer(getFacesContext(), this);
    }

    /**
     * Class representing the clicking of a sortable column.
     */
    private static class SortEvent extends ActionEvent {

        private static final long serialVersionUID = 5946800979154807497L;
        public String Column = null;

        public SortEvent(UIComponent component, String column) {
            super(component);
            Column = column;
        }
    }
}
