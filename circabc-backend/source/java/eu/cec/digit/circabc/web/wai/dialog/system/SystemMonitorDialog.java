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
package eu.cec.digit.circabc.web.wai.dialog.system;

import eu.cec.digit.circabc.action.config.ClipboardConfig;
import eu.cec.digit.circabc.action.config.ImportConfig;
import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.web.servlet.UploadFileServletConfig;
import eu.cec.digit.circabc.web.ui.repo.component.shelf.UIClipboardShelfItem;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.repo.security.authentication.TicketComponent;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class SystemMonitorDialog extends BaseWaiDialog {

    private static final long serialVersionUID = 6158073330963347674L;

    private UploadFileServletConfig config;
    private ImportConfig importConfig;
    private ClipboardConfig clipboardConfig;

// Migration 3.1 -> 3.4.6 - 20/12/2011 - AuthenticationServiceImpl changed because of the authentication subsystems in Alfresco 3.4.6
// The accessed methods belong to the TicketComponent, so this was replaced instead.
// This code suggests me that before a custom ticket component could have been implemented.
//		private AuthenticationService authenticationService;
//
//		public AuthenticationService getAuthenticationService2() {
//			return authenticationService;
//		}
//
//		public void setAuthenticationService(
//				AuthenticationService authenticationService) {
//			this.authenticationService = authenticationService;
//		}

    private TicketComponent ticketComponent = null;

    //***********************************************************************
    //                                                              OVERRIDES
    //***********************************************************************

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {
        return null;
    }

    @Override
    public String getFinishButtonLabel() {
        return this.translate("system_monitor_refresh");
    }

    @Override
    public String getCancelButtonLabel() {
        return this.translate("system_monitor_close");
    }

    public String getPageIconAltText() {
        return null;
    }

    public String getBrowserTitle() {
        return null;
    }

    //***********************************************************************
    //                                                         PRIVATE HELPER
    //***********************************************************************

    //***********************************************************************
    //                                                      GETTER AND SETTER
    //***********************************************************************

    /**
     * @param ticketComponent the ticketComponent to set
     */
    public void setTicketComponent(TicketComponent ticketComponent) {
        this.ticketComponent = ticketComponent;
    }

    public ArrayList<UserDetails> getUsers() {
//		Set<String> userNames = this.getAuthenticationService().getUsersWithTickets(true);
        Set<String> userNames = ticketComponent.getUsersWithTickets(true);
        ArrayList<UserDetails> users = new ArrayList<>();
        for (String userName : userNames) {
            if (!userName.equals("guest")) {
                UserDetails userDets = getBusinessRegistry().getUserDetailsBusinessSrv()
                        .getUserDetails(userName);
                users.add(userDets);
            }
        }
        return users;
    }

    public int getUserCount() {
//		Set<String> userNames = this.getAuthenticationService().getUsersWithTickets(true);
        Set<String> userNames = ticketComponent.getUsersWithTickets(true);
        return userNames.size() - 1;
    }

    public int getTicketCount() {
//		return this.getAuthenticationService().countTickets(true);
        return ticketComponent.countTickets(true);
    }

    public void setConfig(UploadFileServletConfig config) {
        this.config = config;
    }

    public int getMaxFileSize() {
        return this.config.getMaxSizeInMegaBytes();
    }

    public void setMaxFileSize(int value) {
        this.config.setMaxSizeInMegaBytes(value);
    }


    public int getImportMaxFileSize() {
        return this.importConfig.getMaxSizeInMegaBytes();
    }

    public void setImportMaxFileSize(int value) {
        this.importConfig.setMaxSizeInMegaBytes(value);
    }

    public ImportConfig getImportConfig() {
        return importConfig;
    }

    public void setImportConfig(ImportConfig importConfig) {
        this.importConfig = importConfig;
    }

    /**
     * Gets the value of the clipboardConfig
     *
     * @return the clipboardConfig
     */
    public ClipboardConfig getClipboardConfig() {
        return clipboardConfig;
    }

    /**
     * Sets the value of the clipboardConfig
     *
     * @param clipboardConfig the clipboardConfig to set.
     */
    public void setClipboardConfig(ClipboardConfig clipboardConfig) {
        this.clipboardConfig = clipboardConfig;
    }

    /**
     * Gets the value of the downloadLimitMB
     *
     * @return the downloadLimitMB
     */
    public long getDownloadLimitMB() {
        return clipboardConfig.getDownloadLimitMB();
    }

    /**
     * Sets the value of the downloadLimitMB
     *
     * @param downloadLimitMB the downloadLimitMB to set.
     */
    public void setDownloadLimitMB(long downloadLimitMB) {
        clipboardConfig.setDownloadLimitMB(downloadLimitMB);
        UIClipboardShelfItem.setDownloadLimitMB(clipboardConfig.getDownloadLimitMB());
    }
}
