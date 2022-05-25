package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PagedUserProfile
 */
public class PagedUserProfile {

    private List<UserProfile> data = new ArrayList<>();

    private Integer total = null;

    public PagedUserProfile data(List<UserProfile> data) {
        this.data = data;
        return this;
    }

    public PagedUserProfile addDataItem(UserProfile dataItem) {
        this.data.add(dataItem);
        return this;
    }

    /**
     * Get data
     *
     * @return data
     */
    public List<UserProfile> getData() {
        return data;
    }

    public void setData(List<UserProfile> data) {
        this.data = data;
    }

    public PagedUserProfile total(Integer total) {
        this.total = total;
        return this;
    }

    /**
     * return the total amount of nodes, without paging so the UI can compute the number of pages
     *
     * @return total
     */
    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
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
        PagedUserProfile pagedUserProfile = (PagedUserProfile) o;
        return Objects.equals(this.data, pagedUserProfile.data)
                && Objects.equals(this.total, pagedUserProfile.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, total);
    }

    @Override
    public String toString() {

        return "class PagedUserProfile {\n"
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
