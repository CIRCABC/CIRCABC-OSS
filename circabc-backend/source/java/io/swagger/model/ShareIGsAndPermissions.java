package io.swagger.model;

import org.alfresco.util.Pair;

import java.util.List;

public class ShareIGsAndPermissions {

    List<Pair<String, String>> igs;
    List<String> permissions;

    public ShareIGsAndPermissions(List<Pair<String, String>> igs, List<String> permissions) {
        super();
        this.igs = igs;
        this.permissions = permissions;
    }

    /**
     * @return the igs
     */
    public List<Pair<String, String>> getIgs() {
        return igs;
    }

    /**
     * @param igs the igs to set
     */
    public void setIgs(List<Pair<String, String>> igs) {
        this.igs = igs;
    }

    /**
     * @return the permissions
     */
    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
