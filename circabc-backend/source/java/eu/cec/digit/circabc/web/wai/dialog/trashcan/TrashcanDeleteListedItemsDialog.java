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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import net.sf.acegisecurity.Authentication;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

public class TrashcanDeleteListedItemsDialog extends TrashcanDialog {

  /**
   *
   */
  private static final long serialVersionUID = 249489779589432007L;
  private static final String MSG_YES = "yes";
  private static final String MSG_NO = "no";

  private String deleteListedItems(FacesContext context, String outcome) {
    if (property.isInProgress()) {
      return null;
    }

    property.setInProgress(true);

    try {
      List<NodeRef> nodeRefs = new ArrayList<>(
        property.getListedItems().size()
      );
      for (Node node : property.getListedItems()) {
        nodeRefs.add(node.getNodeRef());
      }
      property.getNodeArchiveService().purgeArchivedNodes(nodeRefs);
    } catch (Throwable err) {
      Utils.addErrorMessage(
        MessageFormat.format(
          Application.getMessage(context, Repository.ERROR_GENERIC),
          err.getMessage()
        ),
        err
      );
    } finally {
      property.setInProgress(false);
    }

    return outcome;
  }

  @Override
  protected String finishImpl(FacesContext context, String outcome)
    throws Exception {
    Authentication originalFullAuthentication =
      AuthenticationUtil.getFullAuthentication();
    String userName = AuthenticationUtil.getRunAsUser();
    try {
      AuthenticationUtil.setRunAsUserSystem();
      return deleteListedItems(context, outcome);
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
