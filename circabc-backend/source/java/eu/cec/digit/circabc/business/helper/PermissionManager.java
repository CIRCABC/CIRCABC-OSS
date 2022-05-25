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
package eu.cec.digit.circabc.business.helper;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.lang.NotImplementedException;


/**
 * Business manager to perform permission related common tasks.
 *
 * @author Yanick Pignot
 */
public class PermissionManager {

    private PermissionService permissionService;

    //--------------
    //-- public methods

    /**
     * Return if the current authenticated user can read the given node.
     *
     * @param nodeRef The noderef
     */
    public boolean canRead(final NodeRef nodeRef) {
        return hasPermission(nodeRef, PermissionService.READ);
    }

    /**
     * Return if the current authenticated user can delete the given node.
     *
     * @param nodeRef The noderef
     */
    public boolean canDelete(final NodeRef nodeRef) {
        return hasPermission(nodeRef, PermissionService.DELETE);
    }

    /**
     * Return if the current authenticated user can edit the content of the given node;
     *
     * @param nodeRef The noderef
     */
    public boolean canEditContent(final NodeRef nodeRef) {
        return hasPermission(nodeRef, PermissionService.WRITE_CONTENT);
    }

    /**
     * Return if the current authenticated user can edit the given node properties.
     *
     * @param nodeRef The noderef
     */
    public boolean canEditProperties(final NodeRef nodeRef) {
        return hasPermission(nodeRef, PermissionService.WRITE_PROPERTIES);
    }

    /**
     * Return if the current authenticated user can add/create a child to the given node.
     *
     * @param nodeRef The noderef
     */
    public boolean canAddChild(final NodeRef nodeRef) {
        return hasPermission(nodeRef, PermissionService.CREATE_CHILDREN);
    }


    /**
     * Return if the current authenticated in adminsitrator in all services of the interest group.
     *
     * @param nodeRef An interest group or any of its child.
     */
    public boolean isAdminInAllService(final NodeRef nodeRef) {
        throw new NotImplementedException();
    }

    /**
     * Return if the current authenticated in adminsitrator in at least one service of the interest
     * group.
     *
     * @param nodeRef An interest group or any of its child.
     */
    public boolean isAdminInOneService(final NodeRef nodeRef) {
        throw new NotImplementedException();
    }

    //--------------
    //-- private helpers

    private boolean hasPermission(final NodeRef nodeRef, final String permission) {
        if (nodeRef == null) {
            return false;
        } else {
            return AccessStatus.ALLOWED.equals(getPermissionService().hasPermission(nodeRef, permission));
        }
    }

    //--------------
    //-- IOC

    /**
     * @return the permissionService
     */
    protected final PermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public final void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
}
