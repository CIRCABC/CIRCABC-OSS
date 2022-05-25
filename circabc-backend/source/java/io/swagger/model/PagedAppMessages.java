package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PagedAppMessages {

    private List<AppMessage> data = new ArrayList<>();

    private Long total = null;

    /**
     * Get data
     *
     * @return data
     */
    public List<AppMessage> getData() {
        return data;
    }

    public void setData(List<AppMessage> data) {
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
        PagedAppMessages pagedNodes = (PagedAppMessages) o;
        return Objects.equals(this.data, pagedNodes.data)
                && Objects.equals(this.total, pagedNodes.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, total);
    }

    @Override
    public String toString() {

        return "class PagedNodes {\n"
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
