package io.swagger.api;

import io.swagger.model.InterestGroup;
import io.swagger.model.Node;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.NodeRef;


import java.util.List;

/**
 * @author beaurpi
 */
public interface NodesApi {

    Node getNode(final NodeRef nodeRef);

    Node getNode(final NodeRef nodeRef, Node node);

    Node getNodeById(String id);

    List<Node> getPathByNode(String id);

    /**
     * used to update the ownership of the node the currently alf authenticated user
     * will take the
     * ownership
     */
    Node nodesIdOwnershipPut(String id);

    String generateUniqueName(final NodeRef parent, final String candidateName);

    String getFileNameExtension(final String fileName);

    String removeFileNameExtension(final String fileName);

    InterestGroup nodesIdGroupGet(String id) throws InvalidArgumentException;
}
