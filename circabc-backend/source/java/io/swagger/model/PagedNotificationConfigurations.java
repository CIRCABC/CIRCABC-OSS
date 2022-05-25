package io.swagger.model;

import eu.cec.digit.circabc.web.wai.dialog.notification.NotificationWrapper;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author schwerr
 */
public class PagedNotificationConfigurations {

    private List<NotificationWrapper> data;
    private long total;

    public PagedNotificationConfigurations(List<NotificationWrapper> data, long total) {
        super();
        this.data = data;
        this.total = total;
    }

    /**
     * @return the data
     */
    public List<NotificationWrapper> getData() {
        return data;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }
}
