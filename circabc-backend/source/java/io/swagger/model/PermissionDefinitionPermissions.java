package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PermissionDefinitionPermissions
 */
public class PermissionDefinitionPermissions {

    private List<PermissionDefinitionPermissionsProfiles> profiles = new ArrayList<>();

    private List<PermissionDefinitionPermissionsUsers> users = new ArrayList<>();

    /**
     * Get profiles
     *
     * @return profiles
     */
    public List<PermissionDefinitionPermissionsProfiles> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<PermissionDefinitionPermissionsProfiles> profiles) {
        this.profiles = profiles;
    }

    /**
     * Get users
     *
     * @return users
     */
    public List<PermissionDefinitionPermissionsUsers> getUsers() {
        return users;
    }

    public void setUsers(List<PermissionDefinitionPermissionsUsers> users) {
        this.users = users;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PermissionDefinitionPermissions permissionDefinitionPermissions =
                (PermissionDefinitionPermissions) o;
        return Objects.equals(this.profiles, permissionDefinitionPermissions.profiles)
                && Objects.equals(this.users, permissionDefinitionPermissions.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profiles, users);
    }

    @Override
    public String toString() {

        return "class PermissionDefinitionPermissions {\n"
                + "    profiles: "
                + toIndentedString(profiles)
                + "\n"
                + "    users: "
                + toIndentedString(users)
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
