package io.swagger.api;

import io.swagger.model.Profile;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;
import java.util.Set;

/**
 * @author beaurpi
 */
public interface ProfilesApi {

    List<Profile> groupsIdProfilesGet(String id, String searchQuery, boolean nonEmptyProfiles);

    Profile groupsIdProfilesPost(NodeRef nodeRef, Profile body);

    /**
     * noSync for the CIRCABC method Interceptor in the case of a IG Creation. Sync should be done at
     * the end of the IG creation. not during Profile creation
     */
    Profile groupsIdProfilesPostNoSync(NodeRef nodeRef, Profile body);

    /**
     * pass the node ref of the profile to be deleted and return the noderef of the IG to be
     * resynchronized in the DB
     *
     * @return the IGRef
     */
    NodeRef profilesIdDelete(NodeRef profileRef);

    Profile profilesIdPut(NodeRef profileRef, Profile body);

    Profile groupsIdImportedProfilesPost(NodeRef nodeRef, Profile body);

    Set<String> getInvitedUsers(NodeRef nodeRef, String profilePrefix);

    Profile profilesIdGet(String profileId);
}
