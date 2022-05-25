package io.swagger.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InterestGroup {

    private String id = null;

    private String name = null;

    private I18nProperty title = new I18nProperty();

    private I18nProperty description = new I18nProperty();

    private I18nProperty contact = new I18nProperty();

    private String libraryId;

    private String informationId;

    private String newsgroupId;

    private String eventId;

    private Boolean isPublic = null;

    private Boolean isRegistered = null;

    private Boolean allowApply = null;

    private Map<String, String> permissions = new HashMap<>();

    private String logoUrl = null;

    public InterestGroup() {
        this.informationId = "";
        this.libraryId = "";
        this.eventId = "";
        this.newsgroupId = "";
    }

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
     * Get libraryId
     *
     * @return libraryId
     */
    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    /**
     * Get informationId
     *
     * @return informationId
     */
    public String getInformationId() {
        return informationId;
    }

    public void setInformationId(String informationId) {
        this.informationId = informationId;
    }

    /**
     * Get newsgroupId
     *
     * @return newsgroupId
     */
    public String getNewsgroupId() {
        return newsgroupId;
    }

    public void setNewsgroupId(String newsgroupId) {
        this.newsgroupId = newsgroupId;
    }

    /**
     * Get eventId
     *
     * @return eventId
     */
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Get isPublic
     *
     * @return isPublic
     */
    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Get isRegistered
     *
     * @return isRegistered
     */
    public Boolean getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(Boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    /**
     * Get allowApply
     *
     * @return allowApply
     */
    public Boolean getAllowApply() {
        return allowApply;
    }

    public void setAllowApply(Boolean allowApply) {
        this.allowApply = allowApply;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InterestGroup interestGroup = (InterestGroup) o;
        return Objects.equals(this.id, interestGroup.id)
                && Objects.equals(this.name, interestGroup.name)
                && Objects.equals(this.title, interestGroup.title)
                && Objects.equals(this.description, interestGroup.description)
                && Objects.equals(this.libraryId, interestGroup.libraryId)
                && Objects.equals(this.informationId, interestGroup.informationId)
                && Objects.equals(this.newsgroupId, interestGroup.newsgroupId)
                && Objects.equals(this.eventId, interestGroup.eventId)
                && Objects.equals(this.isPublic, interestGroup.isPublic)
                && Objects.equals(this.isRegistered, interestGroup.isRegistered)
                && Objects.equals(this.allowApply, interestGroup.allowApply)
                && Objects.equals(this.logoUrl, interestGroup.logoUrl)
                && Objects.equals(this.contact, interestGroup.contact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                title,
                description,
                libraryId,
                informationId,
                newsgroupId,
                eventId,
                isPublic,
                isRegistered,
                allowApply,
                logoUrl,
                contact);
    }

    @Override
    public String toString() {

        return "class InterestGroup {\n"
                + "    id: "
                + toIndentedString(id)
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
                + "    libraryId: "
                + toIndentedString(libraryId)
                + "\n"
                + "    informationId: "
                + toIndentedString(informationId)
                + "\n"
                + "    newsgroupId: "
                + toIndentedString(newsgroupId)
                + "\n"
                + "    eventId: "
                + toIndentedString(eventId)
                + "\n"
                + "    isPublic: "
                + toIndentedString(isPublic)
                + "\n"
                + "    isRegistered: "
                + toIndentedString(isRegistered)
                + "\n"
                + "    allowApply: "
                + toIndentedString(allowApply)
                + "\n"
                + "    logoUrl: "
                + toIndentedString(logoUrl)
                + "\n"
                + "    contact: "
                + toIndentedString(contact)
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    /**
     * @return the contact
     */
    public I18nProperty getContact() {
        return contact;
    }

    /**
     * @param contact the contact to set
     */
    public void setContact(I18nProperty contact) {
        this.contact = contact;
    }
}
