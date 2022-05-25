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

import eu.cec.digit.circabc.web.ui.common.component.data.UIRichList;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class TrashcanDialogProperty implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 4291026325350001538L;
    private static final String FILTER_DATE_ALL = "all";
    private static final String FILTER_USER_ALL = "all";
    /**
     * Component reference for Deleted Items RichList control
     */
    protected UIRichList itemsRichList;
    /**
     * NodeArchiveService bean reference
     */
    transient private NodeArchiveService nodeArchiveService;
    /**
     * Search text
     */
    private String searchText = null;

    /**
     * We show an empty list until a Search or Show All is executed
     */
    private boolean showItems = false;

    private boolean fullTextSearch = false;

    /**
     * Currently listed items
     */
    private List<Node> listedItems = Collections.emptyList();

    private List<Node> successItems = Collections.emptyList();

    private List<Node> failureItems = Collections.emptyList();

    /**
     * Current action context Node
     */
    private Node actionNode;

    /**
     * Root node to the spaces store archive store
     */
    private NodeRef archiveRootRef = null;

    /**
     * Alternative destination for recovered items
     */
    private NodeRef destination = null;

    /**
     * Date filter selection
     */
    private String dateFilter = FILTER_DATE_ALL;

    /**
     * User filter selection
     */
    private String userFilter = FILTER_USER_ALL;

    /**
     * User filter search box text
     */
    private String userSearchText = null;

    private boolean inProgress = false;

    /**
     * @return Returns the itemsRichList.
     */
    public UIRichList getItemsRichList() {
        return this.itemsRichList;
    }

    /**
     * @param itemsRichList The itemsRichList to set.
     */
    public void setItemsRichList(UIRichList itemsRichList) {
        this.itemsRichList = itemsRichList;
    }

    /**
     * @return Returns the searchText.
     */
    public String getSearchText() {
        return this.searchText;
    }

    /**
     * @param searchText The searchText to set.
     */
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    /**
     * @return Returns the alternative destination to use if recovery fails.
     */
    public NodeRef getDestination() {
        return this.destination;
    }

    /**
     * @param destination The alternative destination to use if recovery fails.
     */
    public void setDestination(NodeRef destination) {
        this.destination = destination;
    }

    /**
     * @return Returns the dateFilter.
     */
    public String getDateFilter() {
        return this.dateFilter;
    }

    /**
     * @param dateFilter The dateFilter to set.
     */
    public void setDateFilter(String dateFilter) {
        this.dateFilter = dateFilter;
    }

    /**
     * @return Returns the userFilter.
     */
    public String getUserFilter() {
        return this.userFilter;
    }

    /**
     * @param userFilter The userFilter to set.
     */
    public void setUserFilter(String userFilter) {
        this.userFilter = userFilter;
    }

    /**
     * @return Returns the userSearchText.
     */
    public String getUserSearchText() {
        return this.userSearchText;
    }

    /**
     * @param userSearchText The userSearchText to set.
     */
    public void setUserSearchText(String userSearchText) {
        this.userSearchText = userSearchText;
    }

    /**
     * @return Returns the listed items.
     */
    public List<Node> getListedItems() {
        return this.listedItems;
    }

    /**
     * @param listedItems The listed items to set.
     */
    public void setListedItems(List<Node> listedItems) {
        this.listedItems = listedItems;
    }

    /**
     * @return count of the items that failed to recover
     */
    public int getFailureItemsCount() {
        return this.failureItems.size();
    }

    /**
     * @return the item context for the current action
     */
    public Node getItem() {
        return this.actionNode;
    }

    /**
     * @param node The item context for the current action
     */
    public void setItem(Node node) {
        this.actionNode = node;
    }

    public boolean isShowItems() {
        return showItems;
    }

    public void setShowItems(boolean showItems) {
        this.showItems = showItems;
    }

    public boolean isFullTextSearch() {
        return fullTextSearch;
    }

    public void setFullTextSearch(boolean fullTextSearch) {
        this.fullTextSearch = fullTextSearch;
    }

    public List<Node> getSuccessItems() {
        return successItems;
    }

    public void setSuccessItems(List<Node> successItems) {
        this.successItems = successItems;
    }

    public List<Node> getFailureItems() {
        return failureItems;
    }

    public void setFailureItems(List<Node> failureItems) {
        this.failureItems = failureItems;
    }

    public Node getActionNode() {
        return actionNode;
    }

    public void setActionNode(Node actionNode) {
        this.actionNode = actionNode;
    }

    public NodeRef getArchiveRootRef() {
        return archiveRootRef;
    }

    public void setArchiveRootRef(NodeRef archiveRootRef) {
        this.archiveRootRef = archiveRootRef;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    /**
     * @return nodeArchiveService
     */
    public NodeArchiveService getNodeArchiveService() {
        //check for null for cluster environment
        if (nodeArchiveService == null) {
            nodeArchiveService = (NodeArchiveService) FacesHelper
                    .getManagedBean(FacesContext.getCurrentInstance(), "nodeArchiveService");
        }
        return nodeArchiveService;
    }

    /**
     * @param nodeArchiveService The nodeArchiveService to set.
     */
    public void setNodeArchiveService(NodeArchiveService nodeArchiveService) {
        this.nodeArchiveService = nodeArchiveService;
    }


}
