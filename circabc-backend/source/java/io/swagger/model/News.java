package io.swagger.model;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.*;

/**
 * News
 */
public class News {

    private String id = null;

    private I18nProperty title = null;

    private String content = null;

    private LocalDate date = null;

    private Map<String, String> permissions = new HashMap<>();
    private Map<String, String> properties = new HashMap<>();

    private PatternEnum pattern = null;
    private LayoutEnum layout = null;
    private Integer size = null;
    private List<Node> files = new ArrayList<>();
    private DateTime modified = null;
    private String modifier = null;
    private DateTime created = null;
    private String creator = null;
    private String url = null;

    /**
     * Get id
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get title
     *
     * @return title
     */
    public I18nProperty getTitle() {
        return title;
    }

    public void setTitle(I18nProperty title) {
        this.title = title;
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
     * Get date
     *
     * @return date
     */
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Get pattern
     *
     * @return pattern
     */
    public PatternEnum getPattern() {
        return pattern;
    }

    public void setPattern(PatternEnum pattern) {
        this.pattern = pattern;
    }

    /**
     * Get layout
     *
     * @return layout
     */
    public LayoutEnum getLayout() {
        return layout;
    }

    public void setLayout(LayoutEnum layout) {
        this.layout = layout;
    }

    /**
     * valid values are 1 / 2 / 3
     *
     * @return size
     */
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * Get files
     *
     * @return files
     */
    public List<Node> getFiles() {
        return files;
    }

    public void setFiles(List<Node> files) {
        this.files = files;
    }

    /**
     * Get modified
     *
     * @return modified
     */
    public DateTime getModified() {
        return modified;
    }

    public void setModified(DateTime modified) {
        this.modified = modified;
    }

    /**
     * Get modifier
     *
     * @return modifier
     */
    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        News news = (News) o;
        return Objects.equals(this.id, news.id)
                && Objects.equals(this.title, news.title)
                && Objects.equals(this.content, news.content)
                && Objects.equals(this.date, news.date)
                && Objects.equals(this.pattern, news.pattern)
                && Objects.equals(this.layout, news.layout)
                && Objects.equals(this.size, news.size)
                && Objects.equals(this.files, news.files)
                && Objects.equals(this.modified, news.modified)
                && Objects.equals(this.modifier, news.modifier)
                && Objects.equals(this.created, news.created)
                && Objects.equals(this.creator, news.creator)
                && Objects.equals(this.permissions, news.permissions)
                && Objects.equals(this.properties, news.properties)
                && Objects.equals(this.url, news.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                title,
                content,
                date,
                pattern,
                layout,
                size,
                files,
                modified,
                modifier,
                created,
                creator,
                permissions,
                url);
    }

    @Override
    public String toString() {

        return "class News {\n"
                + "    id: "
                + toIndentedString(id)
                + "\n"
                + "    title: "
                + toIndentedString(title)
                + "\n"
                + "    content: "
                + toIndentedString(content)
                + "\n"
                + "    date: "
                + toIndentedString(date)
                + "\n"
                + "    pattern: "
                + toIndentedString(pattern)
                + "\n"
                + "    layout: "
                + toIndentedString(layout)
                + "\n"
                + "    size: "
                + toIndentedString(size)
                + "\n"
                + "    files: "
                + toIndentedString(files)
                + "\n"
                + "    modified: "
                + toIndentedString(modified)
                + "\n"
                + "    modifier: "
                + toIndentedString(modifier)
                + "    created: "
                + toIndentedString(created)
                + "    creator: "
                + toIndentedString(creator)
                + "\n"
                + "    permissions: "
                + toIndentedString(permissions)
                + "\n"
                + "    properties: "
                + toIndentedString(properties)
                + "\n"
                + "    url: "
                + toIndentedString(url)
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

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Gets or Sets pattern
     */
    public enum PatternEnum {
        TEXT("text"),

        DOCUMENT("document"),

        IMAGE("image"),

        DATE("date"),

        IFRAME("iframe");

        private String value;

        PatternEnum(String value) {
            this.value = value;
        }

        public static PatternEnum fromValue(String text) {
            for (PatternEnum b : PatternEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * Gets or Sets layout
     */
    public enum LayoutEnum {
        NORMAL("normal"),

        IMPORTANT("important"),

        REMINDER("reminder");

        private String value;

        LayoutEnum(String value) {
            this.value = value;
        }

        public static LayoutEnum fromValue(String text) {
            for (LayoutEnum b : LayoutEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
