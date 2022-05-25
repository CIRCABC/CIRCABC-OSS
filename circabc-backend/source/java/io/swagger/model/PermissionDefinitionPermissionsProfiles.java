package io.swagger.model;

import java.util.Objects;

/**
 * PermissionDefinitionPermissionsProfiles
 */
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.SpringCodegen",
        date = "2016-11-25T13:04:03.820Z")
public class PermissionDefinitionPermissionsProfiles {

    private Profile profile = null;
    private Boolean inherited = null;
    private String permission = null;

    /**
     * Get profile
     *
     * @return profile
     */
    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
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
        PermissionDefinitionPermissionsProfiles permissionDefinitionPermissionsProfiles =
                (PermissionDefinitionPermissionsProfiles) o;
        return Objects.equals(this.profile, permissionDefinitionPermissionsProfiles.profile)
                && Objects.equals(this.permission, permissionDefinitionPermissionsProfiles.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile, permission);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PermissionDefinitionPermissionsProfiles {\n");

        sb.append("    profile: ").append(toIndentedString(profile)).append("\n");
        sb.append("    permission: ").append(toIndentedString(permission)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }
}
