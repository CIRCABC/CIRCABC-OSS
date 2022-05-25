package io.swagger.model;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author schwerr
 */
public class PagedShares {

    private List<Share> data;
    private long total;

    public PagedShares(List<Share> data, long total) {
        super();
        this.data = data;
        this.total = total;
    }

    /**
     * @return the data
     */
    public List<Share> getData() {
        return data;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }
}
