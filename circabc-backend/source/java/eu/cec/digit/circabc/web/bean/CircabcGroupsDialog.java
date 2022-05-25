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
package eu.cec.digit.circabc.web.bean;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.web.bean.groups.GroupsDialog;

import java.util.*;

/**
 * Gets all root groups in addition to Alresco's normal get groups behaviour.
 *
 * @author schwerr
 */
public class CircabcGroupsDialog extends GroupsDialog {

    private static final long serialVersionUID = 8400018839652467976L;
    private static final ThreadLocal<Boolean> rootGroups = new ThreadLocal<Boolean>() {
        /**
         * @see java.lang.ThreadLocal#initialValue()
         */
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    transient private AuthorityService authorityService = null;

    public CircabcGroupsDialog() {
        super();
        rootGroups.set(false);
    }

    /**
     * Action handler to show all the root groups
     *
     * @return The outcome
     */
    public String showRootGroups() {

        group = null;
        groupsRichList.setValue(null);

        Set<String> results = authorityService.getAllRootAuthoritiesInZone(
                AuthorityService.ZONE_APP_DEFAULT, AuthorityType.GROUP);

        List<String> authorities = new ArrayList<>(results);

        groups = new ArrayList<>(authorities.size());

        for (String authority : authorities) {

            Map<String, String> authMap = new HashMap<>(8);

            String name = authorityService.getAuthorityDisplayName(authority);
            if (name == null) {
                name = authorityService.getShortName(authority);
            }
            authMap.put("name", name);
            authMap.put("id", authority);
            authMap.put("group", authority);
            authMap.put("groupName", name);

            groups.add(authMap);
        }

        rootGroups.set(true);

        return null;
    }

    /**
     * @return The list of group objects to display. Returns the list of root groups if set.
     */
    public List<Map<String, String>> getGroups() {

        if (!rootGroups.get()) {
            return super.getGroups();
        }

        rootGroups.set(false);

        return groups;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public void setAuthService(AuthorityService authorityService) {
        this.authorityService = authorityService;
        super.setAuthService(authorityService);
    }
}
