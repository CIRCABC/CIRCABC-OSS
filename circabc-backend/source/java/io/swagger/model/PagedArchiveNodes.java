package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PagedArchiveNodes
 */
public class PagedArchiveNodes {

    private List<ArchiveNode> data = new ArrayList<>();

    private Long total = null;

    /**
     * Get data
     *
     * @return data
     */
    public List<ArchiveNode> getData() {
        return data;
    }

    public void setData(List<ArchiveNode> data) {
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
        PagedArchiveNodes pagedArchiveNodes = (PagedArchiveNodes) o;
        return Objects.equals(this.data, pagedArchiveNodes.data)
                && Objects.equals(this.total, pagedArchiveNodes.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, total);
    }

    @Override
    public String toString() {

        return "class PagedArchiveNodes {\n"
                + "    data: "
                + toIndentedString(data)
                + "\n"
                + "    total: "
                + toIndentedString(total)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }
}
