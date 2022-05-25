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
package eu.cec.digit.circabc.web.wai.generator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.renderer.DatePickerRenderer;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.*;

/**
 * Renderer that displays a date picker
 *
 * @author Guillaume
 */
public class DatePickerGeneratorRenderer extends DatePickerRenderer {

    private static final String FIELD_YEAR = "_year";
    private static final String FIELD_MONTH = "_month";
    private static final String FIELD_DAY = "_day";
    private static final String FIELD_HOUR = "_hour";
    private static final String FIELD_MINUTE = "_minute";
    private static final String FIELD_CMD = "_cmd";

    @SuppressWarnings("deprecation")
    private static final int DEFAULT_START_YEAR = new Date().getYear() + 1900 + 2;

    private static final int DEFAULT_YEAR_COUNT = 25;

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
//				 TODO: should check for disabled/readonly here - no need to decode

            String clientId = component.getClientId(context);
            Map params = context.getExternalContext().getRequestParameterMap();

            int[] parts = new int[5];

            Calendar now = Calendar.getInstance();
            parts[0] = now.get(Calendar.YEAR);
            parts[1] = now.get(Calendar.MONTH);
            parts[2] = now.get(Calendar.DAY_OF_MONTH);
            parts[3] = now.get(Calendar.HOUR_OF_DAY);
            parts[4] = now.get(Calendar.MINUTE);

            // see if a command was invoked - Check fo today button
            if (((String) params.get(clientId + FIELD_CMD_TODAY)) != null
                    && ((String) params.get(clientId + FIELD_CMD_TODAY)).length() > 0) {
                // Click on today
                ((EditableValueHolder) component).setSubmittedValue(parts);

            } else if (((String) params.get(clientId + FIELD_CMD_NONE)) != null
                    && ((String) params.get(clientId + FIELD_CMD_NONE)).length() > 0) {
                // Click on none - Open version
                // set the submitted value to be null
                ((EditableValueHolder) component).setSubmittedValue(null);

                // set the component value to be null too
                ((EditableValueHolder) component).setValue(null);

            } else if (((String) params.get(clientId + FIELD_CMD_SET)) != null
                    && ((String) params.get(clientId + FIELD_CMD_SET)).length() > 0) {
                // Click on none - Closed version
                ((EditableValueHolder) component).setSubmittedValue(parts);

            } else {
                // a command was not invoked so decode the date the user set (if present)
                String yearStr = (String) params.get(clientId + FIELD_YEAR);
                if (yearStr != null) {
                    // save the data in an object for our component as the "EditableValueHolder"
                    // all UI Input Components support this interface for the submitted value
                    // found data for our component
                    parts[0] = Integer.parseInt((String) params.get(clientId + FIELD_YEAR));
                    parts[1] = Integer.parseInt((String) params.get(clientId + FIELD_MONTH));
                    parts[2] = Integer.parseInt((String) params.get(clientId + FIELD_DAY));
                    parts[3] = Integer.parseInt((String) params.get(clientId + FIELD_HOUR));
                    parts[4] = Integer.parseInt((String) params.get(clientId + FIELD_MINUTE));

                    ((EditableValueHolder) component).setSubmittedValue(parts);
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

            Date date = null;
            String clientId = component.getClientId(context);
            ResponseWriter out = context.getResponseWriter();
            Boolean initIfNull = (Boolean) component.getAttributes().get("initialiseIfNull");

            // this is part of the spec:
            // first you attempt to build the date from the submitted value
            int[] submittedValue = (int[]) ((EditableValueHolder) component).getSubmittedValue();
            if (submittedValue != null) {
                date = (Date) getConvertedValue(context, component, submittedValue);
            } else {
                // second - if no submitted value is found, default to the current value
                Object value = ((ValueHolder) component).getValue();
                if (value instanceof Date) {
                    date = (Date) value;
                } else if (value != null) {
                    try {
                        date = ISO8601DateFormat.parse((String) value);
                    } catch (AlfrescoRuntimeException ignore) {
                    }
                }

                // third - if no date is present and the initialiseIfNull attribute
                // is set to true set the date to today's date
                if (date == null && initIfNull != null && initIfNull) {
                    date = new Date();
                }
            }

            // create a flag to show if the component is disabled
            Boolean disabled = (Boolean) component.getAttributes().get("disabled");
            if (disabled == null) {
                disabled = Boolean.FALSE;
            }

            if (date != null) {
                // get the attributes from the component we need for rendering
                int nStartYear = getDateFromAttributes(context, "startYear", component.getAttributes(),
                        DEFAULT_START_YEAR);

                int nYearCount = getDateFromAttributes(context, "yearCount", component.getAttributes(),
                        DEFAULT_YEAR_COUNT);

                // now we render the output for our component
                // we create 3 drop-down menus for day, month and year and
                // two text fields for the hour and minute

                // note that we build a client id for our form elements that we are then
                // able to decode() as above.
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(date);
                renderMenu(out, component, getDays(), calendar.get(Calendar.DAY_OF_MONTH),
                        clientId + FIELD_DAY);
                renderMenu(out, component, getMonths(), calendar.get(Calendar.MONTH),
                        clientId + FIELD_MONTH);
                renderMenu(out, component, getYears(nStartYear, nYearCount), calendar.get(Calendar.YEAR),
                        clientId + FIELD_YEAR);

                // make sure we have a flag to determine whether to show the time
                Boolean showTime = (Boolean) component.getAttributes().get("showTime");
                if (showTime == null) {
                    showTime = Boolean.FALSE;
                }

                out.write("&nbsp;");
                renderTimeElement(out, component, calendar.get(Calendar.HOUR_OF_DAY), clientId + FIELD_HOUR,
                        showTime);
                if (showTime) {
                    out.write("&nbsp;:&nbsp;");
                }
                renderTimeElement(out, component, calendar.get(Calendar.MINUTE), clientId + FIELD_MINUTE,
                        showTime);
                out.write("&nbsp;");

                // render 2 links (if the component is not disabled) to allow the user to reset the
                // date back to null (if initialiseIfNull is false) or to select today's date
                if (!disabled) {
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
                String noneLabel = (String) component.getAttributes().get("noneLabel");
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

    private int getDateFromAttributes(FacesContext context, String key, Map attributes,
                                      int defaultValue) {
        final Object value = attributes.get(key);
        Integer integerValue = null;

        if (value != null) {
            if (value instanceof ValueBinding) {
                integerValue = (Integer) ((ValueBinding) value).getValue(context);
            } else if (value instanceof Integer) {
                integerValue = (Integer) value;
            } else {
                integerValue = Integer.valueOf(value.toString());
            }
        }

        if (integerValue == null) {
            return defaultValue;
        } else {
            return integerValue;
        }

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
    private void renderMenu(ResponseWriter out, UIComponent component, List items,
                            int selected, String clientId)
            throws IOException {
        out.write("<select");
        outputAttribute(out, clientId, "name");

        if (component.getAttributes().get("styleClass") != null) {
            outputAttribute(out, component.getAttributes().get("styleClass"), "class");
        }
        if (component.getAttributes().get("style") != null) {
            outputAttribute(out, component.getAttributes().get("style"), "style");
        }
        if (component.getAttributes().get("disabled") != null) {
            outputAttribute(out, component.getAttributes().get("disabled"), "disabled");
        }
        out.write(">");

        for (Object item1 : items) {
            SelectItem item = (SelectItem) item1;
            Integer value = (Integer) item.getValue();
            out.write("<option");
            outputAttribute(out, value, "value");

            // show selected value
            if (value == selected) {
                outputAttribute(out, "selected", "selected");
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
    private void renderTimeElement(ResponseWriter out, UIComponent component,
                                   int currentValue, String clientId, boolean showTime) throws IOException {
        out.write("<input");
        outputAttribute(out, clientId, "name");

        if (showTime) {
            out.write(" type='text' size='1' maxlength='2'");

            if (component.getAttributes().get("disabled") != null) {
                outputAttribute(out, component.getAttributes().get("disabled"), "disabled");
            }
        } else {
            out.write(" type='hidden'");
        }

        // make sure there are always 2 digits
        String strValue = Integer.toString(currentValue);
        if (strValue.length() == 1) {
            strValue = "0" + strValue;
        }

        outputAttribute(out, strValue, "value");
        out.write("/>");
    }

    private List getYears(int startYear, int yearCount) {
        List<SelectItem> years = new ArrayList<>();
        for (int i = startYear; i > startYear - yearCount; i--) {
            Integer year = i;
            years.add(new SelectItem(year, year.toString()));
        }
        return years;
    }

    private List getMonths() {
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

    private List getDays() {
        List<SelectItem> days = new ArrayList<>(31);
        for (int i = 1; i < 32; i++) {
            Integer day = i;
            days.add(new SelectItem(day, day.toString()));
        }
        return days;
    }

}
