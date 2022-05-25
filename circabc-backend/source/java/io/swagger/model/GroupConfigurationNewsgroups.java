package io.swagger.model;

import java.util.Objects;

/**
 * GroupConfigurationNewsgroups
 */
public class GroupConfigurationNewsgroups {

    private Boolean enableFlagNewTopic = null;

    private Boolean enableFlagNewForum = null;

    private Integer ageFlagNewTopic = null;

    private Integer ageFlagNewForum = null;

    public Boolean getEnableFlagNewTopic() {
        return enableFlagNewTopic;
    }

    public void setEnableFlagNewTopic(Boolean enableFlagNewTopic) {
        this.enableFlagNewTopic = enableFlagNewTopic;
    }

    public Boolean getEnableFlagNewForum() {
        return enableFlagNewForum;
    }

    public void setEnableFlagNewForum(Boolean enableFlagNewForum) {
        this.enableFlagNewForum = enableFlagNewForum;
    }

    /**
     * Get ageFlagNewTopic
     *
     * @return ageFlagNewTopic
     */
    public Integer getAgeFlagNewTopic() {
        return ageFlagNewTopic;
    }

    public void setAgeFlagNewTopic(Integer ageFlagNewTopic) {
        this.ageFlagNewTopic = ageFlagNewTopic;
    }

    /**
     * Get ageFlagNewForum
     *
     * @return ageFlagNewForum
     */
    public Integer getAgeFlagNewForum() {
        return ageFlagNewForum;
    }

    public void setAgeFlagNewForum(Integer ageFlagNewForum) {
        this.ageFlagNewForum = ageFlagNewForum;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupConfigurationNewsgroups groupConfigurationNewsgroups = (GroupConfigurationNewsgroups) o;
        return Objects.equals(this.enableFlagNewTopic, groupConfigurationNewsgroups.enableFlagNewTopic)
                && Objects.equals(this.enableFlagNewForum, groupConfigurationNewsgroups.enableFlagNewForum)
                && Objects.equals(this.ageFlagNewTopic, groupConfigurationNewsgroups.ageFlagNewTopic)
                && Objects.equals(this.ageFlagNewForum, groupConfigurationNewsgroups.ageFlagNewForum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enableFlagNewTopic, enableFlagNewForum, ageFlagNewTopic, ageFlagNewForum);
    }

    @Override
    public String toString() {

        return "class GroupConfigurationNewsgroups {\n"
                + "    enableFlagNewTopic: "
                + toIndentedString(enableFlagNewTopic)
                + "\n"
                + "    enableFlagNewForum: "
                + toIndentedString(enableFlagNewForum)
                + "\n"
                + "    ageFlagNewTopic: "
                + toIndentedString(ageFlagNewTopic)
                + "\n"
                + "    ageFlagNewForum: "
                + toIndentedString(ageFlagNewForum)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    public String toJsonString() {
        return "{\n"
                + "\t\"enableFlagNewTopic\": "
                + enableFlagNewTopic
                + ",\n"
                + "\t\"enableFlagNewForum\": "
                + enableFlagNewForum
                + ",\n"
                + "\t\"ageFlagNewTopic\": "
                + ageFlagNewTopic
                + ",\n"
                + "\t\"ageFlagNewForum\": "
                + ageFlagNewForum
                + "\n"
                + "}";
    }
}
