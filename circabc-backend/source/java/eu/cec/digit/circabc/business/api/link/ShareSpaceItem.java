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
package eu.cec.digit.circabc.business.api.link;

import eu.cec.digit.circabc.business.acl.AclAwareWrapper;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;


/**
 * @author Slobodan Filipovic
 */
public final class ShareSpaceItem implements Serializable, AclAwareWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 4555015582693128592L;

    private NodeRef id;
    private String path;


    public ShareSpaceItem(NodeRef id, String path) {
        this.id = id;
        this.path = path;
    }

    public NodeRef getNodeRef() {
        return id;
    }

    public String getPath() {
        return path;
    }
}
