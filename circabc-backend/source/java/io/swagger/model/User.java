package io.swagger.model;

import java.util.Objects;

/**
 * a CIRCABC user
 */
public class User {

    private String userId = null;
    private String firstname = null;
    private String lastname = null;
    private String email = null;
    private String phone = null;
    private String uiLang = null;
    private String contentFilterLang = null;
    private Boolean visibility = null;
    private Object properties = null;
    private String avatar = null;
    private boolean defaultAvatar = false;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUiLang() {
        return uiLang;
    }

    public void setUiLang(String uiLang) {
        this.uiLang = uiLang;
    }

    public String getContentFilterLang() {
        return contentFilterLang;
    }

    public void setContentFilterLang(String contentFilterLang) {
        this.contentFilterLang = contentFilterLang;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public Object getProperties() {
        return properties;
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * @return the defaultAvatar
     */
    public boolean isDefaultAvatar() {
        return defaultAvatar;
    }

    /**
     * @param defaultAvatar the defaultAvatar to set
     */
    public void setDefaultAvatar(boolean defaultAvatar) {
        this.defaultAvatar = defaultAvatar;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(this.userId, user.userId)
                && Objects.equals(this.firstname, user.firstname)
                && Objects.equals(this.lastname, user.lastname)
                && Objects.equals(this.email, user.email)
                && Objects.equals(this.phone, user.phone)
                && Objects.equals(this.uiLang, user.uiLang)
                && Objects.equals(this.contentFilterLang, user.contentFilterLang)
                && Objects.equals(this.visibility, user.visibility)
                && Objects.equals(this.properties, user.properties)
                && Objects.equals(this.defaultAvatar, user.defaultAvatar)
                && Objects.equals(this.avatar, user.avatar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                userId,
                firstname,
                lastname,
                email,
                phone,
                uiLang,
                contentFilterLang,
                visibility,
                properties,
                avatar,
                defaultAvatar);
    }

    @Override
    public String toString() {

        return "class User {\n"
                + "    userId: "
                + toIndentedString(userId)
                + "\n"
                + "    firstname: "
                + toIndentedString(firstname)
                + "\n"
                + "    lastname: "
                + toIndentedString(lastname)
                + "\n"
                + "    email: "
                + toIndentedString(email)
                + "\n"
                + "    phone: "
                + toIndentedString(phone)
                + "\n"
                + "    uiLang: "
                + toIndentedString(uiLang)
                + "\n"
                + "    contentFilterLang: "
                + toIndentedString(contentFilterLang)
                + "\n"
                + "    visibility: "
                + toIndentedString(visibility)
                + "\n"
                + "    properties: "
                + toIndentedString(properties)
                + "\n"
                + "    avatar: "
                + toIndentedString(avatar)
                + "\n"
                + "    defaultAvatar: "
                + toIndentedString(defaultAvatar)
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
