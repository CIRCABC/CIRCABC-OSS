/**
 *
 */
package io.swagger.model;

import java.util.ArrayList;
import java.util.List;

/** @author beaurpi */
public class PagedUserRevocationRequest {

    private List<UserRevocationRequest> data = new ArrayList<>();
    private Integer total;

    /** @return the data */
    public List<UserRevocationRequest> getData() {
        return data;
    }

    /** @param data the data to set */
    public void setData(List<UserRevocationRequest> data) {
        this.data = data;
    }

    /** @return the total */
    public Integer getTotal() {
        return total;
    }

    /** @param total the total to set */
    public void setTotal(Integer total) {
        this.total = total;
    }
}
