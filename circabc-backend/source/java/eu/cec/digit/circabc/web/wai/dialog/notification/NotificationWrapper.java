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

import eu.cec.digit.circabc.service.notification.NotificationStatus;
import io.swagger.model.I18nProperty;
import org.alfresco.service.cmr.security.AuthorityType;

import java.io.Serializable;

/**
 * Light weight object that represents a dispalyable Notification element for the UI
 *
 * @author Yanick Pignot
 */
public class NotificationWrapper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8112745596456613819L;


    private String username;
    private NotificationStatus status;
    private AuthorityType type;
    private String authority;
    private String nodeId;
    private boolean isInherited;
    private I18nProperty title;


    /**
     * @param type
     * @param username
     * @param status
     * @param autority
     */
    public NotificationWrapper(final AuthorityType type, final String username,
                               final NotificationStatus status, final String authority, final String nodeId,
                               final boolean isInherited) {
        super();
        this.type = type;
        this.username = username;
        this.status = status;
        this.authority = authority;
        this.nodeId = nodeId;
        this.isInherited = isInherited;
    }

    /**
     * @param type
     * @param username
     * @param status
     * @param autority
     */
    public NotificationWrapper(final AuthorityType type, final String username,
                               final NotificationStatus status, final String authority, final String nodeId,
                               final boolean isInherited, final I18nProperty title) {
        super();
        this.type = type;
        this.username = username;
        this.status = status;
        this.authority = authority;
        this.nodeId = nodeId;
        this.isInherited = isInherited;
        this.title = title;
    }

    /**
     * @return the title
     */
    public I18nProperty getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(I18nProperty title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return this.authority + " ( " + this.username + " ) " + this.status;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificationWrapper)) {
            return false;
        }
        final NotificationWrapper other = (NotificationWrapper) o;
        return this.getAuthority().equals(other.getAuthority())
                && this.getStatus().equals(other.getStatus());
    }

    @Override
    public int hashCode() {
        return authority.hashCode() + nodeId.hashCode() + status.hashCode();
    }

    /**
     * @return the autority
     */
    public final String getAuthority() {
        return authority;
    }

    /**
     * @return the user friendly status as String
     */
    public final String getStatus() {
        return NotificationUtils.translateStatus(status);
    }

    public String getStatusName() {
        return status.name();
    }

    /**
     * @return the user friendly tye As string
     */
    public final String getType() {
        return NotificationUtils.translateAuthorityType(type);
    }

    public String gettypeName() {
        return type.name();
    }

    /**
     * @return the username
     */
    public final String getUsername() {
        return username;
    }

    /**
     * @return the nodeId
     */
    public final String getNodeId() {
        return nodeId;
    }

    /**
     * @return the status
     */
    public final NotificationStatus getStatusValue() {
        return status;
    }

    /**
     * @return the type
     */
    public final AuthorityType getTypeValue() {
        return type;
    }

    /**
     * @return the status as an object string
     */
    public final String getStatusValueToString() {
        return status.toString();
    }

    /**
     * @return the type as an object string
     */
    public final String getTypeValueToString() {
        return type.toString();
    }

    public boolean getInherited() {
        return isInherited;
    }

    public void setInherited(boolean isInherited) {
        this.isInherited = isInherited;
    }

    public String getInheritedString() {
        return String.valueOf(isInherited);
    }

    public void setInheritedString(String isInheritedString) {
        this.isInherited = Boolean.valueOf(isInheritedString);
    }

}
