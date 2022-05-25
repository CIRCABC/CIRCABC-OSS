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
package eu.cec.digit.circabc.web.ui.common.tag;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

import javax.faces.component.UIComponent;

/**
 * Tag handler for an Input UI Component specific for Date input.
 * <p>
 * This tag collects the user params needed to specify an Input component to allow the user to enter
 * a date. It specifies the renderer as below to be our Date specific renderer.
 *
 * @author Guillaume
 */
public class InputDatePickerTag extends HtmlComponentTag {

    private String startYear = null;
    private String yearCount = null;
    private String value = null;
    private String showTime = null;
    private String disabled = null;
    private String initialiseIfNull = null;
    private String noneLabel = null;
    private String showDate = null;
    private String timeAsList = null;
    private String step = null;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.InputDatePicker";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        return "eu.cec.digit.circabc.faces.DatePickerRenderer";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.startYear = null;
        this.yearCount = null;
        this.value = null;
        this.showTime = null;
        this.disabled = null;
        this.initialiseIfNull = null;
        this.noneLabel = null;
        this.showDate = null;
        this.timeAsList = null;
        this.step = null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        // set the properties of tag into the component
        setIntProperty(component, "startYear", this.startYear);
        setIntProperty(component, "yearCount", this.yearCount);
        setStringProperty(component, "value", this.value);
        setStringProperty(component, "noneLabel", this.noneLabel);
        setBooleanProperty(component, "showTime", this.showTime);
        setBooleanProperty(component, "disabled", this.disabled);
        setBooleanProperty(component, "initialiseIfNull", this.initialiseIfNull);
        setBooleanProperty(component, "showDate", this.showDate);
        setBooleanProperty(component, "timeAsList", this.timeAsList);
        setIntProperty(component, "step", this.step);
    }

    /**
     * Set the value
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Set the startYear
     *
     * @param startYear the startYear
     */
    public void setStartYear(String startYear) {
        this.startYear = startYear;
    }

    /**
     * Set the yearCount
     *
     * @param yearCount the yearCount
     */
    public void setYearCount(String yearCount) {
        this.yearCount = yearCount;
    }

    /**
     * Determines whether the time is rendered
     *
     * @param showTime true to allow the time to be edited
     */
    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    /**
     * Determines whether the date is rendered
     *
     * @param date true to allow the time to be edited
     */
    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    /**
     * Determines whether the time is rendered as list with 15 min of interval
     *
     * @param date timeAsList
     */
    public void setTimeAsList(String timeAsList) {
        this.timeAsList = timeAsList;
    }

    /**
     * Sets whether the component should be rendered in a disabled state
     *
     * @param disabled true to render the component in a disabled state
     */
    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }

    /**
     * Sets whether today's date should be shown initially if the underlying model value is null. This
     * will also hide the None button thus disallowing the user to set the date back to null.
     *
     * @param initialiseIfNull true to show today's date instead of 'None'
     */
    public void setInitialiseIfNull(String initialiseIfNull) {
        this.initialiseIfNull = initialiseIfNull;
    }

    /**
     * Sets the explicit label to use when there is no date set
     *
     * @param noneLabel 'None' label to use
     */
    public void setNoneLabel(String noneLabel) {
        this.noneLabel = noneLabel;
    }

    public void setStep(String step) {
        this.step = step;
    }
}
