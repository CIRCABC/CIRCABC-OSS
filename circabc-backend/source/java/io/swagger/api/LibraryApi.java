/**
 *
 */
package io.swagger.api;

import io.swagger.model.Node;
import io.swagger.model.Profile;

import java.util.List;

/** @author beaurpi */
public interface LibraryApi {

    List<Node> getLockedNodes(String nodeId);

    List<Node> getSharedNodes(String nodeId);

    List<Profile> getSharedProfiles(String nodeId);
}
