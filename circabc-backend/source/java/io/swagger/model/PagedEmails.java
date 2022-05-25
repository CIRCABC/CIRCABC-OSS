package io.swagger.model;

import eu.cec.digit.circabc.service.app.message.DistributionEmailDAO;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author beaurpi
 */
public class PagedEmails {

    private List<DistributionEmailDAO> data;
    private long total;

    /**
     * @return the data
     */
    public List<DistributionEmailDAO> getData() {
        return data;
    }

    public void setData(List<DistributionEmailDAO> data) {
        this.data = data;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
