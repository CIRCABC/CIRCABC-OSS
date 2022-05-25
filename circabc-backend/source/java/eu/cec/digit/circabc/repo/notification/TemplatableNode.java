/**
 *
 */
package eu.cec.digit.circabc.repo.notification;

import java.util.HashMap;
import java.util.Map;

/** @author beaurpi */
public class TemplatableNode {

    private String name;
    private String title;
    private String description;
    private String modifier;
    private String modified;
    private String keywords;
    private String path;
    private Map<String, String> dynamicProperties = new HashMap<>();
    private String url;

    /** @return the name */
    public String getName() {
        return name;
    }

    /** @param name the name to set */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the title */
    public String getTitle() {
        return title;
    }

    /** @param title the title to set */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @return the description */
    public String getDescription() {
        return description;
    }

    /** @param description the description to set */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return the modifier */
    public String getModifier() {
        return modifier;
    }

    /** @param modifier the modifier to set */
    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    /** @return the modified */
    public String getModified() {
        return modified;
    }

    /** @param modified the modified to set */
    public void setModified(String modified) {
        this.modified = modified;
    }

    /** @return the keywords */
    public String getKeywords() {
        return keywords;
    }

    /** @param keywords the keywords to set */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /** @return the path */
    public String getPath() {
        return path;
    }

    /** @param path the path to set */
    public void setPath(String path) {
        this.path = path;
    }

    /** @return the dynamicProperties */
    public Map<String, String> getDynamicProperties() {
        return dynamicProperties;
    }

    /** @param dynamicProperties the dynamicProperties to set */
    public void setDynamicProperties(Map<String, String> dynamicProperties) {
        this.dynamicProperties = dynamicProperties;
    }

    /** @return the url */
    public String getUrl() {
        return url;
    }

    /** @param url the url to set */
    public void setUrl(String url) {
        this.url = url;
    }
}
