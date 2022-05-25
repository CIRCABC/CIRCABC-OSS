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
public final class InterestGroupItem implements Serializable, AclAwareWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 2898619656671170057L;

    private final NodeRef id;
    private final String name;
    private final String title;
    private final String permission;

    public InterestGroupItem(final NodeRef id, final String name, final String permission,
                             final String title) {
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.title = title;
    }

    public NodeRef getNodeRef() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public final String getTitle() {
        return title;
    }

}
