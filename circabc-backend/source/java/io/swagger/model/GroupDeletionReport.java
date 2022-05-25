/**
 *
 */
package io.swagger.model;

import java.util.List;

/** @author beaurpi */
public class GroupDeletionReport {

    private List<Node> lockedNodes;
    private List<Node> sharedFolders;
    private List<Profile> sharedProfiles;

    /** @return the lockedNodes */
    public List<Node> getLockedNodes() {
        return lockedNodes;
    }

    /** @param lockedNodes the lockedNodes to set */
    public void setLockedNodes(List<Node> lockedNodes) {
        this.lockedNodes = lockedNodes;
    }

    /** @return the sharedFolders */
    public List<Node> getSharedFolders() {
        return sharedFolders;
    }

    /** @param sharedFolders the sharedFolders to set */
    public void setSharedFolders(List<Node> sharedFolders) {
        this.sharedFolders = sharedFolders;
    }

    /** @return the sharedProfiles */
    public List<Profile> getSharedProfiles() {
        return sharedProfiles;
    }

    /** @param sharedProfiles the sharedProfiles to set */
    public void setSharedProfiles(List<Profile> sharedProfiles) {
        this.sharedProfiles = sharedProfiles;
    }
}
