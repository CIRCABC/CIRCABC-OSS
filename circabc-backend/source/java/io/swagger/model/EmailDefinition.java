package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * EmailDefinition
 */
public class EmailDefinition {

    private String subject = null;

    private String content = null;

    private List<String> attachments = new ArrayList<>();

    private List<User> users = new ArrayList<>();

    private List<Profile> profiles = new ArrayList<>();

    private Boolean copyToSender = false;

    /**
     * Get subject
     *
     * @return subject
     */
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
     * Get attachments
     *
     * @return attachments
     */
    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    /**
     * Get users
     *
     * @return users
     */
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Get profiles
     *
     * @return profiles
     */
    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    public Boolean getCopyToSender() {
        return copyToSender;
    }

    public void setCopyToSender(Boolean copyToSender) {
        this.copyToSender = copyToSender;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmailDefinition emailDefinition = (EmailDefinition) o;
        return Objects.equals(this.subject, emailDefinition.subject)
                && Objects.equals(this.content, emailDefinition.content)
                && Objects.equals(this.attachments, emailDefinition.attachments)
                && Objects.equals(this.users, emailDefinition.users)
                && Objects.equals(this.profiles, emailDefinition.profiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, content, attachments, users, profiles);
    }

    @Override
    public String toString() {

        return "class EmailDefinition {\n"
                + "    subject: "
                + toIndentedString(subject)
                + "\n"
                + "    content: "
                + toIndentedString(content)
                + "\n"
                + "    attachments: "
                + toIndentedString(attachments)
                + "\n"
                + "    users: "
                + toIndentedString(users)
                + "\n"
                + "    profiles: "
                + toIndentedString(profiles)
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
