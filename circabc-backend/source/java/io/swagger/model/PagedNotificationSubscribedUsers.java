package io.swagger.model;

import eu.cec.digit.circabc.service.notification.NotifiableUser;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author schwerr
 */
public class PagedNotificationSubscribedUsers {

    private List<NotifiableUser> data;
    private long total;

    public PagedNotificationSubscribedUsers(List<NotifiableUser> data, long total) {
        super();
        this.data = data;
        this.total = total;
    }

    /**
     * @return the data
     */
    public List<NotifiableUser> getData() {
        return data;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }
}
