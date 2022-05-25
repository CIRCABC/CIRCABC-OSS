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

import eu.cec.digit.circabc.web.ui.common.component.UIInputDatePicker;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.renderer.BaseRenderer;
import org.apache.myfaces.shared_impl.renderkit.html.HTML;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.*;

/**
 * Renderer that displays a date picker
 *
 * @author Guillaume
 */
public class DatePickerRenderer extends BaseRenderer {

    /**
     * the renderer type
     */
    public static final String RENDERER_TYPE = "eu.cec.digit.circabc.faces.DatePickerRenderer";

    /**
     * Suffix for input select drop down managing year
     */
    private static final String FIELD_YEAR = "_year";

    /**
     * Suffix for input select drop down managing month
     */
    private static final String FIELD_MONTH = "_month";

    /**
     * Suffix for input select drop down managing day
     */
    private static final String FIELD_DAY = "_day";

    /**
     * Suffix for input text managing hour
     */
    private static final String FIELD_HOUR = "_hour";

    /**
     * Suffix for input text managing minute
     */
    private static final String FIELD_MINUTE = "_minute";

    /**
     * Suffix for input button command today
     */
    private static final String FIELD_CMD_TODAY = "_cmd_today";

    /**
     * Suffix for input button command today
     */
    private static final String FIELD_CMD_NONE = "_cmd_none";

    /**
     * Suffix for input button command today
     */
    private static final String FIELD_CMD_SET = "_cmd_set";


    /**
     * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     * <p>
     * The decode method takes the parameters from the external requests, finds the ones revelant to
     * this component and decodes the results into an object known as the "submitted value".
     */
    public void decode(FacesContext context, UIComponent component) {
        try {
            UIInputDatePicker datePicker = (UIInputDatePicker) component;
            // check if we have the right to change the value
            if (!datePicker.getDisabled()) {

                String clientId = datePicker.getClientId(context);
                Map params = context.getExternalContext().getRequestParameterMap();

                // see if a command was invoked - Check fo today button
                if (((String) params.get(clientId + FIELD_CMD_TODAY)) != null
                        && ((String) params.get(clientId + FIELD_CMD_TODAY)).length() > 0) {
                    // Click on today
                    datePicker.setValue(new Date());
                } else if (((String) params.get(clientId + FIELD_CMD_NONE)) != null
                        && ((String) params.get(clientId + FIELD_CMD_NONE)).length() > 0) {
                    // Click on none - Open version
                    datePicker.setValue(null);
                } else if (((String) params.get(clientId + FIELD_CMD_SET)) != null
                        && ((String) params.get(clientId + FIELD_CMD_SET)).length() > 0) {
                    // Click on none - Closed version
                    datePicker.setValue(new Date());
                } else {
                    // a command was not invoked so decode the date the user set (if present)

                    int year = Integer.parseInt((String) params.get(clientId + FIELD_YEAR));
                    if (year >= datePicker.getStartYear()) {
                        // found data for our component
                        int month = Integer.parseInt((String) params.get(clientId + FIELD_MONTH));
                        int day = Integer.parseInt((String) params.get(clientId + FIELD_DAY));
                        int hour = Integer.parseInt((String) params.get(clientId + FIELD_HOUR));
                        int minute = Integer.parseInt((String) params.get(clientId + FIELD_MINUTE));

                        Calendar date = new GregorianCalendar(year, month, day, hour, minute);
                        datePicker.setValue(date.getTime());
                        datePicker.setValueFinal(date.getTime());
                    }
                }
            }
        } catch (NumberFormatException nfe) {
            // just ignore the error and skip the update of the property
        }
    }

    /**
     * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     * <p>
     * All rendering logic for this component is implemented here. A renderer for an input component
     * must render the submitted value if it's set, and use the local value only if there is no
     * submitted value.
     */
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        // always check for this flag - as per the spec
        if (component.isRendered()) {
            UIInputDatePicker datePicker = (UIInputDatePicker) component;

            Date date = null;
            String clientId = component.getClientId(context);
            ResponseWriter out = context.getResponseWriter();
            boolean initIfNull = datePicker.getInitialiseIfNull();
            boolean disabled = datePicker.getDisabled();
            boolean showTime = datePicker.getShowTime();
            boolean showDate = datePicker.getShowDate();
            boolean timeAsList = (showTime) ? datePicker.getTimeAsList() : false;
            int step = 15;
            if (showTime && timeAsList) {
                step = datePicker.getStep();
            }

            date = datePicker.getValue();
            if (date == null && initIfNull) {
                date = new Date();
            }

            if (date != null) {
                // now we render the output for our component
                // we create 3 drop-down menus for day, month and year and
                // two text fields for the hour and minute

                // note that we build a client id for our form elements that we are then
                // able to decode() as above.
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(date);
                if (showDate) {
                    renderMenu(out, datePicker, getDays(), calendar.get(Calendar.DAY_OF_MONTH),
                            clientId + FIELD_DAY);
                    renderMenu(out, datePicker, getMonths(), calendar.get(Calendar.MONTH),
                            clientId + FIELD_MONTH);
                    renderMenu(out, datePicker,
                            getYears(datePicker.getStartYear(), datePicker.getYearCount()),
                            calendar.get(Calendar.YEAR), clientId + FIELD_YEAR);
                    out.write("&nbsp;");
                } else {
                    renderTimeElement(out, datePicker, calendar.get(Calendar.DAY_OF_MONTH),
                            clientId + FIELD_DAY, false);
                    renderTimeElement(out, datePicker, calendar.get(Calendar.MONTH), clientId + FIELD_MONTH,
                            false);
                    renderTimeElement(out, datePicker, calendar.get(Calendar.YEAR), clientId + FIELD_YEAR,
                            false);
                }

                int minute = calendar.get(Calendar.MINUTE);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (timeAsList) {
                    int roundMinute = roundMinute(minute, step);
                    int roundHour = hour + roundHour(minute, step);

                    renderMenu(out, datePicker, getHours(), roundHour, clientId + FIELD_HOUR);
                    out.write("&nbsp;:&nbsp;");
                    renderMenu(out, datePicker, getMinutes(step), roundMinute, clientId + FIELD_MINUTE);
                } else {
                    renderTimeElement(out, datePicker, hour, clientId + FIELD_HOUR, showTime);
                    if (showTime) {
                        out.write("&nbsp;:&nbsp;");
                    }
                    renderTimeElement(out, datePicker, minute, clientId + FIELD_MINUTE, showTime);
                }

                out.write("&nbsp;");

                // render 2 links (if the component is not disabled) to allow the user to reset the
                // date back to null (if initialiseIfNull is false) or to select today's date
                if (!disabled && showDate) {
                    out.write("<input type=\"submit\" name=\"");
                    out.write(clientId + FIELD_CMD_TODAY);
                    out.write("\" value=\"");
                    out.write(Application.getMessage(context, "date_today"));
                    out.write("\">&nbsp;");

                    if (!initIfNull) {
                        out.write("<input type=\"submit\" name=\"");
                        out.write(clientId + FIELD_CMD_NONE);
                        out.write("\" value=\"");
                        out.write(Application.getMessage(context, "date_none"));
                        out.write("\">");
                    }
                }
            } else {
                // Render a link indicating there isn't a date set (unless the property is disabled)
                // work out which label to use
                String noneLabel = datePicker.getNoneLabel();
                if (noneLabel == null || noneLabel.length() == 0) {
                    noneLabel = Application.getMessage(context, "date_none");
                }

                if (!disabled) {
                    out.write("<input type=\"submit\" name=\"");
                    out.write(clientId + FIELD_CMD_SET);
                    out.write("\" value=\"");
                    // out.write(Application.getMessage(context, "click_to_set_date"));
                    out.write(noneLabel);
                    out.write("\">");
                } else {
                    out.write(noneLabel);
                }

            }
        }
    }

    private int roundHour(int minute, int step) {

        return ((minute > 60 - (step / 2)) && (minute < 60)) ? 1 : 0;
    }

    private int roundMinute(int minute, int step) {
        if ((60 % step != 0) || (60 <= step) || (step < 1)) {
            throw new IllegalArgumentException("step should be in {1,2,3,4,5,6,10,12,15,20,30} ");
        }
        if ((minute < 0) || (minute > 59)) {
            throw new IllegalArgumentException("minute should greater or equal 0 and less then 60");
        }
        int result = Math.round((float) minute / (float) step) * step;
        return result;
    }

    private String trimTwoCars(int intToTrim) {
        return ((intToTrim < 10) ? "0" : "") + String.valueOf(intToTrim);
    }

    /**
     * Render a drop-down menu to represent an element for the date picker.
     *
     * @param out       Response Writer to output too
     * @param component The compatible component
     * @param items     To display in the drop-down list
     * @param selected  Which item index is selected
     * @param clientId  Client Id to use
     */
    private void renderMenu(ResponseWriter out, UIInputDatePicker component, List<SelectItem> items,
                            int selected, String clientId) throws IOException {

        out.write("<select");
        outputAttribute(out, clientId, HTML.NAME_ATTR);

        if (component.getAttributes().get("styleClass") != null) {
            outputAttribute(out, component.getAttributes().get("styleClass"), HTML.STYLE_CLASS_ATTR);
        }
        if (component.getDisabled()) {
            outputAttribute(out, HTML.DISABLED_ATTR, HTML.DISABLED_ATTR);
        }
        out.write(">");

        for (SelectItem item : items) {
            Integer value = (Integer) item.getValue();
            out.write("<option");
            outputAttribute(out, value, HTML.VALUE_ATTR);

            // show selected value
            if (value == selected) {
                outputAttribute(out, "selected", HTML.SELECTED_ATTR);
            }
            out.write(">");
            out.write(item.getLabel());
            out.write("</option>");
        }

        out.write("</select>");
    }

    /**
     * Renders either the hour or minute field
     *
     * @param out          The ResponseWriter
     * @param currentValue The value of the hour or minute
     * @param clientId     The id to use for the field
     */
    private void renderTimeElement(ResponseWriter out, UIInputDatePicker component, int currentValue,
                                   String clientId, boolean showTime) throws IOException {
        out.write("<input");
        outputAttribute(out, clientId, HTML.NAME_ATTR);

        if (showTime) {
            out.write(" type='text' size='1' maxlength='2'");

            if (component.getDisabled()) {
                outputAttribute(out, HTML.DISABLED_ATTR, HTML.DISABLED_ATTR);
            }
        } else {
            out.write(" type='hidden'");
        }

        // make sure there are always 2 digits
        String strValue = Integer.toString(currentValue);
        if (strValue.length() == 1) {
            strValue = "0" + strValue;
        }

        outputAttribute(out, strValue, HTML.VALUE_ATTR);
        out.write("/>");
    }

    private List<SelectItem> getYears(int startYear, int yearCount) {
        List<SelectItem> years = new ArrayList<>();
        for (int i = startYear; i < startYear + yearCount; i++) {
            Integer year = i;
            years.add(new SelectItem(year, year.toString()));
        }
        return years;
    }

    private List<SelectItem> getMonths() {
        // get names of the months for default locale
        Locale locale = Application.getLanguage(FacesContext.getCurrentInstance());
        if (locale == null) {
            locale = Locale.getDefault();
        }
        DateFormatSymbols dfs = new DateFormatSymbols(locale);
        String[] names = dfs.getMonths();
        List<SelectItem> months = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            Integer key = i;
            months.add(new SelectItem(key, names[i]));
        }
        return months;
    }

    private List<SelectItem> getDays() {
        List<SelectItem> days = new ArrayList<>(31);
        for (int i = 1; i < 32; i++) {
            Integer day = i;
            days.add(new SelectItem(day, trimTwoCars(day)));
        }
        return days;
    }

    private List<SelectItem> getHours() {
        List<SelectItem> hours = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            Integer hour = i;
            hours.add(new SelectItem(hour, trimTwoCars(hour)));
        }
        return hours;
    }

    private List<SelectItem> getMinutes(int step) {
        final int size = 60 / step;
        List<SelectItem> minutes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final int currentMinute = i * step;
            minutes.add(new SelectItem(currentMinute, String.valueOf(currentMinute)));
        }
        return minutes;
    }
}
