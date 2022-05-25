package io.swagger.model;

import eu.cec.digit.circabc.repo.statistics.ReportFile;

import java.util.List;

/**
 * Encapsulation to store the list of items retrieved with the total amount of items for pagination
 * by the UI
 *
 * @author schwerr
 */
public class PagedStatisticsContents {

    private List<ReportFile> data;
    private long total;

    public PagedStatisticsContents(List<ReportFile> data, long total) {
        super();
        this.data = data;
        this.total = total;
    }

    /**
     * @return the data
     */
    public List<ReportFile> getData() {
        return data;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }
}
