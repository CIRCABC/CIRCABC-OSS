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
package eu.cec.digit.circabc.business.api.content;

import eu.cec.digit.circabc.business.acl.AclAwareWrapper;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Yanick Pignot
 */
public interface Attachement extends AclAwareWrapper {

    /**
     * AclAware wrapper method contract to allow permission check on this wrapper
     *
     * @see eu.cec.digit.circabc.business.acl.AclAwareWrapper#getNodeRef()
     */
    NodeRef getNodeRef();

    /**
     * Get the node on wich the node is attached
     */
    NodeRef getAttachedOn();

    /**
     * Get the name of the attachement node
     */
    String getName();

    /**
     * Get the title of the attachement node
     */
    String getTitle();

    /**
     * Get the type of the attachement.
     */
    AttachementType geType();

    long getSize();

    void setSize(long size);

    String getMimetype();

    void setMimetype(String mimetype);

    String getEncoding();

    void setEncoding(String encoding);

    /**
     * Represent the kind of attachement.
     * <p>
     * A attachement can be either -	a link in the repository. -	a content hidden under the node where
     * it is attached.
     *
     * @author Yanick Pignot
     */
    enum AttachementType {
        REPO_LINK,

        HIDDEN_FILE
    }
}
