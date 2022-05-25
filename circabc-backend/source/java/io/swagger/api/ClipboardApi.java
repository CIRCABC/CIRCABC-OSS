package io.swagger.api;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Api to copy, move and link nodes.
 *
 * @author schwerr
 */
public interface ClipboardApi {

    /**
     * Pastes a node into the destination folder. According to the action, the node is copied, linked
     * or moved.
     */
    void paste(final NodeRef nodeRef, final NodeRef destRef, final int action) throws Exception;

    /**
     * Pastes a list of nodes given as a comma separated string into the destination folder. According
     * to the action, the node is copied, linked or moved.
     */
    void paste(final String[] nodeIds, final NodeRef destRef, final int action) throws Exception;
}
