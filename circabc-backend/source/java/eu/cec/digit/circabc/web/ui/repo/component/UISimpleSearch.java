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

import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.ui.common.UtilsCircabc;
import eu.cec.digit.circabc.web.ui.common.WebResourcesCircabc;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.search.AdvancedSearchDialog;
import eu.cec.digit.circabc.web.wai.dialog.search.SearchResultDialog;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.search.SearchContext;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.ui.common.ConstantMethodBinding;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
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
public class UISimpleSearch extends UICommand {

    private static final String ACTION_LINK_COMPONENT = "eu.cec.digit.circabc.faces.ActionLink";

    private static final Class ACTION_CLASS_ARGS[] = {javax.faces.event.ActionEvent.class};
    private static final String ACTION_LISTENER = "#{WaiDialogManager.setupParameters}";

    // ------------------------------------------------------------------------------
    // Component implementation
    private static final Log logger = LogFactory.getLog(UISimpleSearch.class);
    /**
     * The label of the Box
     */
    private String label;
    /**
     * The alt text of the image at the left of the label
     */
    private String labelAltText;
    /**
     * The label of the button
     */
    private String button;
    /**
     * The alt text of the image for the search action
     */
    private String buttonAltText;
    /**
     * last search context used
     */
    private SearchContext search = null;

    /**
     * Default Constructor
     */
    public UISimpleSearch() {
        // specifically set the renderer type to null to indicate to the
        // framework
        // that this component renders itself - there is no abstract renderer
        // class
        setRendererType(null);
    }

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.SimpleSearch";
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.search = (SearchContext) values[1];
        this.label = (String) values[2];
        this.labelAltText = (String) values[3];
        this.button = (String) values[4];
        this.buttonAltText = (String) values[5];
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[6];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = this.search;
        values[2] = this.label;
        values[3] = this.labelAltText;
        values[4] = this.button;
        values[5] = this.buttonAltText;
        return (values);
    }

    /**
     * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
     */
    public void decode(FacesContext context) {
        Map requestMap = context.getExternalContext().getRequestParameterMap();
        String fieldId = getClientId(context);
        String value = (String) requestMap.get(fieldId);
        if (value != null) {
            // if (value.equals(this.getButton()))
            // {
            String searchText = (String) requestMap.get(getClientId(context)
                    + "Text");

            if (searchText.length() != 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Search text submitted: " + searchText);
                }
                // queue event so system can perform a search and update the
                // component
                // option = 0 -> All field
                SearchEvent event = new SearchEvent(this, searchText, 0);
                this.queueEvent(event);
            }
            // }
        }
    }

    /**
     * @see javax.faces.component.UICommand#broadcast(javax.faces.event.FacesEvent)
     */
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        FacesContext fc = getFacesContext();
        if (event instanceof SearchEvent) {
            // update the component parameters from the search event details
            SearchEvent searchEvent = (SearchEvent) event;

            // construct the Search Context object
            SearchContext context = new SearchContext();
            context.setText(searchEvent.searchText);
            context.setMode(searchEvent.searchMode);
            context.setForceAndTerms(Application.getClientConfig(fc)
                    .getForceAndTerms());
            context.setSimpleSearchAdditionalAttributes(Application
                    .getClientConfig(fc).getSimpleSearchAdditionalAttributes());
            this.search = context;

            super.broadcast(event);
        }
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    @SuppressWarnings("unchecked")
    public void encodeBegin(FacesContext context) throws IOException {
        if (isRendered() == false) {
            return;
        }

        ResponseWriter out = context.getResponseWriter();
        String styleClass = (String) getAttributes().get("styleClass");

        out.write("<div");
        Utils.outputAttribute(out, getAttributes().get("styleClass"), "class");
        out.write(">");

        // Header
        out.write(UtilsCircabc.buildImageTag(context,
                WebResourcesCircabc.IMAGE_SEARCH_LABEL, getLabelAltText(),
                getLabel(), styleClass));
        out.write(getLabel());
        out.write("<br />");

        // input text box
        String clientId = getClientId(context);
        String idText = clientId + "Text";
        out.write("<input id=\"");
        out.write(idText);
        out.write("\" name=\"");
        out.write(idText);
        out.write("\" type=\"text\" maxlength=\"1024\" value=\"");
        out.write(Utils.replace(getLastSearch(), "\"", "&quot;"));
        out.write("\"");
        if (styleClass != null) {
            out.write(" class=\"");
            out.write(styleClass);
            out.write("Text\"");
        }
        out.write(">");

        // search Go image button
        out.write("<input id=\"");
        out.write(clientId);
        out.write("\" name=\"");
        out.write(clientId);
        out.write("\" type=\"submit\" value=\"");
        out.write(getButton());
        out.write("\" title=\"");
        out.write(getButtonAltText());
        out.write("\"");
        if (styleClass != null) {
            out.write(" class=\"");
            out.write(styleClass);
            out.write("\"");
        }
        out.write("><br />");

        // Advanced search

        out.write("<div class=\"pad8px\">");

        out.write(UtilsCircabc.buildImageTag(context, "/images/icons/search_icon.gif", "."));

        out.write("&nbsp;&nbsp;");

        final javax.faces.application.Application facesApp = context.getApplication();
        final UIActionLink advSearchActionLink = (UIActionLink) facesApp
                .createComponent(ACTION_LINK_COMPONENT);

        advSearchActionLink.setAction(new ConstantMethodBinding(AdvancedSearchDialog.WAI_DIALOG_CALL));
        advSearchActionLink.setValue(Application.getMessage(context, "advanced_search_action"));
        advSearchActionLink.setActionListener(
                context.getApplication().createMethodBinding(ACTION_LISTENER, ACTION_CLASS_ARGS));
        advSearchActionLink
                .setTooltip(Application.getMessage(context, "advanced_search_action_tooltip"));
        advSearchActionLink.setParent(this);
        advSearchActionLink.setId("advSearch" + System.currentTimeMillis());

        final UIParameter advSearchParam =
                (UIParameter) facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);

        advSearchParam.setName(BaseWaiDialog.NODE_ID_PARAMETER);
        advSearchParam.setValue(Beans.getWaiNavigator().getCurrentNodeId());
        advSearchParam.setId("advSearchParam" + System.currentTimeMillis());
        advSearchActionLink.getChildren().add(advSearchParam);
        getChildren().add(advSearchActionLink);
        Utils.encodeRecursive(context, advSearchActionLink);

        // Back to search result

        if (Beans.getSearchResultDialog().hasSearchAvailableInCache()) {
            out.write("<br />");

            out.write(UtilsCircabc.buildImageTag(context, "/images/icons/filter.gif", "."));

            out.write("&nbsp;&nbsp;");

            final UIActionLink backToSearchActionLink = (UIActionLink) facesApp
                    .createComponent(ACTION_LINK_COMPONENT);

            backToSearchActionLink
                    .setAction(new ConstantMethodBinding(SearchResultDialog.WAI_DIALOG_CALL));
            backToSearchActionLink
                    .setValue(Application.getMessage(context, "advanced_search_back_to_search"));
            backToSearchActionLink.setActionListener(
                    context.getApplication().createMethodBinding(ACTION_LISTENER, ACTION_CLASS_ARGS));
            backToSearchActionLink
                    .setTooltip(Application.getMessage(context, "advanced_search_back_to_search_tooltip"));
            backToSearchActionLink.setParent(this);
            backToSearchActionLink.setId("backToSearch" + System.currentTimeMillis());

            getChildren().add(backToSearchActionLink);
            Utils.encodeRecursive(context, backToSearchActionLink);
        }

        out.write("</div>");

        out.write("</div>");
    }

    /**
     * Return the current Search Context
     */
    public SearchContext getSearchContext() {
        return this.search;
    }

    /**
     * @return The last set search text value
     */
    public String getLastSearch() {
        if (search != null) {
            return this.search.getText();
        } else {
            return "";
        }
    }

    /**
     * @return The current search mode (see constants)
     */
    public int getSearchMode() {
        if (search != null) {
            return this.search.getMode();
        } else {
            return SearchContext.SEARCH_ALL;
        }
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

    // ------------------------------------------------------------------------------
    // Private data

    /**
     * Get the alt text for the label to use
     *
     * @return the alt text for the label
     */
    public String getLabelAltText() {
        ValueBinding vb = getValueBinding("labelAltText");
        if (vb != null) {
            this.labelAltText = (String) vb.getValue(getFacesContext());
        }

        return this.labelAltText;
    }

    /**
     * Set the alt text for the label to use
     *
     * @param labelAltText the alt text
     */
    public void setLabelAltText(String labelAltText) {
        this.labelAltText = labelAltText;
    }

    /**
     * Get the label for the search button
     *
     * @return Returns the label of the button.
     */
    public String getButton() {
        ValueBinding vb = getValueBinding("button");
        if (vb != null) {
            this.button = (String) vb.getValue(getFacesContext());
        }

        return this.button;
    }

    /**
     * Set the label for the search button
     *
     * @param button The label of the button to set.
     */
    public void setButton(String button) {
        this.button = button;
    }

    /**
     * Get the alt text for the search's button action
     *
     * @return String the alt text for the search's button action
     */
    public String getButtonAltText() {
        ValueBinding vb = getValueBinding("buttonAltText");
        if (vb != null) {
            this.buttonAltText = (String) vb.getValue(getFacesContext());
        }

        return this.buttonAltText;
    }

    /**
     * Set the alt text for the search's button action
     *
     * @param String the alt text for the search's button action
     */
    public void setButtonAltText(String buttonAltText) {
        this.buttonAltText = buttonAltText;
    }

    // ------------------------------------------------------------------------------
    // Inner classes

    /**
     * Class representing a search execution from the UISimpleSearch component.
     */
    public static class SearchEvent extends ActionEvent {

        private static final long serialVersionUID = 6955087098765144480L;
        public String searchText;
        public int searchMode;

        public SearchEvent(UIComponent component, String text, int mode) {
            super(component);
            searchText = text.trim();
            searchMode = mode;
        }
    }

}
