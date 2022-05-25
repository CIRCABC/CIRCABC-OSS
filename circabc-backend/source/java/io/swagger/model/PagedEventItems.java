package io.swagger.model;

import eu.cec.digit.circabc.service.event.EventItem;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author schwerr
 */
public class PagedEventItems {

    private List<EventItem> data;
    private long total;

    public PagedEventItems(List<EventItem> data, long total) {
        super();
        this.data = data;
        this.total = total;
    }

    /**
     * @return the data
     */
    public List<EventItem> getData() {
        return data;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }
}
