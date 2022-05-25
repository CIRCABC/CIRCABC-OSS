package io.swagger.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * representation of a node
 */
public class Node {

    private String id = null;

    private String type = null;

    private CircabcServiceName service = null;

    private String name = null;

    private I18nProperty title = new I18nProperty();

    private I18nProperty description = new I18nProperty();

    private Map<String, String> properties = new HashMap<>();

    private Map<String, String> permissions = new HashMap<>();

    private String parentId = null;

    private String notifications = "";

    private Boolean favourite = false;

    private Boolean hasSubFolders = false;
    
    private Boolean hasGuestAccess = false;

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
     * Get type
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
     * Get description
     *
     * @return description
     */
    public I18nProperty getDescription() {
        return description;
    }

    public void setDescription(I18nProperty description) {
        this.description = description;
    }

    /**
     * Get properties
     *
     * @return properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Node node = (Node) o;
        return Objects.equals(this.id, node.id)
                && Objects.equals(this.type, node.type)
                && Objects.equals(this.service, node.service)
                && Objects.equals(this.name, node.name)
                && Objects.equals(this.title, node.title)
                && Objects.equals(this.description, node.description)
                && Objects.equals(this.properties, node.properties)
                && Objects.equals(this.notifications, node.notifications)
                && Objects.equals(this.favourite, node.favourite)
                && Objects.equals(this.hasSubFolders, node.hasSubFolders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                type,
                service,
                name,
                title,
                description,
                properties,
                notifications,
                favourite,
                hasSubFolders);
    }

    @Override
    public String toString() {

        return "class Node {\n"
                + "    id: "
                + toIndentedString(id)
                + "\n"
                + "    type: "
                + toIndentedString(type)
                + "\n"
                + "    service: "
                + toIndentedString(service)
                + "\n"
                + "    name: "
                + toIndentedString(name)
                + "\n"
                + "    title: "
                + toIndentedString(title)
                + "\n"
                + "    description: "
                + toIndentedString(description)
                + "\n"
                + "    properties: "
                + toIndentedString(properties)
                + "\n"
                + "    notification: "
                + toIndentedString(notifications)
                + "\n"
                + "    favourite: "
                + toIndentedString(favourite)
                + "\n"
                + "    hasSubFolders: "
                + toIndentedString(hasSubFolders)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    protected String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    /**
     * @return the parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
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
     * @return the notification
     */
    public String getNotifications() {
        return notifications;
    }

    /**
     * @param notifications the notification to set
     */
    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    /**
     * @return the favourite
     */
    public Boolean getFavourite() {
        return favourite;
    }

    /**
     * @param favourite the favourite to set
     */
    public void setFavourite(Boolean favourite) {
        this.favourite = favourite;
    }

    public CircabcServiceName getService() {
        return service;
    }

    public void setService(CircabcServiceName service) {
        this.service = service;
    }

    public Boolean getHasSubFolders() {
        return hasSubFolders;
    }

    public void setHasSubFolders(Boolean hasSubFolders) {
        this.hasSubFolders = hasSubFolders;
    }
    
    public Boolean getHasGuestAccess() {
        return hasGuestAccess;
    }

    public void setHasGuestAccess(Boolean hasGuestAccess) {
        this.hasGuestAccess = hasGuestAccess;
    }
}
