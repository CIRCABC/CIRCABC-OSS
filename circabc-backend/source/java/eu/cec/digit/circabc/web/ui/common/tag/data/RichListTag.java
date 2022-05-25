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
package eu.cec.digit.circabc.web.ui.common.tag.data;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

import javax.faces.component.UIComponent;


/**
 * @author Guillaume
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 BaseComponentTag was moved to Spring Surf. Class
 * implementation didn't change between Alfresco versions.
 */
public class RichListTag extends BaseComponentTag {
    // ------------------------------------------------------------------------------
    // Component methods

    /**
     * the refreshOnBind
     */
    private String refreshOnBind;
    /**
     * the header row CSS Class
     */
    private String headerStyleClass;
    /**
     * the row CSS Class
     */
    private String rowStyleClass;
    /**
     * the alternate row CSS Class
     */
    private String altRowStyleClass;

    // ------------------------------------------------------------------------------
    // Bean implementation
    /**
     * the styleClass
     */
    private String styleClass;
    /**
     * the value
     */
    private String value;
    /**
     * the var
     */
    private String var;
    /**
     * the viewMode
     */
    private String viewMode;
    /**
     * the pageSize
     */
    private String pageSize;
    /**
     * the initialSortColumn
     */
    private String initialSortColumn;
    /**
     * the initialSortDescending
     */
    private String initialSortDescending;
    /**
     * the listConfig
     */
    private String listConfig;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.RichList";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        return "eu.cec.digit.circabc.faces.RichListRenderer";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.value = null;
        this.var = null;
        this.initialSortColumn = null;
        this.initialSortDescending = null;
        this.listConfig = null;
        this.viewMode = null;
        this.styleClass = null;
        this.rowStyleClass = null;
        this.altRowStyleClass = null;
        this.headerStyleClass = null;
        this.pageSize = null;
        this.refreshOnBind = null;

    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(final UIComponent component) {
        super.setProperties(component);
        setStringBindingProperty(component, "value", this.value);
        setStringStaticProperty(component, "var", this.var);
        setStringProperty(component, "initialSortColumn", this.initialSortColumn);
        setBooleanProperty(component, "initialSortDescending", this.initialSortDescending);
        setStringProperty(component, "listConfig", this.listConfig);
        setStringProperty(component, "viewMode", this.viewMode);
        setStringProperty(component, "styleClass", this.styleClass);
        setStringProperty(component, "rowStyleClass", this.rowStyleClass);
        setStringProperty(component, "altRowStyleClass", this.altRowStyleClass);
        setStringProperty(component, "headerStyleClass", this.headerStyleClass);
        setIntProperty(component, "pageSize", this.pageSize);
        setBooleanProperty(component, "refreshOnBind", this.refreshOnBind);
    }

    // ------------------------------------------------------------------------------
    // Private data

    /**
     * Set the viewMode
     *
     * @param viewMode the viewMode
     */
    public void setViewMode(final String viewMode) {
        this.viewMode = viewMode;
    }

    /**
     * Set the pageSize
     *
     * @param pageSize the pageSize
     */
    public void setPageSize(final String pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Set the initialSortColumn
     *
     * @param initialSortColumn the initialSortColumn
     */
    public void setInitialSortColumn(final String initialSortColumn) {
        this.initialSortColumn = initialSortColumn;
    }

    /**
     * Set the initialSortDescending
     *
     * @param initialSortDescending the initialSortDescending
     */
    public void setInitialSortDescending(final String initialSortDescending) {
        this.initialSortDescending = initialSortDescending;
    }

    /**
     * Set the listConfig
     *
     * @param listConfig the listConfig
     */
    public void setListConfig(final String listConfig) {
        this.listConfig = listConfig;
    }

    /**
     * Set the value
     *
     * @param value the value
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Set the var
     *
     * @param var the var
     */
    public void setVar(final String var) {
        this.var = var;
    }

    /**
     * Set the styleClass
     *
     * @param styleClass the styleClass
     */
    public void setStyleClass(final String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * Set the the row CSS Class
     *
     * @param rowStyleClass the the row CSS Class
     */
    public void setRowStyleClass(final String rowStyleClass) {
        this.rowStyleClass = rowStyleClass;
    }

    /**
     * Set the alternate row CSS Class
     *
     * @param altRowStyleClass the alternate row CSS Class
     */
    public void setAltRowStyleClass(final String altRowStyleClass) {
        this.altRowStyleClass = altRowStyleClass;
    }

    /**
     * Set the header row CSS Class
     *
     * @param headerStyleClass the header row CSS Class
     */
    public void setHeaderStyleClass(final String headerStyleClass) {
        this.headerStyleClass = headerStyleClass;
    }

    /**
     * Set the refreshOnBind flag. True to force the list to retrieve bound data on bind().
     *
     * @param refreshOnBind the refreshOnBind
     */
    public void setRefreshOnBind(String refreshOnBind) {
        this.refreshOnBind = refreshOnBind;
    }
}
