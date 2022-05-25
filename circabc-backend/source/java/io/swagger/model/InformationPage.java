package io.swagger.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * InformationPage
 */
public class InformationPage {

    private String url = null;

    private Boolean adapt = null;

    private Boolean displayOldInformation = false;

    private Map<String, String> permissions = new HashMap<>();

    /**
     * Get url
     *
     * @return url
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get adapt
     *
     * @return adapt
     */
    public Boolean getAdapt() {
        return adapt;
    }

    public void setAdapt(Boolean adapt) {
        this.adapt = adapt;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InformationPage informationPage = (InformationPage) o;
        return Objects.equals(this.url, informationPage.url)
                && Objects.equals(this.adapt, informationPage.adapt)
                && Objects.equals(this.displayOldInformation, informationPage.displayOldInformation)
                && Objects.equals(this.permissions, informationPage.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, adapt, permissions);
    }

    @Override
    public String toString() {

        return "class InformationPage {\n"
                + "    url: "
                + toIndentedString(url)
                + "\n"
                + "    adapt: "
                + toIndentedString(adapt)
                + "\n"
                + "    displayOldInformation: "
                + toIndentedString(displayOldInformation)
                + "\n"
                + "    permissions: "
                + toIndentedString(permissions)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    /**
     * @return the permissions
     */
    public Map<String, String> getPermissions() {
        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    public Boolean getDisplayOldInformation() {
        return displayOldInformation;
    }

    public void setDisplayOldInformation(Boolean displayOldInformation) {
        this.displayOldInformation = displayOldInformation;
    }
}
