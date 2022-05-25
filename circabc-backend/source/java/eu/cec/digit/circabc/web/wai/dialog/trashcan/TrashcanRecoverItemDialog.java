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
/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package eu.cec.digit.circabc.web.wai.dialog.trashcan;

import net.sf.acegisecurity.Authentication;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.text.MessageFormat;

public class TrashcanRecoverItemDialog extends TrashcanDialog {

    private static final long serialVersionUID = -8237457079397611071L;

    private static final String RICHLIST_ID = "trashcan-list";
    private static final String RICHLIST_MSG_ID = "trashcan" + ':' + RICHLIST_ID;
    private static final String MSG_RECOVERED_ITEM_SUCCESS = "recovered_item_success";
    private static final String MSG_RECOVERED_ITEM_INTEGRITY = "recovered_item_integrity";
    private static final String MSG_RECOVERED_ITEM_PERMISSION = "recovered_item_permission";
    private static final String MSG_RECOVERED_ITEM_PARENT = "recovered_item_parent";
    private static final String MSG_RECOVERED_ITEM_FAILURE = "recovered_item_failure";

    private static final String MSG_RECOVER_ITEM = "recover_item";
    private static final String MSG_NO = "no";
    private static final String MSG_YES = "yes";

    private String recoverItem(FacesContext context, String outcome) {
        Node item = property.getItem();
        if (item != null) {

            FacesContext fc = context;
            try {
                String msg;
                FacesMessage errorfacesMsg = null;

                // restore the node - the user may have requested a restore to a
                // different parent
                RestoreNodeReport report;

                if (property.getDestination() == null) {
                    report = property.getNodeArchiveService().restoreArchivedNode(item.getNodeRef());
                } else {
                    report = property.getNodeArchiveService()
                            .restoreArchivedNode(item.getNodeRef(), property.getDestination(), null, null);
                }
                switch (report.getStatus()) {
                    case SUCCESS:
                        msg = MessageFormat
                                .format(Application.getMessage(fc, MSG_RECOVERED_ITEM_SUCCESS), item.getName());
                        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
                        fc.addMessage(RICHLIST_MSG_ID, facesMsg);

                        break;

                    case FAILURE_INVALID_PARENT:
                        msg = MessageFormat
                                .format(Application.getMessage(fc, MSG_RECOVERED_ITEM_PARENT), item.getName());
                        errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                        break;

                    case FAILURE_PERMISSION:
                        msg = MessageFormat
                                .format(Application.getMessage(fc, MSG_RECOVERED_ITEM_PERMISSION), item.getName());
                        errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                        break;

                    case FAILURE_INTEGRITY:
                        msg = MessageFormat
                                .format(Application.getMessage(fc, MSG_RECOVERED_ITEM_INTEGRITY), item.getName());
                        errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                        break;

                    default:
                        String reason = report.getCause().getMessage();
                        msg = MessageFormat
                                .format(Application.getMessage(fc, MSG_RECOVERED_ITEM_FAILURE), item.getName(),
                                        reason);
                        errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                        break;
                }

                // report the failure if one occured we stay on the current
                // screen
                if (errorfacesMsg != null) {
                    fc.addMessage(null, errorfacesMsg);
                }
            } catch (Throwable err) {
                // most exceptions will be caught and returned as
                // RestoreNodeReport objects by the service
                String reason = err.getMessage();
                String msg = MessageFormat
                        .format(Application.getMessage(fc, MSG_RECOVERED_ITEM_FAILURE), item.getName(), reason);
                FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                fc.addMessage(null, facesMsg);
            }
        }

        return "wai:dialog:close";
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        Authentication originalFullAuthentication = AuthenticationUtil.getFullAuthentication();
        String userName = AuthenticationUtil.getRunAsUser();

        try {
            AuthenticationUtil.setRunAsUserSystem();
            return recoverItem(context, outcome);
        } finally {
            if (originalFullAuthentication == null) {
                AuthenticationUtil.clearCurrentSecurityContext();
            } else {
                AuthenticationUtil.setFullAuthentication(originalFullAuthentication);
                AuthenticationUtil.setRunAsUser(userName);
            }
        }


    }

    @Override
    public String getContainerTitle() {
        Authentication originalFullAuthentication = AuthenticationUtil.getFullAuthentication();
        String userName = AuthenticationUtil.getRunAsUser();
        try {
            AuthenticationUtil.setRunAsUserSystem();
            return Application.getMessage(FacesContext.getCurrentInstance(), MSG_RECOVER_ITEM) + " '"
                    + property.getItem().getName() + "'";
        } finally {
            if (originalFullAuthentication == null) {
                AuthenticationUtil.clearCurrentSecurityContext();
            } else {
                AuthenticationUtil.setFullAuthentication(originalFullAuthentication);
                AuthenticationUtil.setRunAsUser(userName);
            }
        }
    }

    @Override
    public String getCancelButtonLabel() {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_NO);
    }

    @Override
    public boolean getFinishButtonDisabled() {
        return false;
    }

    @Override
    public String getFinishButtonLabel() {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_YES);
    }

}
