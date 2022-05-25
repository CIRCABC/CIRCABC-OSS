package io.swagger.model;

import org.joda.time.DateTime;

import java.util.Objects;

/**
 * AppMessage
 */
@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.SpringCodegen",
        date = "2018-02-19T14:19:50.800+01:00")
public class AppMessage {

    private Integer id = null;

    private String content = null;

    private DateTime dateClosure = null;

    private String level = null;

    private Boolean enabled = false;

    private Integer displayTime = null;

    /**
     * Get id
     *
     * @return id
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get content
     *
     * @return content
     */
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Get dateClosure
     *
     * @return dateClosure
     */
    public DateTime getDateClosure() {
        return dateClosure;
    }

    public void setDateClosure(DateTime dateClosure) {
        this.dateClosure = dateClosure;
    }

    /**
     * Get level
     *
     * @return level
     */
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Get enabled
     *
     * @return enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get displayTime
     *
     * @return displayTime
     */
    public Integer getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(Integer displayTime) {
        this.displayTime = displayTime;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppMessage appMessage = (AppMessage) o;
        return Objects.equals(this.id, appMessage.id)
                && Objects.equals(this.content, appMessage.content)
                && Objects.equals(this.dateClosure, appMessage.dateClosure)
                && Objects.equals(this.level, appMessage.level)
                && Objects.equals(this.enabled, appMessage.enabled)
                && Objects.equals(this.displayTime, appMessage.displayTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, dateClosure, level, enabled, displayTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AppMessage {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    dateClosure: ").append(toIndentedString(dateClosure)).append("\n");
        sb.append("    level: ").append(toIndentedString(level)).append("\n");
        sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
        sb.append("    displayTime: ").append(toIndentedString(displayTime)).append("\n");
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
