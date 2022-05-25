package io.swagger.api;

import eu.cec.digit.circabc.business.api.link.ShareSpaceItem;
import io.swagger.model.*;

import java.util.List;

/**
 * @author beaurpi
 */
public interface SpacesApi {

    /**
     * get the content of a space returns a list of space/files/urls or any kind of child of this node
     * /spaces/{id}/children
     */
    List<Node> spaceGetChildren(String id, boolean folderOnly);

    /**
     * creates a new space/folder within the current visited space/folder /spaces/{id}/spaces
     */
    Node spacesIdSpacesPost(String id, Node body);

    /**
     * delete the referenced space/folder DELETE /spaces/{id}
     */
    void spaceDelete(String id);

    /**
     * get the content of a space returns a list of space/files/urls or any kind of child of this node
     * with paging and sorting options
     */
    PagedNodes spaceGetChildren(
            String id, int nbPage, int nbLimit, String sort, boolean folderOnly, boolean fileOnly);

    /**
     * update the definition of a space
     */
    void spacesIdPut(String id, Node body);

    Node spacesIdUrlPost(String id, Node body);

    // Space sharing related methods

    PagedShares getInvitedInterestGroups(String spaceId, int page, int limit);

    void deleteShare(String spaceId, String sharedIGId);

    void addShare(String spaceId, Share share, boolean notifyLeaders);

    void changeSharePermission(
            String sharedSpaceId, String igId, String newPermission, boolean notifyLeaders);

    ShareIGsAndPermissions getShareIGsAndPermissions(String spaceId);

    List<ShareSpaceItem> getAvailableSharedSpaces(String spaceId);

    void createSharedSpaceLink(String spaceId, String parentId, String title, String description);

    PagedNodes restrictedSpaceGetChildren(
            String id, int nbPage, int nbLimit, String sort, boolean folderOnly, boolean fileOnly);
    
    int getFolderSize(String id);
}
