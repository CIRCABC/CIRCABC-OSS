/**
 *
 */
package io.swagger.model;

/** @author beaurpi */
public class ListingOptions {

    private Integer page = 1;
    private Integer limit = 10;
    private String sort = "modified_DESC";

    /** @return the page */
    public Integer getPage() {
        return page;
    }

    /** @param page the page to set */
    public void setPage(Integer page) {
        this.page = page;
    }

    /** @return the limit */
    public Integer getLimit() {
        return limit;
    }

    /** @param limit the limit to set */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /** @return the sort */
    public String getSort() {
        return sort;
    }

    /** @param sort the sort to set */
    public void setSort(String sort) {
        this.sort = sort;
    }
}
