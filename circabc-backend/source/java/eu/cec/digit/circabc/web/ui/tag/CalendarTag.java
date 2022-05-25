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
package eu.cec.digit.circabc.web.ui.tag;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

import javax.faces.component.UIComponent;

/**
 * The CalendarTag provides a simple and easy way to generate an interactive calendar in your java
 * enabled pages.
 * <p>
 * The library provides quick implementation and fully customizable look and feel.
 *
 * <code>
 * <i>Example:</i>  Using literals to define the calendar:
 * <calendartag:calendar month="9" day="19" year="2004" />
 * <p>
 * Using an object to define the calendar: <calendartag:calendar date="${date}" />
 * <p>
 * Using a custom date range: <calendartag:calendar startDate="${startDate}" endDate="${endDate}"
 * />
 * <p>
 * Using a date object with a custom date range: <calendartag:calendar date="${date}"
 * startDate="${startDate}" endDate="${endDate}" />
 * </code>
 *
 * @author Yanick Pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 BaseComponentTag was moved to Spring Surf. This class is a
 * customization of the calendartag-1.0.1.jar that didn't change on this version of CircaBC
 */
public class CalendarTag extends BaseComponentTag {

    private String dayWidth;
    private String dayHeight;
    private String id;
    private String cssPrefix;
    private String weekStart;
    private String day;
    private String month;
    private String year;
    private String action;
    private String actionListener;
    private String viewMode;
    private String date;
    private String startDate;
    private String endDate;
    private String decorator;
    private String showPreviousNextLinks;
    private String beyond;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.Calendar";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        // the component is self renderering
        return null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        setIntProperty(component, "dayWidth", this.dayWidth);
        setIntProperty(component, "dayHeight", this.dayHeight);
        setStringProperty(component, "id", this.id);
        setStringProperty(component, "cssPrefix", this.cssPrefix);
        setStringProperty(component, "weekStart", this.weekStart);
        setIntProperty(component, "day", this.day);
        setIntProperty(component, "month", this.month);
        setIntProperty(component, "year", this.year);
        setStringProperty(component, "action", this.action);
        setStringProperty(component, "actionListener", this.actionListener);
        setStringBindingProperty(component, "viewMode", this.viewMode);
        setStringBindingProperty(component, "date", this.date);
        setStringBindingProperty(component, "startDate", this.startDate);
        setStringBindingProperty(component, "endDate", this.endDate);
        setStringProperty(component, "decorator", this.decorator);
        setBooleanProperty(component, "showPreviousNextLinks", this.showPreviousNextLinks);
        setBooleanProperty(component, "beyond", this.beyond);

    }

    /**
     * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
     */
    public void release() {
        super.release();

        this.dayWidth = null;
        this.dayHeight = null;
        this.id = null;
        this.cssPrefix = null;
        this.weekStart = null;
        this.day = null;
        this.month = null;
        this.year = null;
        this.action = null;
        this.actionListener = null;
        this.viewMode = null;
        this.date = null;
        this.startDate = null;
        this.endDate = null;
        this.decorator = null;
        this.showPreviousNextLinks = null;
        this.beyond = null;
    }

    /**
     * @param cssPrefix the cssPrefix to set
     */
    public void setCssPrefix(String cssPrefix) {
        this.cssPrefix = cssPrefix;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @param day the day to set
     */
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * @param dayHeight the dayHeight to set
     */
    public void setDayHeight(String dayHeight) {
        this.dayHeight = dayHeight;
    }

    /**
     * @param dayWidth the dayWidth to set
     */
    public void setDayWidth(String dayWidth) {
        this.dayWidth = dayWidth;
    }

    /**
     * @param decorator the decorator to set
     */
    public void setDecorator(String decorator) {
        this.decorator = decorator;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(String month) {
        this.month = month;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @param actionListener the actionListener to set
     */
    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * @param viewMode the viewMode to set
     */
    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
    }

    /**
     * @param showPreviousNextLinks the showPreviousNextLinks to set
     */
    public void setShowPreviousNextLinks(String showPreviousNextLinks) {
        this.showPreviousNextLinks = showPreviousNextLinks;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * @param weekStart the weekStart to set
     */
    public void setWeekStart(String weekStart) {
        this.weekStart = weekStart;
    }

    /**
     * @param year the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @param beyond the beyond to set
     */
    public void setBeyond(String beyond) {
        this.beyond = beyond;
    }
}
