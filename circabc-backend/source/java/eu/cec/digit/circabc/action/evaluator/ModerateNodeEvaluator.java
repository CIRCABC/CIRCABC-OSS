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
package eu.cec.digit.circabc.action.evaluator;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;

/**
 * Evaluates whether the current user can approve/reject the given node in the moderation process
 *
 * @author yanick pignot
 */
public class ModerateNodeEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = 6112283424092389996L;

    public boolean evaluate(final Node node) {
        if (node.hasAspect(ModerationModel.ASPECT_WAITING_APPROVAL)) {
            if (node.hasAspect(CircabcModel.ASPECT_LIBRARY)) {
                return node.hasPermission(LibraryPermissions.LIBADMIN.toString()) && isOwner(node) == false;
            } else {
                return node.hasPermission(NewsGroupPermissions.NWSMODERATE.toString())
                        && isOwner(node) == false;

            }
        } else {
            return false;
        }
    }

    /**
     * @param node
     * @throws AuthenticationException
     */
    private boolean isOwner(final Node node) throws AuthenticationException {
        final FacesContext fc = FacesContext.getCurrentInstance();
        final ServiceRegistry registry = Services.getAlfrescoServiceRegistry(fc);
        final OwnableService ownableService = registry.getOwnableService();

        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        final String owner = ownableService.getOwner(node.getNodeRef());

        return owner != null && owner.equals(currentUser);
    }

}
