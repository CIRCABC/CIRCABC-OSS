package io.swagger.model;

import eu.cec.digit.circabc.repo.config.auto.upload.Configuration;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author schwerr
 */
public class PagedAutoUploadConfiguration {

    private List<Configuration> data;
    private long total;

    public PagedAutoUploadConfiguration(List<Configuration> data, long total) {
        super();
        this.data = data;
        this.total = total;
    }

    /**
     * @return the data
     */
    public List<Configuration> getData() {
        return data;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }
}
