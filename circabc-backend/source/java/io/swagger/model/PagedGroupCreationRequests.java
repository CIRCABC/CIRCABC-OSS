package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PagedGroupCreationRequests
 */
public class PagedGroupCreationRequests {

    private List<GroupCreationRequest> data = new ArrayList<>();

    private Long total = null;

    /**
     * Get data
     *
     * @return data
     */
    public List<GroupCreationRequest> getData() {
        return data;
    }

    public void setData(List<GroupCreationRequest> data) {
        this.data = data;
    }

    /**
     * Get total
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
        PagedGroupCreationRequests pagedNews = (PagedGroupCreationRequests) o;
        return Objects.equals(this.data, pagedNews.data) && Objects.equals(this.total, pagedNews.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, total);
    }

    @Override
    public String toString() {

        return "class PagedNews {\n"
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
