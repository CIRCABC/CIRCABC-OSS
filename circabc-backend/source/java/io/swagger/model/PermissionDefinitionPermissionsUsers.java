package io.swagger.model;

import java.util.Objects;

/**
 * PermissionDefinitionPermissionsUsers
 */
public class PermissionDefinitionPermissionsUsers {

    private User user = null;
    private Boolean inherited;
    private String permission = null;

    /**
     * Get user
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get permission
     *
     * @return permission
     */
    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * @return the inherited
     */
    public Boolean getInherited() {
        return inherited;
    }

    /**
     * @param inherited the inherited to set
     */
    public void setInherited(Boolean inherited) {
        this.inherited = inherited;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PermissionDefinitionPermissionsUsers permissionDefinitionPermissionsUsers =
                (PermissionDefinitionPermissionsUsers) o;
        return Objects.equals(this.user, permissionDefinitionPermissionsUsers.user)
                && Objects.equals(this.permission, permissionDefinitionPermissionsUsers.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, permission);
    }

    @Override
    public String toString() {

        return "class PermissionDefinitionPermissionsUsers {\n"
                + "    user: "
                + toIndentedString(user)
                + "\n"
                + "    permission: "
                + toIndentedString(permission)
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
