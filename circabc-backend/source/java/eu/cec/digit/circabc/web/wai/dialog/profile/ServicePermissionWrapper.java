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
package eu.cec.digit.circabc.web.wai.dialog.profile;

import eu.cec.digit.circabc.service.profile.CircabcServices;
import eu.cec.digit.circabc.web.PermissionUtils;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.SortableSelectItem;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yanick Pignot
 */
public class ServicePermissionWrapper {

    private static final String SERVICE_NAME_SUFFIX = "_menu";
    private final CircabcServices service;
    private final List<SortableSelectItem> permissions;
    private final boolean locked;
    private String permissionValue;

    /*package*/ ServicePermissionWrapper(final CircabcServices service, final String permission,
                                         final Enum[] permissions) {
        this(service, permission, permissions, false);
    }

    /*package*/ ServicePermissionWrapper(final CircabcServices service, final String permission,
                                         final Enum[] permissions, final boolean locked) {
        this.permissionValue = permission;
        this.service = service;
        this.locked = locked;
        this.permissions = new ArrayList<>(permissions.length);

        for (final Enum permEnum : permissions) {
            final String perm = permEnum.toString();

            this.permissions.add(
                    new SortableSelectItem(perm,
                            PermissionUtils.getPermissionLabel(perm),
                            PermissionUtils.getPermissionTooltip(perm)));
        }
    }

    /**
     * @return the permissions
     */
    public final List<SortableSelectItem> getPermissions() {
        return permissions;
    }

    /**
     * @return the permissionValue
     */
    public final String getPermissionValue() {
        return permissionValue;
    }

    /**
     * @param permissionValue the permissionValue to set
     */
    public final void setPermissionValue(String permissionValue) {
        this.permissionValue = permissionValue;
    }

    /**
     * @return the service
     */
    public final CircabcServices getService() {
        return service;
    }

    /**
     * @return the service
     */
    public final String getServiceLabel() {
        return Application.getMessage(FacesContext.getCurrentInstance(),
                service.toString().toLowerCase() + SERVICE_NAME_SUFFIX);
    }

    /**
     * @return the locked
     */
    public final boolean isLocked() {
        return locked;
    }


}
