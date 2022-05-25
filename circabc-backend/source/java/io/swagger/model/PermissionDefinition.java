package io.swagger.model;

import java.util.Objects;

/**
 * the representation of all the permission configuration of one node
 */
public class PermissionDefinition {

    private Boolean inherited = false;

    private PermissionDefinitionPermissions permissions = new PermissionDefinitionPermissions();

    /**
     * Get inherited
     *
     * @return inherited
     */
    public Boolean getInherited() {
        return inherited;
    }

    public void setInherited(Boolean inherited) {
        this.inherited = inherited;
    }

    /**
     * Get permissions
     *
     * @return permissions
     */
    public PermissionDefinitionPermissions getPermissions() {
        return permissions;
    }

    public void setPermissions(PermissionDefinitionPermissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PermissionDefinition permissionDefinition = (PermissionDefinition) o;
        return Objects.equals(this.inherited, permissionDefinition.inherited)
                && Objects.equals(this.permissions, permissionDefinition.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inherited, permissions);
    }

    @Override
    public String toString() {

        return "class PermissionDefinition {\n"
                + "    inherited: "
                + toIndentedString(inherited)
                + "\n"
                + "    permissions: "
                + toIndentedString(permissions)
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
