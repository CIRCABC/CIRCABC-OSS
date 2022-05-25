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
package eu.cec.digit.circabc.web.wai.dialog.notification;

import eu.cec.digit.circabc.service.notification.NotificationManagerService;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

import javax.faces.context.FacesContext;
import java.util.Map;

public class NotificationPasteDialog extends BaseWaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = -2922334756583014210L;
    private NotificationManagerService notificationManagerService;
    private boolean notifyPasteAll;
    private boolean notifyPaste;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setNotifyPasteAll(getNotificationManagerService()
                .isPasteAllNotificationEnabled(getActionNode().getNodeRef()));
        setNotifyPaste(
                getNotificationManagerService().isPasteNotificationEnabled(getActionNode().getNodeRef()));
    }


    public String getPageIconAltText() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getBrowserTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        getNotificationManagerService()
                .setPasteAllNotificationEnabled(getActionNode().getNodeRef(), isNotifyPasteAll());
        getNotificationManagerService()
                .setPasteNotificationEnabled(getActionNode().getNodeRef(), isNotifyPaste());
        return outcome;
    }

    public NotificationManagerService getNotificationManagerService() {
        return notificationManagerService;
    }

    public void setNotificationManagerService(NotificationManagerService notificationManagerService) {
        this.notificationManagerService = notificationManagerService;
    }

    public boolean isNotifyPasteAll() {
        return notifyPasteAll;
    }

    public void setNotifyPasteAll(boolean notifyPasteAll) {
        this.notifyPasteAll = notifyPasteAll;
    }

    public boolean isNotifyPaste() {
        return notifyPaste;
    }

    public void setNotifyPaste(boolean notifyPaste) {
        this.notifyPaste = notifyPaste;
    }


}
