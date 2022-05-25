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
package eu.cec.digit.circabc.web.wai.bean.navigation.library;

import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.common.VersionLabelComparator;
import org.alfresco.service.cmr.ml.EditionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.*;

/**
 * Bean that backs the navigation inside the multilingual content details in the Library Service
 *
 * @author yanick pignot
 */
public class MLContentDetailsBean extends ContentDetailsBean implements WaiNavigator {

    public static final String BEAN_NAME = "LibMLContentDetailsBean";
    public static final String JSP_NAME = "ml-content-details.jsp";
    public static final String MSG_PAGE_TITLE = "manage_multilingual_details_title";
    public static final String MSG_PAGE_DESCRIPTION = "manage_multilingual_details_title_desc";
    public static final String MSG_PAGE_ICON_ALT = "manage_multilingual_details_icon_tooltip";
    public static final String MSG_BROWSER_TITLE = "title_manage_multilingual_details";
    protected static final String MSG_SUCCESS_OWNERSHIP_ML_CONTAINER = "success_ownership";
    /**
     *
     */
    private static final long serialVersionUID = -1967164575499663894L;
    /**
     * Transient lists of editions nodes for display
     */
    protected List<MapNode> editionNodes = null;
    private transient EditionService editionService;
    private transient OwnableService ownableService;

    @Override
    public String getPageIconAltText() {
        return translate(MSG_PAGE_ICON_ALT);
    }

    @Override
    public String getBrowserTitle() {
        return translate(MSG_BROWSER_TITLE);
    }

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        editionNodes = null;
    }

    @Override
    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.LIBRARY_ML_CONTENT;
    }

    @Override
    public String getPageTitle() {
        return translate(MSG_PAGE_TITLE, getCurrentNode().getName());
    }

    @Override
    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION, getCurrentNode().getName());
    }

    @Override
    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + "library/" + JSP_NAME;
    }

    @Override
    public String getWebdavUrl() {
        // not relevant for ml container
        return null;
    }

    /**
     * Get the list of editions
     *
     * @return List of MapNode editions
     */
    public List<MapNode> getListOfEditionForEditionHistory() {
        if (this.editionNodes == null) {
            // The list has not been build
            buildListofEditionforEditionHistory();
        }

        return this.editionNodes;
    }

    /**
     * Fill the list of editions
     */
    @SuppressWarnings("unchecked")
    protected void buildListofEditionforEditionHistory() {
        // Initialisation
        editionNodes = new ArrayList<>();

        // Get the mlContainer
        NodeRef mlContainer = getDocumentMlContainer().getNodeRef();

        // Get all editions and sort them ascending according their version label
        List<Version> orderedEditionList = new ArrayList<>(
                getEditionService().getEditions(mlContainer).getAllVersions());
        Collections.sort(orderedEditionList, new VersionLabelComparator());

        // For each edition, create the MapNode
        for (Version edition : orderedEditionList) {
            MapNode clientEdition = new MapNode(edition.getFrozenStateNodeRef());

            // Force get of properties
            clientEdition.put("editionLabel", edition.getVersionLabel());
            clientEdition.put("editionNotes", edition.getDescription());
            clientEdition.put("editionAuthor", edition.getFrozenModifier());
            clientEdition.put("editionDate", edition.getFrozenModifiedDate());
            editionNodes.add(clientEdition);
        }
    }

    /**
     * Action Handler to take Ownership of the current ml container
     */
    public void takeOwnershipMLContainer(final ActionEvent event) {
        NodeRef mlContainer = null;

        if (getCurrentNode().getType().equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
            mlContainer = getCurrentNode().getNodeRef();
        } else if (getCurrentNode().hasAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
            mlContainer = getMultilingualContentService()
                    .getTranslationContainer(getCurrentNode().getNodeRef());
        } else {
            throw new IllegalArgumentException("This method must be called with a multilingual document");
        }

        final FacesContext fc = FacesContext.getCurrentInstance();
        final NodeRef finalMLContainer = mlContainer;

        // get the translations
        final Collection<NodeRef> translations = getMultilingualContentService()
                .getTranslations(mlContainer).values();

        try {
            RetryingTransactionHelper txnHelper = Repository
                    .getRetryingTransactionHelper(FacesContext.getCurrentInstance());
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {

                public Object execute() throws Throwable {
                    // take ownership on the mlConatier
                    getOwnableService().takeOwnership(finalMLContainer);
                    // and for all translations
                    for (final NodeRef ref : translations) {
                        getOwnableService().takeOwnership(ref);

                        getCurrentNode().reset();
                    }

                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                            Application.getBundle(fc).getString(MSG_SUCCESS_OWNERSHIP_ML_CONTAINER));

                    return null;
                }

            };
            txnHelper.doInTransaction(callback);
        } catch (Throwable e) {
            Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, e.getMessage()), e);
        }
    }

    /**
     * @return the editionService
     */
    protected EditionService getEditionService() {
        if (this.editionService == null) {
            this.editionService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getEditionService();
        }
        return editionService;
    }

    /**
     * @param editionService the editionService to set
     */
    public void setEditionService(EditionService editionService) {
        this.editionService = editionService;
    }

    /**
     * @return the ownableService
     */
    public OwnableService getOwnableService() {
        if (ownableService == null) {
            ownableService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getOwnableService();
        }
        return ownableService;
    }

    /**
     * @param ownableService the ownableService to set
     */
    public void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }

}
