/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.business.api.nav;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;
import java.util.Map;

/**
 * Business service that helps the navigation througt circabc spaces and contents.
 *
 * @author Yanick Pignot
 */
public interface NavigationBusinessSrv {

    /**
     * Return the node path of a NodeRef as a String. Must -	try first to return a circabc like path,
     * -	if the node is not managed by circabc, return the alfresco like path -	if any error occurs,
     * return the nodeRef passed in parameter as Srting.
     *
     * @param nodeRef The Not Null nodeRef to retreive path
     * @return Always an not null String
     */
    String getNodePathString(final NodeRef nodeRef);


    /**
     * Get the Circabc root node
     *
     * <p><i>Circabc root folder is visible for every body, authenticated or not</p>
     */
    NodeRef getCircabcRoot();

    /**
     * Get all headers defined in circabc
     *
     * <p><i>Headers are visible for every body, authenticated or not</p>
     */
    List<NodeRef> getHeaders();

    /**
     * Get all categories for a given header
     *
     * <p><i>categories are visible for every body, authenticated or not</i></p>
     **/
    List<NodeRef> getCategories();

    /**
     * Get all categories for a given header
     *
     * <p><i>categories are visible for every body, authenticated or not</i></p>
     */
    List<NodeRef> getCategories(final NodeRef categoryHeader);

    /**
     * Get the category with the given name. Null if not exists
     */
    NodeRef getCategory(String categoryName);

    /**
     * Return all categories for each header
     *
     * <p><i>Headers and Categories are visible for every body, authenticated or not</i></p>
     *
     * @return A map with as key: all headers. As values: each categories in the key header.
     */
    Map<NodeRef, List<NodeRef>> getCategoriesByHeader();

    /**
     * Return all interestgroup for a given category <b>visible for the current authenticated
     * user</b>.
     */
    List<NodeRef> getInterestGroups(final NodeRef category);

    /**
     * Return all interestgroup for a given category <b>visible for the current authenticated
     * user</b>. The list is sorted by Interst Group Visibility Mode (Public, Registred or Members)
     *
     * <p><i>All know Access Mode will be found in the keys, consequently the values can be empty
     * lists but never null</i></p>
     *
     * @return A map with as key: all Interest group modes. As values: each interestgourp in the
     * key mode.
     */
    Map<InterestGroupAccessMode, List<NodeRef>> getInterestGroupsByMode(final NodeRef category);

    /**
     * Return all interestgroup services for a given interestgroup <b>visible for the current
     * authenticated user</b>. The list is sorted by Interst Group Service (Information, Library,
     * Newsgroup, Directory, ... )
     *
     * <p><i>The values will never be null, consequently all services may not appears in the
     * keys</i></p>
     *
     * @return A map with as key: all Interest group services identifier. As values: the node
     * reference of the service for the key.
     */
    Map<InterestGroupServices, NodeRef> getInterestGroupServices(final NodeRef interestGroup);

    /**
     * Return true if the given node is a container (Space, Forum, Topic, ...)
     */
    boolean isContainer(final NodeRef ref);

    /**
     * Return true if the given node is a content (Document, URL, ...)
     */
    boolean isContent(final NodeRef ref);
}
