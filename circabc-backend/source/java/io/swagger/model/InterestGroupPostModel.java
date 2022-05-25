package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The object of an Interest group in CIRCABC to be created
 */
public class InterestGroupPostModel {

    private String name = null;

    private I18nProperty title = new I18nProperty();

    private I18nProperty description = new I18nProperty();

    private I18nProperty contact = new I18nProperty();

    private List<String> leaders = new ArrayList<>();

    private Boolean notify = false;

    private I18nProperty notifyText = new I18nProperty();

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

    public InterestGroupPostModel description(I18nProperty description) {
        this.description = description;
        return this;
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

    public InterestGroupPostModel contact(I18nProperty contact) {
        this.contact = contact;
        return this;
    }

    /**
     * Get contact
     *
     * @return contact
     */
    public I18nProperty getContact() {
        return contact;
    }

    public void setContact(I18nProperty contact) {
        this.contact = contact;
    }

    public InterestGroupPostModel leaders(List<String> leaders) {
        this.leaders = leaders;
        return this;
    }

    public InterestGroupPostModel addLeadersItem(String leadersItem) {
        if (this.leaders == null) {
            this.leaders = new ArrayList<>();
        }
        this.leaders.add(leadersItem);
        return this;
    }

    /**
     * Get leaders
     *
     * @return leaders
     */
    public List<String> getLeaders() {
        return leaders;
    }

    public void setLeaders(List<String> leaders) {
        this.leaders = leaders;
    }

    public InterestGroupPostModel notify(Boolean notify) {
        this.notify = notify;
        return this;
    }

    /**
     * Get notify
     *
     * @return notify
     */
    public Boolean getNotify() {
        return notify;
    }

    public void setNotify(Boolean notify) {
        this.notify = notify;
    }

    public I18nProperty getNotifyText() {
        return notifyText;
    }

    public void setNotifyText(I18nProperty notifyText) {
        this.notifyText = notifyText;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InterestGroupPostModel interestGroupPostModel = (InterestGroupPostModel) o;
        return Objects.equals(this.name, interestGroupPostModel.name)
                && Objects.equals(this.title, interestGroupPostModel.title)
                && Objects.equals(this.description, interestGroupPostModel.description)
                && Objects.equals(this.contact, interestGroupPostModel.contact)
                && Objects.equals(this.leaders, interestGroupPostModel.leaders)
                && Objects.equals(this.notify, interestGroupPostModel.notify)
                && Objects.equals(this.notifyText, interestGroupPostModel.notifyText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, title, description, contact, leaders, notify);
    }

    @Override
    public String toString() {

        return "class InterestGroupPostModel {\n"
                + "    name: "
                + toIndentedString(name)
                + "\n"
                + "    title: "
                + toIndentedString(title)
                + "\n"
                + "    description: "
                + toIndentedString(description)
                + "\n"
                + "    contact: "
                + toIndentedString(contact)
                + "\n"
                + "    leaders: "
                + toIndentedString(leaders)
                + "\n"
                + "    notify: "
                + toIndentedString(notify)
                + "\n"
                + "    notifyText: "
                + toIndentedString(notifyText)
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
