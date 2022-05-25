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
package eu.cec.digit.circabc.web.ui.component;

import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.ui.tag.AppointmentUnit;
import eu.cec.digit.circabc.web.ui.tag.JSFCalendarDecorator;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.Utils;
import org.apache.myfaces.shared_impl.renderkit.html.HTML;
import org.calendartag.util.CalendarTagUtil;
import org.joda.time.LocalTime;
import org.springframework.extensions.webscripts.ui.common.ConstantMethodBinding;

import javax.faces.component.UIComponentBase;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Component to render a calendar.
 *
 * @author yanick Pignot
 */
public class UICalendar extends UIComponentBase {

    public static final String PARAM_BROWSE_DATE = "browseDate";
    public static final String PARAM_VIEW_MODE = "viewMode";

    public static final String VIEW_MODE_MONTH = "month";
    public static final String VIEW_MODE_WEEK = "week";
    public static final String VIEW_MODE_WORKWEEK = "workweek";
    public static final String VIEW_MODE_DAY = "day";
    public static final String VIEW_MODE_AGENDA = "agenda";
    public static final String VIEW_MODE_TRIMESTER = "trimester";
    public static final String WEEK_START_SUNDAY = EventModel.WEEK_START_DAY_CONSTRAINT_VALUES.get(0);
    public static final String WEEK_START_MONDAY = EventModel.WEEK_START_DAY_CONSTRAINT_VALUES.get(1);
    public static final String WEEK_START_TUESDAY = EventModel.WEEK_START_DAY_CONSTRAINT_VALUES
            .get(2);
    public static final String WEEK_START_WEDNESDAY = EventModel.WEEK_START_DAY_CONSTRAINT_VALUES
            .get(3);
    public static final String WEEK_START_THURSDAY = EventModel.WEEK_START_DAY_CONSTRAINT_VALUES
            .get(4);
    public static final String WEEK_START_FRIDAY = EventModel.WEEK_START_DAY_CONSTRAINT_VALUES.get(5);
    public static final String WEEK_START_SATURDAY = EventModel.WEEK_START_DAY_CONSTRAINT_VALUES
            .get(6);
    public static final String WEEK_START_TODAY = EventModel.WEEK_START_DAY_CONSTRAINT_VALUES.get(7);
    private static final List<String> VALID_VIEW_MODES = Collections.unmodifiableList(Arrays
            .asList(VIEW_MODE_MONTH, VIEW_MODE_WEEK, VIEW_MODE_WORKWEEK, VIEW_MODE_DAY, VIEW_MODE_AGENDA,
                    VIEW_MODE_TRIMESTER));
    private static final String MSG_PREVIOUS_TOOLTIP = "calendar_previous_action_tooltip";
    private static final String MSG_NEXT_TOOLTIP = "calendar_next_action_tooltip";
    private static final String MSG_SELECTED_DATE_TOOLTIP = "calendar_selected_date_action_tooltip";
    private static final String MSG_ONE_DAY_TOOLTIP = "calendar_one_day_rich_tooltip";

    private static final String EMPTY_DAY_STYLE = "calendarEmptyDayStyle";
    private static final String TABLE_STYLE = "calendarTableStyle";
    private static final String TITLE_STYLE = "calendarTitleStyle";
    private static final String WEEKDAY_STYLE = "calendarWeekdayStyle";
    private static final String PREVIOUS_LINK_STYLE = "calendarPreviousLinkStyle";
    private static final String NEXT_LINK_STYLE = "calendarNextLinkStyle";

    private static final Boolean INTERACTIVE_DEFAULT = Boolean.TRUE;
    private static final Boolean BEYOND_DEFAULT = Boolean.FALSE;
    private static final String WEEK_START_DEFAULT = "Sunday";
    private static final String VIEW_MODE_DEFAULT = VIEW_MODE_MONTH;
    private static final int DAY_HEIGHT_DEFAULT = 85;
    private static final int DAY_WIDTH_DEFAULT = 85;

    private static final ThreadLocal<DateFormat> _DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd-MM-yyyy");
        }
    };

    private static final String ACTION_LINK_COMPONENT = "eu.cec.digit.circabc.faces.ActionLink";
    private static final Class ACTION_CLASS_ARGS[] = {javax.faces.event.ActionEvent.class};

    private Integer dayWidth;
    private Integer dayHeight;
    private String cssPrefix;
    private String weekStart;
    private Integer day;
    private Integer month;
    private Integer year;
    private String action;
    private String actionListener;
    private String viewMode;
    private Date date;
    private Date startDate;
    private Date endDate;
    private String decorator;
    private Boolean showPreviousNextLinks;
    private Boolean beyond;

    private JSFCalendarDecorator decoratorObject;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private Calendar calendar;
    private Calendar todayCalendar = new GregorianCalendar();

    // ------------------------------------------------------------------------------
    // Component implementation

    //private static final Log logger = LogFactory.getLog(UICalendar.class);

    public static Date convert(String dateAsString) throws ParseException {
        return _DATE_FORMAT.get().parse(dateAsString);
    }

    public static String convert(Date date) {
        return _DATE_FORMAT.get().format(date.getTime());
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    public void restoreState(final FacesContext context, final Object state) {
        final Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.dayWidth = (Integer) values[1];
        this.dayHeight = (Integer) values[2];
        this.cssPrefix = (String) values[3];
        this.weekStart = (String) values[4];
        this.day = (Integer) values[5];
        this.month = (Integer) values[6];
        this.year = (Integer) values[7];
        this.action = (String) values[8];
        this.actionListener = (String) values[9];
        this.viewMode = (String) values[10];
        this.date = (Date) values[11];
        this.startDate = (Date) values[12];
        this.endDate = (Date) values[13];
        this.decorator = (String) values[14];
        this.showPreviousNextLinks = (Boolean) values[15];
        this.beyond = (Boolean) values[16];
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[]
                {
                        super.saveState(context),
                        this.dayWidth,
                        this.dayHeight,
                        this.cssPrefix,
                        this.weekStart,
                        this.day,
                        this.month,
                        this.year,
                        this.action,
                        this.actionListener,
                        this.viewMode,
                        this.date,
                        this.startDate,
                        this.endDate,
                        this.decorator,
                        this.showPreviousNextLinks,
                        this.beyond
                };
        return (values);
    }

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.Calendar";
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    @SuppressWarnings("unchecked")
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }

        calendar = GregorianCalendar.getInstance();
        calendar.setTime(getDate());

        decoratorObject = initDecorator(getDecorator());
        initWeekStartDay(calendar, getWeekStart());
        checkDate();

        decoratorObject.setCalendar(calendar);
        decoratorObject.setPageContext(null);
        decoratorObject.setStart(startCalendar);
        decoratorObject.setEnd(endCalendar);
        decoratorObject.initializeCalendar();
        decoratorObject.setFacesContext(context);

        boolean oneDayView = getViewMode().equals(VIEW_MODE_DAY);
        boolean fiveDayView = getViewMode().equals(VIEW_MODE_WORKWEEK);

        // used determine the day that that previous link should direct to
        int dateDiff = CalendarTagUtil.differenceInDays(startCalendar, endCalendar);

        if (oneDayView == false && dateDiff == 0) {
            oneDayView = true;
        }

        if (oneDayView) {
            writeOneDay(context, dateDiff);
        } else if (fiveDayView) {
            writeFiveDaysColumns(context, dateDiff);
        } else {
            writeSevenDaysColumns(context);
        }
    }

    @SuppressWarnings("unused")
    private void writeFiveDaysColumns(final FacesContext context, final int dateDiff)
            throws IOException {
        final ResponseWriter writer = context.getResponseWriter();
        final String eventRootId = Beans.getWaiNavigator().getCurrentNodeId();
        final int columnNumber = 5;
        final int truedayWidth = (getDayWidth() * 7) / 5;
        final int truedayHeight = getDayHeight();

        throw new IllegalStateException("Not implemented yet");
    }

    private void writeOneDay(final FacesContext context, final int dateDiff) throws IOException {
        final ResponseWriter writer = context.getResponseWriter();
        final String eventRootId = Beans.getWaiNavigator().getCurrentNodeId();
        final int columnNumber = 1;
        final int truedayWidth = getDayWidth() * 7;

        //start the table
        writer.write("<table border=\"0\" class=\"" + getCssPrefix() + TABLE_STYLE + "\"");
        writer.write(" id=\"" + getId() + '_' + System.currentTimeMillis() + "\"");
        writer.write(">\r\n  <tr>\r\n");

        // draw the header stuff
        if (getShowPreviousNextLinks()) {
            Calendar newCalendar = (Calendar) calendar.clone();

            newCalendar.set(Calendar.DATE, newCalendar.get(Calendar.DATE) - dateDiff - 1);

            writer.write("    <td colspan=\"1\" class=\"" + cssPrefix + PREVIOUS_LINK_STYLE + "\" >");
            UIActionLink previous = buildActionLink(context,
                    getAction(),
                    getActionListener(),
                    "<<",
                    Application.getMessage(context, MSG_PREVIOUS_TOOLTIP), eventRootId);

            addParam(context, previous, PARAM_BROWSE_DATE, convert(newCalendar.getTime()));

            addParam(context, previous, PARAM_VIEW_MODE, VIEW_MODE_DAY);

            Utils.encodeRecursive(context, previous);
            writer.write("</td>\r\n");

            writer.write("    <td colspan=\"3\" class=\"" + getCssPrefix() + TITLE_STYLE + "\" >");

            writer.write(decoratorObject.getWeekdayTitle(startCalendar.get(Calendar.DAY_OF_WEEK))
                    + HTML.NBSP_ENTITY);
            writer.write(decoratorObject.getDay(null) + HTML.NBSP_ENTITY);
            writer.write(decoratorObject.getCalendarTitle());

            writer.write("</td>\r\n");

            newCalendar = (Calendar) calendar.clone();

            newCalendar.set(Calendar.DATE, newCalendar.get(Calendar.DATE) + dateDiff + 1);

            writer.write("    <td colspan=\"1\" class=\"" + getCssPrefix() + NEXT_LINK_STYLE + "\" >");
            UIActionLink next = buildActionLink(context,
                    getAction(),
                    getActionListener(),
                    ">>",
                    Application.getMessage(context, MSG_NEXT_TOOLTIP),
                    eventRootId);

            addParam(context, next, PARAM_BROWSE_DATE, convert(newCalendar.getTime()));

            addParam(context, next, PARAM_VIEW_MODE, VIEW_MODE_DAY);

            Utils.encodeRecursive(context, next);
            writer.write("</td>\r\n");

        } else {
            // just draw the header, with 7 colspan
            writer.write(
                    "    <td colspan=\"" + columnNumber + "\" class=\"" + cssPrefix + TITLE_STYLE + "\" >");
            writer.write(decoratorObject.getCalendarTitle());
            writer.write("</td>\r\n");
        }

        writer.write("  </tr>\r\n  ");

        ///////decoratorObject.setCalendar(startCalendar);

        /// computeColumnNumber  //// computeLineNumber

        // the appointments are sorted !
        final List<AppointmentUnit> appointments = decoratorObject.getAppointments();

        // the earliest time of the calendar
        LocalTime workDayStart = new LocalTime(8, 0, 0);
        // the latest time of the calendar
        LocalTime endDayStart = new LocalTime(19, 0, 0);

        // the number of appointments of the day
        final int appointmentsSize = appointments.size();

        if (appointmentsSize > 0) {
            final LocalTime firstHour = appointments.get(0).getStart();
            // if an appointment is found before 8:00, change the first hour to display
            if (firstHour.isBefore(workDayStart)) {
                workDayStart = new LocalTime(firstHour.getHourOfDay(), 0, 0);
            }

            LocalTime lastHour = null;
            LocalTime lastHourTemp = null;
            // if an appointment is found lastest 18:45, change the last hour to display
            for (AppointmentUnit appointment : appointments) {
                lastHourTemp = appointment.getEnd();
                if (lastHour == null || lastHour.isBefore(lastHourTemp)) {
                    lastHour = lastHourTemp;
                }
            }

            if (lastHour != null && lastHour.isAfter(endDayStart)) {
                endDayStart = lastHour;
            }
        }

        LocalTime currentTime = workDayStart.plusMinutes(0);

        final LocalTime upperLimit = new LocalTime(0, 0);
        while (currentTime.isBefore(endDayStart)) {
            writer.write("  <tr>\r\n");

            writer.write("    <th colspan=\"1\" rowspan=\"4\" class=\"" + cssPrefix + decoratorObject
                    .getDayStyleClass(true, true) + "\" >");
            writer.write(padInt(currentTime.getHourOfDay()) + ":" + padInt(currentTime.getMinuteOfHour())
                    + HTML.NBSP_ENTITY + HTML.NBSP_ENTITY);
            writer.write("</th>\r\n");

            writeASingleDayQuarter(context, currentTime, appointments, truedayWidth);

            currentTime = currentTime.plusMinutes(15);

            writer.write("  </tr>\r\n");

            for (int quarter = 0; quarter < 3; ++quarter) {
                writer.write("  <tr>\r\n");
                writeASingleDayQuarter(context, currentTime, appointments, truedayWidth);
                writer.write("  </tr>\r\n");

                currentTime = currentTime.plusMinutes(15);
            }
            if (currentTime.isEqual(upperLimit)) {
                break;
            }
        }

        writer.write("</table>\r\n");
    }

    private void writeASingleDayQuarter(final FacesContext context, LocalTime currentTime,
                                        final List<AppointmentUnit> appointments, final int truedayWidth) throws IOException {
        final ResponseWriter writer = context.getResponseWriter();

        writer.write("    <td colspan=\"6\" class=\"" + cssPrefix + decoratorObject
                .getDayStyleClass(false, false) + "\" width=\"" + truedayWidth + "\">");

        boolean firstEventOfQuarter = true;

        writer.write(padInt(currentTime.getMinuteOfHour()) + ":&nbsp;");

        for (AppointmentUnit unit : appointments) {
            if (unit.isInQuarter(currentTime)) {
                if (firstEventOfQuarter == false) {
                    writer.write("<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                } else {
                    firstEventOfQuarter = false;
                }

                String tooltip = MessageFormat.format(
                        Application.getMessage(context, MSG_ONE_DAY_TOOLTIP),
                        unit.getTitle(), unit.getStartAsString(), unit.getEndAsString());

                final UIActionLink currentEventAction = buildActionLinkForSingleAppointement(
                        context,
                        unit.getType() + ":&nbsp;" + unit.getTitle(),
                        tooltip,
                        unit);
                Utils.encodeRecursive(context, currentEventAction);

            }
        }

        writer.write("</td>\r\n");
    }

    private void writeSevenDaysColumns(final FacesContext context) throws IOException {
        final ResponseWriter writer = context.getResponseWriter();
        final String eventRootId = Beans.getWaiNavigator().getCurrentNodeId();
        final int columnNumber = Calendar.DAY_OF_WEEK;
        final int truedayWidth = getDayWidth();
        final int truedayHeight = getDayHeight();

        // start the table
        writer.write("<table border=\"0\" class=\"" + getCssPrefix() + TABLE_STYLE + "\"");
        writer.write(" id=\"" + getId() + '_' + System.currentTimeMillis() + "\"");
        writer.write(">\r\n  <tr>\r\n");

        // draw the header stuff
        if (getShowPreviousNextLinks()) {
            Calendar newCalendar = (Calendar) calendar.clone();

            if (getViewMode().equals(VIEW_MODE_MONTH)) {
                newCalendar.add(Calendar.MONTH, -1);
            } else if (getViewMode().equals(VIEW_MODE_TRIMESTER)) {
                newCalendar.add(Calendar.MONTH, -3);
            } else if (getViewMode().equals(VIEW_MODE_WEEK)) {
                newCalendar.add(Calendar.DATE, -7);
            }

            writer.write("    <td colspan=\"1\" class=\"" + cssPrefix + PREVIOUS_LINK_STYLE + "\" >");
            UIActionLink previous = buildActionLink(context,
                    getAction(),
                    getActionListener(),
                    "<<",
                    Application.getMessage(context, MSG_PREVIOUS_TOOLTIP), eventRootId);

            addParam(context, previous, PARAM_BROWSE_DATE, convert(newCalendar.getTime()));
            addParam(context, previous, PARAM_VIEW_MODE, getViewMode());

            Utils.encodeRecursive(context, previous);
            writer.write("</td>\r\n");

            writer.write("    <td colspan=\"5\" class=\"" + getCssPrefix() + TITLE_STYLE + "\" >");

            writer.write(decoratorObject.getCalendarTitle());

            writer.write("</td>\r\n");

            newCalendar = (Calendar) calendar.clone();

            if (getViewMode().equals(VIEW_MODE_MONTH)) {
                newCalendar.add(Calendar.MONTH, 1);
            } else if (getViewMode().equals(VIEW_MODE_TRIMESTER)) {
                newCalendar.add(Calendar.MONTH, 3);
            } else if (getViewMode().equals(VIEW_MODE_WEEK)) {
                newCalendar.add(Calendar.DATE, 7);
            }

            writer.write("    <td colspan=\"1\" class=\"" + getCssPrefix() + NEXT_LINK_STYLE + "\" >");
            UIActionLink next = buildActionLink(context,
                    getAction(),
                    getActionListener(),
                    ">>",
                    Application.getMessage(context, MSG_NEXT_TOOLTIP),
                    eventRootId);

            addParam(context, next, PARAM_BROWSE_DATE, convert(newCalendar.getTime()));
            addParam(context, next, PARAM_VIEW_MODE, getViewMode());

            Utils.encodeRecursive(context, next);
            writer.write("</td>\r\n");

        } else {
            // just draw the header, with 7 colspan
            writer.write(
                    "    <td colspan=\"" + columnNumber + "\" class=\"" + cssPrefix + TITLE_STYLE + "\" >");
            writer.write(decoratorObject.getCalendarTitle());
            writer.write("</td>\r\n");
        }

        writer.write("  </tr>\r\n");

        writer.write("  <tr>\r\n");
        // draw each weekday abbreviation with the decorator
        for (int i = 0; i < columnNumber; i++) {
            int day = i + calendar.getFirstDayOfWeek();
            if (day > columnNumber) {
                day = day - columnNumber;
            }
            writer.write("    <td class=\"" + cssPrefix + WEEKDAY_STYLE + "\">" + decoratorObject
                    .getWeekdayTitle(day) + "</td>\r\n");
        }
        writer.write("  </tr>\r\n");

        writer.write("  <tr>\r\n");

        // start drawing the days

        int emptyDays;
        int column = 0;

        // count the number of empty days
        int firstDayOfWeek = startCalendar.get(Calendar.DAY_OF_WEEK);
        if (calendar.getFirstDayOfWeek() > firstDayOfWeek) {
            emptyDays = firstDayOfWeek - calendar.getFirstDayOfWeek() + columnNumber;
        } else {
            emptyDays = firstDayOfWeek - calendar.getFirstDayOfWeek();
        }

        Calendar emptyDay = null;
        UIActionLink currentDayAction = null;

        if (getBeyond()) {
            Calendar calStart = (Calendar) startCalendar.clone();
            calStart.set(Calendar.DATE, startCalendar.get(Calendar.DATE) - emptyDays);
            decoratorObject.setStart(calStart);

            int endEmptyDay = (columnNumber - 1) - (
                    (endCalendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek()) % columnNumber);

            Calendar calEnd = (Calendar) endCalendar.clone();
            calEnd.set(Calendar.DATE, endCalendar.get(Calendar.DATE) + endEmptyDay);
            decoratorObject.setEnd(calEnd);
        }
        // draw each empty day
        for (int i = 0; i < emptyDays; i++) {
            writer.write("    <td class=\"" + cssPrefix + EMPTY_DAY_STYLE + "\" height=\"" + dayHeight
                    + "\" width=\"" + truedayHeight + "\">");
            if (getBeyond()) {
                emptyDay = (Calendar) startCalendar.clone();
                emptyDay.set(Calendar.DATE, startCalendar.get(Calendar.DATE) - (emptyDays - i));
                decoratorObject.setCalendar(emptyDay);
                currentDayAction = buildActionLinkForDayEvents(context, convert(emptyDay.getTime()),
                        decoratorObject.getEmptyDay(), eventRootId);
                Utils.encodeRecursive(context, currentDayAction);
            } else {
                writer.write(decoratorObject.getEmptyDay());
            }
            writeAppoitments(context, decoratorObject.getAppointments(), writer);

            writer.write("</td>\r\n");

            column++;
        }

        Calendar iteratingDate = (Calendar) startCalendar.clone();
        boolean isOddMonth = true;
        while (!iteratingDate.after(endCalendar)) {

            decoratorObject.setCalendar(iteratingDate);

            if (column == 0) {
                writer.write("  <tr>\r\n");
            }

            writer.write("    <td " + " class=\"" + cssPrefix + decoratorObject
                    .getDayStyleClass(isOddMonth, CalendarTagUtil.isSameDay(calendar, iteratingDate))
                    + "\" height=\"" + truedayHeight + "\" width=\"" + truedayWidth + "\">");

            currentDayAction = buildActionLinkForDayEvents(context, convert(iteratingDate.getTime()),
                    decoratorObject.getDay(null), eventRootId);
            Utils.encodeRecursive(context, currentDayAction);

            writeAppoitments(context, decoratorObject.getAppointments(), writer);

            writer.write("</td>\r\n");

            // increment the column and end it if neccessary
            column++;

            if (column == Calendar.DAY_OF_WEEK) {
                writer.write("  </tr>\r\n");
                column = 0;
            }

            // check to see if we're changing months & or years
            if (iteratingDate.getActualMaximum(Calendar.DATE) == iteratingDate.get(Calendar.DATE)) {
                if (isOddMonth) {
                    isOddMonth = false;
                } else {
                    isOddMonth = true;
                }
            }

            // increment the date, Calendar wraps it month and/or year if needed
            iteratingDate.set(Calendar.DATE, iteratingDate.get(Calendar.DATE) + 1);

        }

        int columnIncrement = 0;
        while (column < Calendar.DAY_OF_WEEK && column > 0) {
            writer.write(
                    "    <td class=\"" + getCssPrefix() + EMPTY_DAY_STYLE + "\" height=\"" + truedayHeight
                            + "\" width=\"" + truedayWidth + "\">");

            if (getBeyond()) {
                emptyDay = (Calendar) iteratingDate.clone();
                emptyDay.add(Calendar.DATE, columnIncrement++);
                decoratorObject.setCalendar(emptyDay);
                currentDayAction = buildActionLinkForDayEvents(context, convert(emptyDay.getTime()),
                        decoratorObject.getEmptyDay(), eventRootId);
                Utils.encodeRecursive(context, currentDayAction);
            } else {
                writer.write(decoratorObject.getEmptyDay());
            }

            writeAppoitments(context, decoratorObject.getAppointments(), writer);

            writer.write("</td>\r\n");

            column++;
        }

        writer.write("  </tr>\r\n");
        writer.write("</table>\r\n");
    }

    public String padInt(int value) {
        if (value < 10) {
            return "0" + String.valueOf(value);
        } else {
            return String.valueOf(value);
        }
    }

    protected void checkDate() {
        final String mode = getViewMode();

        if (!VALID_VIEW_MODES.contains(mode)) {
            throw new IllegalArgumentException("View mode not recognize: " + mode);
        }

        if (getStartDate() == null ^ getEndDate() == null) {
            throw new IllegalArgumentException("Both start date and end date must be specified !!");
        } else if (getStartDate() != null) {
            if (getStartDate().after(getEndDate())) {
                throw new IllegalArgumentException("You startDate is after your endDate.");
            }

            startCalendar = GregorianCalendar.getInstance();
            startCalendar.setTime(getStartDate());

            endCalendar = GregorianCalendar.getInstance();
            endCalendar.setTime(getEndDate());
        } else if (getDay() != -1 || getMonth() != -1 || getYear() != -1) {
            calendar = (Calendar) todayCalendar.clone();
            if (getDay() != -1) {
                calendar.set(Calendar.DATE, getDay());
            }
            if (getMonth() != -1) {
                calendar.set(Calendar.MONTH, getMonth());
            }
            if (getYear() != -1) {
                calendar.set(Calendar.YEAR, getYear());
            }
        }

        if (startCalendar == null) {
            startCalendar = (Calendar) calendar.clone();

            if (VIEW_MODE_MONTH.equals(mode)) {
                startCalendar.set(Calendar.DAY_OF_MONTH, 1);
            } else if (VIEW_MODE_WEEK.equals(mode)) {
                startCalendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            } else if (VIEW_MODE_WORKWEEK.equals(mode)) {
                startCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            } else if (VIEW_MODE_TRIMESTER.equals(mode)) {
                startCalendar
                        .set(Calendar.MONTH, getStartMonthOfTrimester(startCalendar.get(Calendar.MONTH)));
                startCalendar.set(Calendar.DAY_OF_MONTH, 1);
            }
        }
        if (endCalendar == null) {
            endCalendar = (Calendar) startCalendar.clone();

            if (VIEW_MODE_MONTH.equals(mode)) {
                endCalendar
                        .set(Calendar.DAY_OF_MONTH, startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            } else if (VIEW_MODE_WEEK.equals(mode)) {
                endCalendar.add(Calendar.DATE, 6);
            } else if (VIEW_MODE_WORKWEEK.equals(mode)) {
                endCalendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            } else if (VIEW_MODE_TRIMESTER.equals(mode)) {
                endCalendar.set(Calendar.MONTH, startCalendar.get(Calendar.MONTH) + 2);
                endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
        }

        CalendarTagUtil.trimCalendar(calendar);
        CalendarTagUtil.trimCalendar(startCalendar);
        CalendarTagUtil.trimCalendar(endCalendar);
        CalendarTagUtil.trimCalendar(todayCalendar);
    }

    private void writeAppoitments(final FacesContext context,
                                  final List<AppointmentUnit> appointments, final ResponseWriter writer) throws IOException {
        //display the appointement of the day
        if (appointments != null) {
            writer.write("<br /><br />");

            for (final AppointmentUnit appointment : appointments) {
                writer.write("-&nbsp;<i>");
                UIActionLink currentEventAction = buildActionLinkForSingleAppointement(
                        context,
                        appointment.getStartAsString() + "&nbsp;" + appointment.getType(),
                        appointment.getTitle(),
                        appointment);
                Utils.encodeRecursive(context, currentEventAction);
                writer.write("<br />");
            }
        }
    }

    protected JSFCalendarDecorator initDecorator(String calendarDecoratorClassName) {
        try {
            final Class decoratorClass = Class.forName(calendarDecoratorClassName);
            return (JSFCalendarDecorator) decoratorClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot resolve decorator: " + decorator);
        }
    }

    protected void initWeekStartDay(final Calendar cal, final String weekStartDay) {
        // figure the week start
        if (weekStartDay.equalsIgnoreCase(WEEK_START_SUNDAY)) {
            cal.setFirstDayOfWeek(Calendar.SUNDAY);
        } else if (weekStartDay.equalsIgnoreCase(WEEK_START_MONDAY)) {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
        } else if (weekStartDay.equalsIgnoreCase(WEEK_START_TUESDAY)) {
            cal.setFirstDayOfWeek(Calendar.TUESDAY);
        } else if (weekStartDay.equalsIgnoreCase(WEEK_START_WEDNESDAY)) {
            cal.setFirstDayOfWeek(Calendar.WEDNESDAY);
        } else if (weekStartDay.equalsIgnoreCase(WEEK_START_THURSDAY)) {
            cal.setFirstDayOfWeek(Calendar.THURSDAY);
        } else if (weekStartDay.equalsIgnoreCase(WEEK_START_FRIDAY)) {
            cal.setFirstDayOfWeek(Calendar.FRIDAY);
        } else if (weekStartDay.equalsIgnoreCase(WEEK_START_SATURDAY)) {
            cal.setFirstDayOfWeek(Calendar.SATURDAY);
        } else if (weekStartDay.equalsIgnoreCase(WEEK_START_TODAY)) {
            cal.setFirstDayOfWeek(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        }
    }

    private UIActionLink buildActionLinkForDayEvents(final FacesContext context, final String date,
                                                     final String value, final String valueIdParam) {
        final String tooltip = MessageFormat
                .format(Application.getMessage(context, MSG_SELECTED_DATE_TOOLTIP), date);
        final UIActionLink actionLink = buildActionLink(context, getAction(), getActionListener(),
                value, tooltip, valueIdParam);

        addParam(context, actionLink, PARAM_BROWSE_DATE, date);
        addParam(context, actionLink, PARAM_VIEW_MODE, VIEW_MODE_DAY);

        return actionLink;
    }

    private UIActionLink buildActionLinkForSingleAppointement(final FacesContext context,
                                                              final String title, final String tooltip, final AppointmentUnit appointment) {
        final UIActionLink actionLink = buildActionLink(context, getAction(), getActionListener(),
                title, tooltip, String.valueOf(appointment.getId()));

        return actionLink;
    }

    private UIActionLink buildActionLink(final FacesContext context, final String action,
                                         final String actionListener, final String value, final String tooltip,
                                         final String valueIdParam) {
        final javax.faces.application.Application facesApp = context.getApplication();
        final UIActionLink actionLink = (UIActionLink) facesApp.createComponent(ACTION_LINK_COMPONENT);

        if (action != null) {
            actionLink.setAction(new ConstantMethodBinding(action));
        }
        if (actionListener != null) {
            actionLink.setActionListener(
                    context.getApplication().createMethodBinding(actionListener, ACTION_CLASS_ARGS));
        }
        actionLink.setValue(value);
        actionLink.setTooltip(tooltip);
        actionLink.setParent(this);

        addParam(context, actionLink, "id", valueIdParam);

        return actionLink;
    }

    @SuppressWarnings("unchecked")
    private void addParam(final FacesContext context, UIActionLink actionLink, final String paramName,
                          final String paramValue) {
        final javax.faces.application.Application facesApp = context.getApplication();
        final UIParameter actionParam =
                (UIParameter) facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);

        actionParam.setName(paramName);
        actionParam.setValue(paramValue);
        actionLink.getChildren().add(actionParam);
        getChildren().add(actionLink);
    }

    private int getStartMonthOfTrimester(int month) {
        int firstMonth;

        switch (month) {
            case 0:
            case 1:
            case 2:
                firstMonth = 0;
                break;
            case 3:
            case 4:
            case 5:
                firstMonth = 3;
                break;
            case 6:
            case 7:
            case 8:
                firstMonth = 6;
                break;
            case 9:
            case 10:
            case 11:
                firstMonth = 9;
                break;
            default:
                throw new IllegalArgumentException("Month must be beetween 0-11");

        }

        return firstMonth;
    }

    /**
     * @see javax.faces.component.UIComponentBase#getRendersChildren()
     */
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * @return the cssPrefix
     */
    public String getCssPrefix() {
        if (this.cssPrefix == null) {
            ValueBinding vb = getValueBinding("cssPrefix");
            if (vb != null) {
                this.cssPrefix = (String) vb.getValue(getFacesContext());
            }
            if (this.cssPrefix == null) {
                this.cssPrefix = "";
            }
        }
        return cssPrefix;
    }

    /**
     * @param cssPrefix the cssPrefix to set
     */
    public void setCssPrefix(String cssPrefix) {
        this.cssPrefix = cssPrefix;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        if (this.date == null) {
            ValueBinding vb = getValueBinding("date");
            if (vb != null) {
                this.date = (Date) vb.getValue(getFacesContext());
            }
            if (this.date == null) {
                this.date = todayCalendar.getTime();
            }
        }

        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the day
     */
    public Integer getDay() {
        if (this.day == null) {
            ValueBinding vb = getValueBinding("day");
            if (vb != null) {
                this.day = (Integer) vb.getValue(getFacesContext());
            }
            if (this.day == null) {
                this.day = -1;
            }
        }

        return day;
    }

    /**
     * @param day the day to set
     */
    public void setDay(Integer day) {
        this.day = day;
    }

    /**
     * @return the dayHeight
     */
    public Integer getDayHeight() {
        if (this.dayHeight == null) {
            ValueBinding vb = getValueBinding("dayHeight");
            if (vb != null) {
                this.dayHeight = (Integer) vb.getValue(getFacesContext());
            }
            if (this.dayHeight == null) {
                this.dayHeight = DAY_HEIGHT_DEFAULT;
            }
        }
        return dayHeight;
    }

    /**
     * @param dayHeight the dayHeight to set
     */
    public void setDayHeight(Integer dayHeight) {
        this.dayHeight = dayHeight;
    }

    /**
     * @return the dayWidth
     */
    public Integer getDayWidth() {
        if (this.dayWidth == null) {
            ValueBinding vb = getValueBinding("dayWidth");
            if (vb != null) {
                this.dayWidth = (Integer) vb.getValue(getFacesContext());
            }
            if (this.dayWidth == null) {
                this.dayWidth = DAY_WIDTH_DEFAULT;
            }
        }

        return dayWidth;
    }

    /**
     * @param dayWidth the dayWidth to set
     */
    public void setDayWidth(Integer dayWidth) {
        this.dayWidth = dayWidth;
    }

    /**
     * @return the decorator
     */
    public String getDecorator() {
        if (this.decorator == null) {
            ValueBinding vb = getValueBinding("decorator");
            if (vb != null) {
                this.decorator = (String) vb.getValue(getFacesContext());
            }
            if (this.decorator == null) {
                this.decorator = JSFCalendarDecorator.class.getName();
            }
        }

        return decorator;
    }

    /**
     * @param decorator the decorator to set
     */
    public void setDecorator(String decorator) {
        this.decorator = decorator;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        if (this.endDate == null) {
            ValueBinding vb = getValueBinding("endDate");
            if (vb != null) {
                this.endDate = (Date) vb.getValue(getFacesContext());
            }
        }
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the month
     */
    public Integer getMonth() {
        if (this.month == null) {
            ValueBinding vb = getValueBinding("month");
            if (vb != null) {
                this.month = (Integer) vb.getValue(getFacesContext());
            }
            if (this.month == null) {
                this.month = -1;
            }
        }

        return month;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(Integer month) {
        this.month = month;
    }

    /**
     * @return the action
     */
    public String getAction() {
        if (this.action == null) {
            ValueBinding vb = getValueBinding("action");
            if (vb != null) {
                this.action = vb.getExpressionString();
            }
        }
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return the actionListener
     */
    public String getActionListener() {
        if (this.actionListener == null) {
            ValueBinding vb = getValueBinding("actionListener");
            if (vb != null) {
                this.actionListener = vb.getExpressionString();
            }
        }
        return actionListener;
    }

    /**
     * @param actionListener the actionListener to set
     */
    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * @return the viewMode
     */
    public String getViewMode() {
        if (this.viewMode == null) {
            ValueBinding vb = getValueBinding("viewMode");
            if (vb != null) {
                this.viewMode = (String) vb.getValue(getFacesContext());
            }
            if (this.viewMode == null) {
                this.viewMode = VIEW_MODE_DEFAULT;
            }
        }
        return viewMode;
    }

    /**
     * @param viewMode the viewMode to set
     */
    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
    }

    /**
     * @return the showPreviousNextLinks
     */
    public Boolean getShowPreviousNextLinks() {
        if (this.showPreviousNextLinks == null) {
            ValueBinding vb = getValueBinding("showPreviousNextLinks");
            if (vb != null) {
                this.showPreviousNextLinks = (Boolean) vb.getValue(getFacesContext());
            }
            if (this.showPreviousNextLinks == null) {
                showPreviousNextLinks = INTERACTIVE_DEFAULT;
            }
        }
        return showPreviousNextLinks;
    }

    /**
     * @param showPreviousNextLinks the showPreviousNextLinks to set
     */
    public void setShowPreviousNextLinks(Boolean showPreviousNextLinks) {
        this.showPreviousNextLinks = showPreviousNextLinks;
    }

    /**
     * @return the beyond
     */
    public Boolean getBeyond() {
        if (this.beyond == null) {
            ValueBinding vb = getValueBinding("beyond");
            if (vb != null) {
                this.beyond = (Boolean) vb.getValue(getFacesContext());
            }
            if (this.beyond == null) {
                beyond = BEYOND_DEFAULT;
            }
        }
        return beyond;
    }

    /**
     * @param beyond the beyond to set
     */
    public void setBeyond(Boolean beyond) {
        this.beyond = beyond;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        if (this.startDate == null) {
            ValueBinding vb = getValueBinding("startDate");
            if (vb != null) {
                this.startDate = (Date) vb.getValue(getFacesContext());
            }
        }
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the weekStart
     */
    public String getWeekStart() {
        if (this.weekStart == null) {
            ValueBinding vb = getValueBinding("weekStart");
            if (vb != null) {
                this.weekStart = (String) vb.getValue(getFacesContext());
            }
            if (this.weekStart == null) {
                this.weekStart = WEEK_START_DEFAULT;
            }
        }
        return weekStart;
    }

    /**
     * @param weekStart the weekStart to set
     */
    public void setWeekStart(String weekStart) {
        this.weekStart = weekStart;
    }

    /**
     * @return the year
     */
    public Integer getYear() {
        if (this.year == null) {
            ValueBinding vb = getValueBinding("year");
            if (vb != null) {
                this.year = (Integer) vb.getValue(getFacesContext());
            }
            if (this.year == null) {
                this.year = -1;
            }
        }
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(Integer year) {
        this.year = year;
    }
}
