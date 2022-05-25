package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PagedSearchNodes
 */
@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.SpringCodegen",
        date = "2017-03-22T15:13:11.258+01:00")
public class PagedSearchNodes {

    private List<SearchNode> data = new ArrayList<SearchNode>();

    private Long total = null;

    public PagedSearchNodes addDataItem(SearchNode dataItem) {
        this.data.add(dataItem);
        return this;
    }

    /**
     * Get data
     *
     * @return data
     */
    public List<SearchNode> getData() {
        return data;
    }

    public void setData(List<SearchNode> data) {
        this.data = data;
    }

    /**
     * return the total amount of nodes, without paging so the UI can compute the number of pages
     *
     * @return total
     */
    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PagedSearchNodes pagedSearchNodes = (PagedSearchNodes) o;
        return Objects.equals(this.data, pagedSearchNodes.data)
                && Objects.equals(this.total, pagedSearchNodes.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, total);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PagedSearchNodes {\n");

        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("    total: ").append(toIndentedString(total)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }
}
