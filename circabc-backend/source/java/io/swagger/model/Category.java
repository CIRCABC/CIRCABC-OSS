package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representation of a Category in CIRCABC in a JSON object
 */
public class Category {

    private String id = null;
    private String name = null;
    private I18nProperty title = new I18nProperty();
    private String logoRef = null;
    private Boolean useSingleContact = false;
    private List<String> contactEmails = new ArrayList<>();
    private Boolean contactVerified = false;

    public Category() {
        super();
    }

    public Category(String id, String name) {
        super();
        this.id = id;
        this.name = name;
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

    public String getLogoRef() {
        return logoRef;
    }

    public void setLogoRef(String logoRef) {
        this.logoRef = logoRef;
    }

    public Boolean getUseSingleContact() {
        return useSingleContact;
    }

    public void setUseSingleContact(Boolean useSingleContact) {
        this.useSingleContact = useSingleContact;
    }

    public List<String> getContactEmails() {
        return contactEmails;
    }

    public void setContactEmails(List<String> contactEmails) {
        this.contactEmails = contactEmails;
    }

    public Boolean getContactVerified() {
        return contactVerified;
    }

    public void setContactVerified(Boolean contactVerified) {
        this.contactVerified = contactVerified;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Category category = (Category) o;
        return Objects.equals(this.id, category.id)
                && Objects.equals(this.name, category.name)
                && Objects.equals(this.title, category.title)
                && Objects.equals(this.logoRef, category.logoRef)
                && Objects.equals(this.useSingleContact, category.useSingleContact)
                && Objects.equals(this.contactEmails, category.contactEmails)
                && Objects.equals(this.contactVerified, category.contactVerified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, title, logoRef, useSingleContact, contactEmails, contactVerified);
    }

    @Override
    public String toString() {

        return "class Category {\n"
                + "    id: "
                + toIndentedString(id)
                + "\n"
                + "    name: "
                + toIndentedString(name)
                + "\n"
                + "    title: "
                + toIndentedString(title)
                + "\n"
                + "    logoRef: "
                + toIndentedString(logoRef)
                + "\n"
                + "    useSingleContact: "
                + toIndentedString(useSingleContact)
                + "\n"
                + "    contactEmails: "
                + toIndentedString(contactEmails)
                + "\n"
                + "    contactVerified: "
                + toIndentedString(contactVerified)
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
