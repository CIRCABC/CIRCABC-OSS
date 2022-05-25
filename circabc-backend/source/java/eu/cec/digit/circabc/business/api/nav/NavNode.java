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

import eu.cec.digit.circabc.business.acl.AclAwareWrapper;
import eu.cec.digit.circabc.business.api.props.PropertyItem;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Map;

/**
 * Represent a node in the business layer. Must encapsulate and hide all target implementation
 * details.
 *
 * <p>
 * The code for this class has been shamelessly stolen from Alfresco, but we want to remove our
 * dependency to the alfresco-webclient.jar.
 *
 * @author Yanick Pignot
 * <p>
 * TODO to decide how to use and implement this class. Do we need a single NavNode for ALL puposes,
 * or generalize it to reflect each circabc node types. 1.		One class by type (Circabc, Library,
 * NewsPost, ...). 2.		With perhaps common super interface-class for similar (but not equals type) -
 * A IGServiceNode --> NewsServiceNode, LibraryServiceNode. - A TopLevelNode  --> RootNode, CatNode,
 * IGNode - A ContentNode   --> LibDocument, InfDocument, LibURL, LibPost, NewsPost, ...
 * @see org.alfresco.web.bean.repository.MapNode
 * </p>
 */
public interface NavNode extends Map<String, PropertyItem>, AclAwareWrapper {

    /**
     * Get the node reference identifier.
     *
     * @return The node reference
     */
    NodeRef getNodeRef();
}
