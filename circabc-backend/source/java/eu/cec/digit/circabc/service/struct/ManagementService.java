/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.struct;

import eu.cec.digit.circabc.repo.struct.SimplePath;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import javax.jcr.PathNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * Main circabc management service. It serves to manage the particular structure of primary Circabc
 * Nodes
 *
 * <p>Company Home (The Alfresco Company Home) |_ Circabc [1.1] |_ Category (linked to a Category
 * Header) [1.n] |_ Interest Group Home (alias IgHome) [1.n] |_ Library (location of spaces and
 * documents) [1.1] |_ Newsgroups (location of forums) [1.1] |_ Surveys (Location of the IPM
 * surveys) [1.1] |_ Information (Location of the information about IG (IG Mini site)) [1.1] |_
 * Event (Location of the Circabc events (calendar)) [1.1] |_ <i>And other futur circabc services
 * (as Wiki? Blog?)</i>
 *
 * @author clincst
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface ManagementService {

    String SERVICE_NAME = "ManagementService";
    String DYNAMIC_AUTHORITY_CACHE_ENABLED = "dynamic.authority.cache.enabled";
    String DYNAMIC_AUTHORITY_CACHE_SIZE = "dynamic.authority.cache.size";
    String DYNAMIC_AUTHORITY_CACHE_TIMOUT = "dynamic.authority.cache.timeout";
    String DYNAMIC_AUTHORITY_CACHE_TIMOUT_RESET_ON_READ =
            "dynamic.authority.cache.timeout.reset.on.read";
    String DYNAMIC_AUTHORITY_CACHE_TIMOUT_CLEANUP = "dynamic.authority.cache.timeout.cleanup";
    String PROFILE_MAP_CACHE_ENABLED = "profile.map.cache.enabled";
    String PROFILE_MAP_CACHE_SIZE = "profile.map.cache.size";
    String PROFILE_MAP_CACHE_TIMOUT = "profile.map.cache.timeout";
    String PROFILE_MAP_CACHE_TIMOUT_RESET_ON_READ = "profile.map.cache.timeout.reset.on.read";
    String PROFILE_MAP_CACHE_TIMOUT_CLEANUP = "profile.map.cache.timeout.cleanup";

    String DEFAULT_SPACE_ICON_NAME = "space-icon-default";
    String DEFAULT_DOSSIER_ICON_NAME = "space-icon-pen";
    String DEFAULT_LIBRARY_ICON_NAME = "space-icon-doc";
    String DEFAULT_SURVEY_ICON_NAME = "space-icon-pen";
    String DEFAULT_SPACE_LINK_ICON_NAME = "space-icon-link";
    String DEFAULT_NEWSGROUP_ICON_NAME = "forums";
    String DEFAULT_INFORMATION_ICON_NAME = "../extension/icons/information";
    String DEFAULT_EVENT_ICON_NAME = "../extension/icons/events";
    String DEFAULT_FORUM_ICON_NAME = "forum";
    String DEFAULT_TOPIC_ICON_NAME = "topic";

    /**
     * Create circabc root level
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"parentNodeRef"})
    NodeRef createCircabc(final NodeRef parentNodeRef);

    /**
     * Create CircabcCategory level
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {
            "parentNodeRef",
            "folderName",
            "alfrescoCategoryNodeRef"
    })
    NodeRef createCategory(
            final NodeRef parentNodeRef, final String folderName, final NodeRef alfrescoCategoryNodeRef);

    /**
     * @param parentNodeRef
     * @param folderName
     * @param circabcIGRootDescription
     * @return
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {
            "parentNodeRef",
            "folderName",
            "circabcIGRootContact"
    })
    NodeRef createIGRoot(
            final NodeRef parentNodeRef, final String folderName, final String circabcIGRootContact);

    /**
     * @param parentNodeRef
     * @param folderName
     * @param circabcIGRootContact
     * @param createOptionalProfiles
     * @return
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {
            "parentNodeRef",
            "folderName",
            "circabcIGRootContact"
    })
    NodeRef createIGRoot(
            final NodeRef parentNodeRef,
            final String folderName,
            final String circabcIGRootContact,
            final boolean createOptionalProfiles);

    /**
     * Returns the categoryHearders binded to the given category
     */
    @Auditable
    List<NodeRef> getCategoryHeaders(NodeRef nodeRef);

    /**
     * Return the list of available category headers
     */
    @Auditable
    List<NodeRef> getExistingCategoryHeaders();

    /**
     * Return a Map of Category as key and a List of binded categoryHearders as value
     */
    @Auditable
    Map<NodeRef, List<NodeRef>> getCategoryMap();

    /**
     * Return a Map of CategoryHeaders as key and a List of binded Category as value
     */
    @Auditable
    Map<NodeRef, List<NodeRef>> getCategoryHeadersMap();

    /**
     * @return the root category header node or null if not created yet
     */
    @Auditable(/*key = Auditable.Key.RETURN*/)
    NodeRef getRootCategoryHeader();

    /**
     * Create the library ig service under a given Interest Group
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"parentNodeRef"})
    NodeRef createLibrary(final NodeRef parentNodeRef);

    /**
     * Create the news group ig service under a given Interest Group
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"parentNodeRef"})
    NodeRef createNewsGroup(final NodeRef parentNodeRef);

    /**
     * Create the survey ig service under a given Interest Group
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"parentNodeRef"})
    NodeRef createSurvey(final NodeRef parentNodeRef);

    /**
     * Create the directory ig service under a given Interest Group
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"parentNodeRef"})
    NodeRef createDirectory(final NodeRef parentNodeRef);

    /**
     * Create the Information ig service under a given Interest Group
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"parentNodeRef"})
    NodeRef createInformationService(final NodeRef parentNodeRef);

    /**
     * Create the Event ig service under a given Interest Group
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"parentNodeRef"})
    NodeRef createEventService(final NodeRef parentNodeRef);

    /**
     * Get the company home node reference
     */
    @NotAuditable
    NodeRef getCompanyHomeNodeRef();

    /**
     * Get the guest home node reference
     */
    @NotAuditable
    NodeRef getGuestHomeNodeRef();

    /**
     * Get the circabc root folder noderef
     */
    @NotAuditable
    NodeRef getCircabcNodeRef();

    /**
     * Get the alfresco dictionary noderef
     */
    @NotAuditable
    NodeRef getAlfrescoDictionaryNodeRef();

    /**
     * Get the circabc dictionary noderef
     */
    @NotAuditable
    NodeRef getCircabcDictionaryNodeRef();

    /**
     * Get the the full lucene path of the circabc node
     */
    @NotAuditable
    String getCircabcLucenePath();

    /**
     * Get the the full lucene path of the Alfresco Dictionary
     */
    @NotAuditable
    String getAlfrescoDictionaryLucenePath();

    /**
     * Return the list of existing categories
     */
    @NotAuditable
    List<NodeRef> getCategories();

    /**
     * Return the list of interest group for a given category
     */
    @NotAuditable
    List<NodeRef> getInterestGroups(final NodeRef category);

    /**
     * Return the category matching the given name
     */
    @NotAuditable
    NodeRef getCategory(String categoryName);

    /**
     * Return if the current user is contained in Circabc node
     */
    @NotAuditable
    boolean isCircabcUser(final String userName);

    /**
     * Return if the guest has Visibility on the IGRoot node
     */
    @NotAuditable
    boolean hasGuestVisibility(final NodeRef igRootNodeRef);

    /**
     * Return if the ALL_CIRCA_USER has Visibility on the IGRoot node
     */
    @NotAuditable
    boolean hasAllCircabcUsersVisibility(final NodeRef igRootNodeRef);

    /**
     * Gets the reference of the current interest group.
     *
     * @param currentNodeRef The current node's ref
     * @return a reference to the current intereest group
     */
    @NotAuditable
    NodeRef getCurrentInterestGroup(final NodeRef currentNodeRef);

    /**
     * Gets the reference of the current library node.
     *
     * @param currentNodeRef The current node's ref
     * @return a reference to the current library node
     */
    @NotAuditable
    NodeRef getCurrentLibrary(final NodeRef currentNodeRef);

    /**
     * Gets the reference of the current newsGroups node.
     *
     * @param currentNodeRef The current node's ref
     * @return a reference to the current newsGroups node
     */
    @NotAuditable
    NodeRef getCurrentNewsGroup(final NodeRef currentNodeRef);

    /**
     * Gets the reference of the current surveys node.
     *
     * @param currentNodeRef The current node's ref
     * @return a reference to the current survey node
     */
    @NotAuditable
    NodeRef getCurrentSurvey(final NodeRef currentNodeRef);

    /**
     * Gets the reference of the current Circabc category.
     *
     * @param currentNodeRef The current node's ref
     * @return a reference to the current Circabc category
     */
    @NotAuditable
    NodeRef getCurrentCategory(final NodeRef currentNodeRef);

    /**
     * Gets the reference of the current forum.
     *
     * @param currentNodeRef The current node's ref
     * @return a reference to the current forum
     */
    @NotAuditable
    NodeRef getCurrentForum(final NodeRef currentNodeRef);

    /**
     * Gets the reference of the current topic.
     *
     * @param currentNodeRef The current node's ref
     * @return a reference to the current topic
     */
    @NotAuditable
    NodeRef getCurrentTopic(final NodeRef currentNodeRef);

    /**
     * Get secured list of nodeRef that represent the parents of a given node until the CircabcRoot.
     *
     * @param nodeRef            the node from which the tree stucture must be computed
     * @param includeUntilAspect inculde or not the contents in the path
     * @return A secure list of the parents of the given node
     */
    @NotAuditable
    List<NodeRef> getAllParents(final NodeRef nodeRef, final boolean includeContens);

    /**
     * Get secured list of nodeRef that represent the parents of a given node until the given type of
     * node given as second parameter.
     *
     * @param nodeRef            the node from which the tree stucture must be computed
     * @param includeUntilAspect inculde on not the contents in the path
     * @param untilAspect        the type of the last parent
     * @param includeUntilAspect inculde the last parent parameter or not
     * @return A secure list of the parents of the given node
     */
    @NotAuditable
    List<NodeRef> getAllParents(
            final NodeRef nodeRef,
            final boolean includeContens,
            final QName untilAspect,
            final boolean includeUntilAspect);

    /**
     * Get the path of the circqbc root
     *
     * <p>* @return The 'circabc like' path for the circabc root node
     */
    @NotAuditable
    SimplePath getRootSimplePath() throws PathNotFoundException;

    /**
     * Get the path of the givent node
     *
     * @param nodeRef the node from which the tree stucture must be computed
     * @return The 'circabc like' path for the given NodeRef
     */
    @NotAuditable
    SimplePath getNodePath(final NodeRef nodeRef) throws PathNotFoundException;

    /**
     * Get a list of SimplePath object who are childs of the givent node
     *
     * @param nodeRef the node from which the tree stucture must be computed
     * @return The 'circabc like' path for the given NodeRef
     */
    @NotAuditable
    List<SimplePath> getChildsPath(final NodeRef nodeRef) throws PathNotFoundException;

    /**
     * Get the path of the givent String
     *
     * @param pathString the path as string to parse
     * @return The 'circabc like' path for the given path string
     */
    @NotAuditable
    SimplePath getNodePath(final String pathString) throws PathNotFoundException;

    /**
     * Get secured list of nodeRef that represent the childss of a given node
     *
     * @param nodeRef the node from which the tree stucture must be computed
     * @param folders inculde on not the folders
     * @param files   inculde on not the files
     * @return A secure list of the childs of the given node
     */
    @NotAuditable
    List<NodeRef> luceneSearch(
            final NodeRef contextNodeRef, final boolean folders, final boolean files);

    @NotAuditable
    NodeRef getMTNodeRef();

    /**
     * Create machine translation root level
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"parentNodeRef"})
    NodeRef createMTSpace(final NodeRef parentNodeRef);
}
