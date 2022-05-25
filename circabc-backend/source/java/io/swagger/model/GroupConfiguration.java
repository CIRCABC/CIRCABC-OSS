package io.swagger.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JSON object that contains all the configuration variables of an Interest Group
 */
public class GroupConfiguration {

    private Map<String, String> information = null;

    private Map<String, String> library = null;

    private GroupConfigurationNewsgroups newsgroups = null;

    private Map<String, String> events = null;

    private Map<String, String> dashboard = null;

    public GroupConfiguration putInformationItem(String key, String informationItem) {
        if (this.information == null) {
            this.information = new HashMap<>();
        }
        this.information.put(key, informationItem);
        return this;
    }

    /**
     * Get information
     *
     * @return information
     */
    public Map<String, String> getInformation() {
        return information;
    }

    public void setInformation(Map<String, String> information) {
        this.information = information;
    }

    public GroupConfiguration putLibraryItem(String key, String libraryItem) {
        if (this.library == null) {
            this.library = new HashMap<>();
        }
        this.library.put(key, libraryItem);
        return this;
    }

    /**
     * Get library
     *
     * @return library
     */
    public Map<String, String> getLibrary() {
        return library;
    }

    public void setLibrary(Map<String, String> library) {
        this.library = library;
    }

    /**
     * Get newsgroups
     *
     * @return newsgroups
     */
    public GroupConfigurationNewsgroups getNewsgroups() {
        return newsgroups;
    }

    public void setNewsgroups(GroupConfigurationNewsgroups newsgroups) {
        this.newsgroups = newsgroups;
    }

    public GroupConfiguration putEventsItem(String key, String eventsItem) {
        if (this.events == null) {
            this.events = new HashMap<>();
        }
        this.events.put(key, eventsItem);
        return this;
    }

    /**
     * Get events
     *
     * @return events
     */
    public Map<String, String> getEvents() {
        return events;
    }

    public void setEvents(Map<String, String> events) {
        this.events = events;
    }

    public GroupConfiguration putDashboardItem(String key, String dashboardItem) {
        if (this.dashboard == null) {
            this.dashboard = new HashMap<>();
        }
        this.dashboard.put(key, dashboardItem);
        return this;
    }

    /**
     * Get dashboard
     *
     * @return dashboard
     */
    public Map<String, String> getDashboard() {
        return dashboard;
    }

    public void setDashboard(Map<String, String> dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupConfiguration groupConfiguration = (GroupConfiguration) o;
        return Objects.equals(this.information, groupConfiguration.information)
                && Objects.equals(this.library, groupConfiguration.library)
                && Objects.equals(this.newsgroups, groupConfiguration.newsgroups)
                && Objects.equals(this.events, groupConfiguration.events)
                && Objects.equals(this.dashboard, groupConfiguration.dashboard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(information, library, newsgroups, events, dashboard);
    }

    @Override
    public String toString() {

        return "class GroupConfiguration {\n"
                + "    information: "
                + toIndentedString(information)
                + "\n"
                + "    library: "
                + toIndentedString(library)
                + "\n"
                + "    newsgroups: "
                + toIndentedString(newsgroups)
                + "\n"
                + "    events: "
                + toIndentedString(events)
                + "\n"
                + "    dashboard: "
                + toIndentedString(dashboard)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    public String toJsonString() {
        return "{\n"
                + "\t\"information\": "
                + "{},"
                + "\n"
                + "\t\"library\": "
                + "{},"
                + "\n"
                + "\t\"newsgroups\": "
                + newsgroups.toJsonString()
                + ",\n"
                + "\t\"events\": "
                + "{},"
                + "\n"
                + "\t\"dashboard\": "
                + "{}"
                + "\n"
                + "}";
    }
}
