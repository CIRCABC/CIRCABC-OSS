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
package eu.cec.digit.circabc.web.wai.dialog.sharespace;

import eu.cec.digit.circabc.business.api.link.InterestGroupItem;
import eu.cec.digit.circabc.web.PermissionUtils;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;

/**
 * Web side wrapper for Applicant object encapsulation
 *
 * @author Yanick Pignot
 * <p>
 * TODO since InterestGroupItem object is in the business layer, move this logic in the business
 * layer. But we have first refractor PermissionUtils.
 */
public class WebInterestgroupItem implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2395746635023897818L;
    final InterestGroupItem interestGroupItem;
    final String igTitle;
    final String permissionTitle;

    /**
     * @param interestGroupItem
     */
    /*package*/ WebInterestgroupItem(final InterestGroupItem item, final String igTitle) {
        super();
        this.interestGroupItem = item;
        this.igTitle = igTitle;
        this.permissionTitle = PermissionUtils.getPermissionLabel(item.getPermission());
    }

    /**
     * @return the igTitle
     */
    public final String getIgTitle() {
        return igTitle;
    }

    /**
     * @return the permissionTitle
     */
    public final String getPermissionTitle() {
        return permissionTitle;
    }

    /**
     * @see eu.cec.digit.circabc.service.sharespace.InterestGroupItem#getId()
     */
    public NodeRef getId() {
        return interestGroupItem.getNodeRef();
    }

    /**
     * @see eu.cec.digit.circabc.service.sharespace.InterestGroupItem#getName()
     */
    public String getName() {
        return interestGroupItem.getName();
    }

    /**
     * @see eu.cec.digit.circabc.service.sharespace.InterestGroupItem#getPermission()
     */
    public String getPermission() {
        return interestGroupItem.getPermission();
    }

}
