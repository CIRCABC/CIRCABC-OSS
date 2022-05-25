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
/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package eu.cec.digit.circabc.web.ui.common.component;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Kevin Roast
 */
public class UIGenericPicker extends UICommand {

    /**
     * action ids
     */
    private static final int ACTION_NONE = -1;
    private static final int ACTION_SEARCH = 0;
    private static final int ACTION_CLEAR = 1;
    private static final int ACTION_FILTER = 2;
    private static final int ACTION_ADD = 3;

    /**
     * form field postfixes
     */
    private static final String FIELD_FILTER = "_filter";
    private static final String FIELD_CONTAINS = "_contains";
    private static final String FIELD_RESULTS = "_results";

    /**
     * I18N message strings
     */
    private static final String MSG_SEARCH = "search";
    private static final String MSG_CLEAR = "clear";
    private static final String MSG_ADD = "add";
    private static final String MSG_RESULTS1 = "results_contains";
    private static final String MSG_RESULTS2 = "results_contains_filter";

    private static final int DEFAULT_HEIGHT = 100;
    private static final int DEFAULT_WIDTH = 250;
    private static final String MSG_RESULTS_TITLE = "search_results";

    private MethodBinding queryCallback = null;
    private Boolean showFilter = null;
    private Boolean showContains = null;
    private Boolean showAddButton = null;
    private Boolean filterRefresh = null;
    private Boolean multiSelect = null;
    private String addButtonLabel = null;
    private String searchButtonLabel = null;
    private Integer width = null;
    private Integer height = null;

    private SelectItem[] filters = null;
    private int filterIndex = 0;
    private String contains = "";
    private String[] selectedResults = null;
    private SelectItem[] currentResults = null;

    // ------------------------------------------------------------------------------
    // Component implementation

    /**
     * Default constructor
     */
    public UIGenericPicker() {
        setRendererType(null);
    }

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "org.alfresco.faces.GenericPicker";
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        showFilter = (Boolean) values[1];
        showContains = (Boolean) values[2];
        showAddButton = (Boolean) values[3];
        addButtonLabel = (String) values[4];
        width = (Integer) values[5];
        height = (Integer) values[6];
        filterIndex = (Integer) values[7];
        contains = (String) values[8];
        queryCallback = (MethodBinding) restoreAttachedState(context, values[9]);
        selectedResults = (String[]) values[10];
        currentResults = (SelectItem[]) values[11];
        filters = (SelectItem[]) values[12];
        filterRefresh = (Boolean) values[13];
        multiSelect = (Boolean) values[14];
        searchButtonLabel = (String) values[15];
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[16];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = showFilter;
        values[2] = showContains;
        values[3] = showAddButton;
        values[4] = addButtonLabel;
        values[5] = width;
        values[6] = height;
        values[7] = filterIndex;
        values[8] = contains;
        values[9] = saveAttachedState(context, queryCallback);
        values[10] = selectedResults;
        values[11] = currentResults;
        values[12] = filters;
        values[13] = filterRefresh;
        values[14] = multiSelect;
        values[15] = searchButtonLabel;
        return (values);
    }

    /**
     * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
     */
    public void decode(FacesContext context) {
        Map requestMap = context.getExternalContext().getRequestParameterMap();
        Map valuesMap = context.getExternalContext().getRequestParameterValuesMap();
        String fieldId = getHiddenFieldName();
        String value = (String) requestMap.get(fieldId);

        int action = ACTION_NONE;
        if (value != null && value.length() != 0) {
            // decode the values - we are expecting an action identifier
            action = Integer.parseInt(value);
        }

        // we always process these values to keep the component up-to-date

        // now find the Filter drop-down value
        int filterIndex = 0;
        String strFilterIndex = (String) requestMap.get(fieldId + FIELD_FILTER);
        if (strFilterIndex != null && strFilterIndex.length() != 0) {
            filterIndex = Integer.parseInt(strFilterIndex);
        }

        // and the Contains text box value
        String contains = (String) requestMap.get(fieldId + FIELD_CONTAINS);

        // and the Results selections
        String[] results = (String[]) valuesMap.get(fieldId + FIELD_RESULTS);

        // queue an event
        PickerEvent event = new PickerEvent(this, action, filterIndex, contains, results);
        queueEvent(event);
    }

    /**
     * @see javax.faces.component.UIComponentBase#broadcast(javax.faces.event.FacesEvent)
     */
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        if (event instanceof PickerEvent) {
            PickerEvent pickerEvent = (PickerEvent) event;

            // set component state from event properties
            this.filterIndex = pickerEvent.FilterIndex;
            this.contains = pickerEvent.Contains;
            this.selectedResults = pickerEvent.getResults();

            // delegate to appropriate action logic
            switch (pickerEvent.Action) {
                case ACTION_ADD:
                    // call super for actionlistener execution
                    // it's up to the handler to get the results from the getSelectedResults() method
                    super.broadcast(event);
                    break;

                case ACTION_CLEAR:
                    this.contains = "";
                    this.filterIndex = 0;
                    this.selectedResults = null;
                    this.currentResults = null;
                    break;

                case ACTION_FILTER:
                    // filter changed then query with new settings
                case ACTION_SEARCH:
                    // query with current settings
                    MethodBinding callback = getQueryCallback();
                    if (callback != null) {
                        // use reflection to execute the query callback method and retrieve results
                        Object result = callback.invoke(getFacesContext(), new Object[]{
                                this.filterIndex, this.contains.trim()});

                        if (result instanceof SelectItem[]) {
                            this.currentResults = (SelectItem[]) result;
                        } else {
                            this.currentResults = null;
                        }
                    }
                    break;
            }
        } else {
            super.broadcast(event);
        }
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    public void encodeBegin(FacesContext context) throws IOException {
        if (isRendered() == false) {
            return;
        }

        ResponseWriter out = context.getResponseWriter();

        ResourceBundle bundle = Application.getBundle(context);

        String clientId = getClientId(context);

        // start outer table
        out.write("<table border=0 cellspacing=4 cellpadding=0>");

        // top row
        out.write("<tr valign=top><td>");

        // filter drop-down
        if (getShowFilter() == true) {
            out.write("<select name='");
            out.write(clientId + FIELD_FILTER);
            out.write("' size='1'");

            // apply onchange Form submit here if component attributes require it
            if (getFilterRefresh() == true) {
                out.write(" onchange=\"");
                out.write(generateFormSubmit(context, ACTION_FILTER));
                out.write("\"");
            }

            out.write(">");

            // output filter options
            SelectItem[] items = getFilterOptions();
            if (items != null) {
                for (int i = 0; i < items.length; i++) {
                    out.write("<option value=\"");
                    out.write(items[i].getValue().toString());
                    if (this.filterIndex != i) {
                        out.write("\">");
                    } else {
                        out.write("\" selected=\"true\">");
                    }
                    out.write(Utils.encode(items[i].getLabel()));
                    out.write("</option>");
                }
            }

            out.write("</select>");
        }
        out.write("</td></tr><tr><td align=right>");

        // Contains textbox
        if (getShowContains() == true) {
            out.write("<input name='");
            out.write(clientId + FIELD_CONTAINS);
            out.write("' type='text' maxlength='256' style='width:");
            out.write(getShowFilter() ? "300" : "360");
            out.write("px' value=\"");
            out.write(Utils.encode(this.contains));
            out.write("\">&nbsp;");
        }

        // Search button
        out.write("<input type='submit' value='");
        String msg = getSearchButtonLabel();
        if (msg == null || msg.length() == 0) {
            msg = bundle.getString(MSG_SEARCH);
        }
        out.write(Utils.encode(msg));
        out.write("' onclick=\"");
        out.write(generateFormSubmit(context, ACTION_SEARCH));
        out.write("\">");
        out.write("</td></tr>");

        // information row
        if (this.currentResults != null && getShowContains() == true) {
            out.write("<tr><td colspan=3>");
            String resultsMsg;
            if (getShowFilter() == false) {
                resultsMsg = MessageFormat.format(bundle.getString(MSG_RESULTS1),
                        Utils.encode(this.contains));
            } else {
                String filterMsg = this.filters[this.filterIndex].getLabel();
                resultsMsg = MessageFormat.format(bundle.getString(MSG_RESULTS2),
                        Utils.encode(this.contains), filterMsg);
            }
            out.write(resultsMsg);
            out.write("&nbsp;");
            out.write("<a href='#' onclick=\"");
            out.write(generateFormSubmit(context, ACTION_CLEAR));
            out.write("\">");
            out.write(Utils.encode(bundle.getString(MSG_CLEAR)));
            out.write("</a></td></tr>");
        } else {
            out.write("<tr><td>");
            out.write(MessageFormat.format(bundle.getString(MSG_RESULTS_TITLE), new Object[]{}));
            out.write("</td></tr>");
        }

        // results list row
        out.write("<tr><td colspan=2>");
        out.write("<select size='8' style='");
        out.write((currentResults != null && currentResults.length != 0) ? "min-width:" : "width:");
        out.write(Integer.toString(getWidth()));
        out.write("px;height:");
        out.write(Integer.toString(getHeight()));
        out.write("px' name='");
        out.write(clientId + FIELD_RESULTS);
        out.write("' id='");
        out.write(clientId + FIELD_RESULTS);
        out.write("'");
        if (getMultiSelect() == true) {
            out.write(" multiple");
        }
        out.write(">");

        // results
        if (currentResults != null) {
            // show each of the items in the results listbox
            for (SelectItem currentResult : currentResults) {
                if (currentResult.isDisabled()) {
                    // attribute disable doesn't work in IE7. Use optgroup as workaround
                    out.write("<optgroup label=\"");
                    out.write(Utils.encode(currentResult.getLabel()));
                    out.write("\" style=\"color:#CCCCCC\" >");
                    out.write("</optgroup>");
                } else {
                    out.write("<option value=\"");
                    out.write(currentResult.getValue().toString());
                    out.write("\">");
                    out.write(Utils.encode(currentResult.getLabel()));
                    out.write("</option>");
                }
            }
        }

        // end results list
        out.write("</select>");
        out.write("</td></tr>");

        // bottom row - add button
        if (getShowAddButton() == true) {
            out.write("<tr><td colspan=2>");
            out.write("<input type='submit' value='");
            msg = getAddButtonLabel();
            if (msg == null || msg.length() == 0) {
                msg = bundle.getString(MSG_ADD);
            }
            out.write(Utils.encode(msg));
            out.write("' onclick=\"");
            out.write(generateFormSubmit(context, ACTION_ADD));
            out.write("\">");
            out.write("</td></tr>");
        }

        // end outer table
        out.write("</table>");
    }

    /**
     * @return the filter options
     */
    public SelectItem[] getFilterOptions() {
        if (this.filters == null) {
            ValueBinding vb = (ValueBinding) getValueBinding("filters");
            if (vb != null) {
                this.filters = (SelectItem[]) vb.getValue(getFacesContext());
            }
        }

        return this.filters;
    }

    /**
     * @return current filter drop-down selected index value
     */
    public int getFilterIndex() {
        return this.filterIndex;
    }

    /**
     * @return Returns the searchButtonLabel.
     */
    public String getSearchButtonLabel() {
        ValueBinding vb = getValueBinding("searchButtonLabel");
        if (vb != null) {
            this.searchButtonLabel = (String) vb.getValue(getFacesContext());
        }

        return this.searchButtonLabel;
    }

    /**
     * @param searchButtonLabel The searchButtonLabel to set.
     */
    public void setSearchButtonLabel(String searchButtonLabel) {
        this.searchButtonLabel = searchButtonLabel;
    }

    /**
     * @return Returns the addButtonLabel.
     */
    public String getAddButtonLabel() {
        ValueBinding vb = getValueBinding("addButtonLabel");
        if (vb != null) {
            this.addButtonLabel = (String) vb.getValue(getFacesContext());
        }

        return this.addButtonLabel;
    }

    /**
     * @param addButtonLabel The addButtonLabel to set.
     */
    public void setAddButtonLabel(String addButtonLabel) {
        this.addButtonLabel = addButtonLabel;
    }

    /**
     * @return Returns the showAddButton.
     */
    public boolean getShowAddButton() {
        ValueBinding vb = getValueBinding("showAddButton");
        if (vb != null) {
            this.showAddButton = (Boolean) vb.getValue(getFacesContext());
        }

        return showAddButton != null ? showAddButton : true;
    }

    /**
     * @param showAddButton The showAddButton to set.
     */
    public void setShowAddButton(boolean showAddButton) {
        this.showAddButton = showAddButton;
    }

    /**
     * @return Returns the showContains.
     */
    public boolean getShowContains() {
        ValueBinding vb = getValueBinding("showContains");
        if (vb != null) {
            this.showContains = (Boolean) vb.getValue(getFacesContext());
        }

        return showContains != null ? showContains : true;
    }

    /**
     * @param showContains The showContains to set.
     */
    public void setShowContains(boolean showContains) {
        this.showContains = showContains;
    }

    /**
     * @return Returns the showFilter.
     */
    public boolean getShowFilter() {
        ValueBinding vb = getValueBinding("showFilter");
        if (vb != null) {
            this.showFilter = (Boolean) vb.getValue(getFacesContext());
        }

        return showFilter != null ? showFilter : true;
    }

    /**
     * @param showFilter The showFilter to set.
     */
    public void setShowFilter(boolean showFilter) {
        this.showFilter = showFilter;
    }

    /**
     * @return Returns the filterRefresh.
     */
    public boolean getFilterRefresh() {
        ValueBinding vb = getValueBinding("filterRefresh");
        if (vb != null) {
            this.filterRefresh = (Boolean) vb.getValue(getFacesContext());
        }

        return filterRefresh != null ? filterRefresh : false;
    }

    /**
     * @param filterRefresh The filterRefresh to set.
     */
    public void setFilterRefresh(boolean filterRefresh) {
        this.filterRefresh = filterRefresh;
    }

    /**
     * @return true if multi select should be enabled.
     */
    public boolean getMultiSelect() {
        ValueBinding vb = getValueBinding("multiSelect");
        if (vb != null) {
            this.multiSelect = (Boolean) vb.getValue(getFacesContext());
        }

        return multiSelect != null ? multiSelect : true;
    }

    /**
     * @param multiSelect Flag to determine whether multi select is enabled
     */
    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    /**
     * @return Returns the width.
     */
    public int getWidth() {
        ValueBinding vb = getValueBinding("width");
        if (vb != null) {
            this.width = (Integer) vb.getValue(getFacesContext());
        }

        return width != null ? width : DEFAULT_WIDTH;
    }

    /**
     * @param width The width to set.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return Returns the height.
     */
    public int getHeight() {
        ValueBinding vb = getValueBinding("height");
        if (vb != null) {
            this.height = (Integer) vb.getValue(getFacesContext());
        }

        return height != null ? height : DEFAULT_HEIGHT;
    }

    /**
     * @param height The height to set.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return Returns the queryCallback.
     */
    public MethodBinding getQueryCallback() {
        return this.queryCallback;
    }

    /**
     * @param binding The queryCallback MethodBinding to set.
     */
    public void setQueryCallback(MethodBinding binding) {
        this.queryCallback = binding;
    }

    /**
     * @return The selected results. An array of whatever string objects were attached to the
     * SelectItem[] objects supplied as the result of the picker query.
     */
    public String[] getSelectedResults() {
        return this.selectedResults;
    }

    // ------------------------------------------------------------------------------
    // Private helpers

    /**
     * We use a hidden field per picker instance on the page.
     *
     * @return hidden field name
     */
    private String getHiddenFieldName() {
        return getClientId(getFacesContext());
    }

    /**
     * Generate FORM submit JavaScript for the specified action
     *
     * @param context FacesContext
     * @param action  Action index
     * @return FORM submit JavaScript
     */
    private String generateFormSubmit(FacesContext context, int action) {
        return Utils.generateFormSubmit(context, this, getHiddenFieldName(), Integer.toString(action));
    }

    // ------------------------------------------------------------------------------
    // Inner classes

    /**
     * Class representing the an action relevant to the Generic Selector component.
     */
    @SuppressWarnings("serial")
    public static class PickerEvent extends ActionEvent {

        public int Action;
        public int FilterIndex;
        public String Contains;
        private String[] Results;
        public PickerEvent(UIComponent component, int action, int filterIndex, String contains,
                           String[] results) {
            super(component);
            Action = action;
            FilterIndex = filterIndex;
            Contains = contains;

            if (results == null) {
                Results = null;
            } else {
                Results = results.clone();
            }
        }

        public String[] getResults() {
            return Results;
        }
    }
}
