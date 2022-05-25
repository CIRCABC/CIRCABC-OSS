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
package eu.cec.digit.circabc.web.wai.dialog.ml;

import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.common.VersionLabelComparator;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.EditionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.content.DocumentDetailsDialog;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;

/**
 * @author patrice.coppens@trasys.lu
 * <p>
 * 14-sept.-07 - 14:19:54
 */
public class MLContainerHistoryDialog extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "CircabcMLContainerHistoryDialog";
    private static final long serialVersionUID = -7424360183369853548L;

    //private static final Log logger = LogFactory.getLog(MLContainerHistoryDialog.class);
    private String name;

    private boolean isLastVersion = false;

    private NodeRef parentNodeRef;

    private List<MapNode> translations;

    private Version currentEdition;

    private transient EditionService editionService;
    private transient VersionService versionService;
    private transient ContentFilterLanguagesService contentFilterLanguagesService;


    public MLContainerHistoryDialog() {
        super();
    }

    /**
     * action event to see versionned mlContainer
     */
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        String label = parameters.get("label");

        final DocumentDetailsDialog docDetails = (DocumentDetailsDialog) Beans
                .getDocumentDetailsDialog();

        parentNodeRef = docDetails.getDocument().getNodeRef();

        // init mlcontainer
        NodeRef mlRef = docDetails.getDocumentMlContainer().getNodeRef();

        this.name = (String) getNodeService().getProperty(mlRef, ContentModel.PROP_NAME);

        this.currentEdition = getEditionService().getEditions(mlRef).getVersion(label);

        isLastVersion = getVersionService().getCurrentVersion(mlRef).getVersionLabel().equals(label);

        initTranslations();
    }


    /**
     * Returns the ml container of the document this bean is currently representing.
     * <p>
     * same result as getNode()
     *
     * @return The document multilingual container NodeRef
     */
    public Node getDocumentMlContainer() {
        return isLastVersion ?
                new Node(currentEdition.getVersionedNodeRef()) :
                new Node(currentEdition.getFrozenStateNodeRef());
    }


    /**
     * Returns the name this bean is currently representing
     *
     * @return The name
     */
    public String getName() {
        return name;
    }


    /**
     * @see org.alfresco.web.bean.BaseDetailsBean#getPropertiesPanelId()
     */
    protected String getPropertiesPanelId() {
        return "document-props";
    }

    /**
     * Returns a model for use by a template on the Document Details page.
     *
     * @return model containing current document and current space info.
     */
    public Map getTemplateModel() {
        throw new RuntimeException("not implemented");
    }


    /**
     * Resolve the actual document Node from any Link object that may be proxying it
     *
     * @return current document Node or document Node resolved from any Link object
     */
    protected Node getLinkResolvedNode() {
        throw new RuntimeException("not implemented");
    }


    /**
     * Returns a list of objects representing the translations of the current document
     *
     * @return List of translations
     */
    public List getTranslations() {
        if (translations == null) {
            throw new RuntimeException(
                    "translation cannot be null if setupMLContainerhistoryAction is call");
        }
        return translations;
    }


    /**
     * build a list of objects representing the translations of the current document .
     */

    @SuppressWarnings("unchecked")
    private void initTranslations() {
        translations = new ArrayList<>();

        if (isLastVersion) {
            for (final NodeRef tr : getMultilingualContentService()
                    .getTranslations(this.currentEdition.getVersionedNodeRef()).values()) {
                // get the properties of the current translations
                addClientNodeProperties(translations, tr, getNodeService().getProperties(tr));
            }

        } else {

            for (final VersionHistory versionHistory : getEditionService()
                    .getVersionedTranslations(this.currentEdition)) {
                //   get the list of versions and sort them ascending according their version label
                List<Version> orderedVersions = new ArrayList<>(versionHistory.getAllVersions());

                Collections.sort(orderedVersions, new VersionLabelComparator());

                // the last version is the first version of the list
                Version lastVersion = orderedVersions.get(0);

                // get the properties of the lastVersion
                addClientNodeProperties(translations, lastVersion.getFrozenStateNodeRef(),
                        getEditionService().getVersionedMetadatas(lastVersion));
            }
        }
    }

    private void addClientNodeProperties(List<MapNode> clientNodes, NodeRef nodeRef,
                                         Map<QName, Serializable> properties) {
        Locale language = (Locale) properties.get(ContentModel.PROP_LOCALE);

        // create a map node representation of the last version
        MapNode clientLastVersion = new MapNode(nodeRef);

        clientLastVersion.put("versionName", properties.get(ContentModel.PROP_NAME));
        // use the node service for the description to ensure that the returned value is a text and not a MLText
        clientLastVersion.put("versionDescription",
                getNodeService().getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
        clientLastVersion.put("versionAuthor", properties.get(ContentModel.PROP_AUTHOR));
        clientLastVersion.put("versionCreatedDate", properties.get(ContentModel.PROP_CREATED));
        clientLastVersion.put("versionModifiedDate", properties.get(ContentModel.PROP_MODIFIED));
        clientLastVersion.put("versionLanguage",
                getContentFilterLanguagesService().convertToNewISOCode(language.getLanguage())
                        .toUpperCase());

        if (getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION)) {
            clientLastVersion.put("versionUrl", null);
        } else {
            clientLastVersion.put("versionUrl",
                    DownloadContentServlet.generateBrowserURL(nodeRef, clientLastVersion.getName()));
        }

        // add a translation of the editionBean
        clientNodes.add(clientLastVersion);
    }


    /**
     * @return the parentNodeRef
     */
    public NodeRef getParentNodeRef() {
        return parentNodeRef;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME;
    }

    @Override
    public boolean isCancelButtonVisible() {
        return false;
    }

    @Override
    public String getContainerTitle() {
        return translate("manage_multilingual_details_title", getName());
    }

    public String getBrowserTitle() {
        return translate("title_manage_multilingual_details");
    }

    public String getPageIconAltText() {
        return "manage_multilingual_details_icon_tooltip";
    }


    /**
     * @return the contentFilterLanguagesService
     */
    protected final ContentFilterLanguagesService getContentFilterLanguagesService() {
        if (contentFilterLanguagesService == null) {
            contentFilterLanguagesService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getContentFilterLanguagesService();
        }
        return contentFilterLanguagesService;
    }

    /**
     * @param contentFilterLanguagesService the contentFilterLanguagesService to set
     */
    public final void setContentFilterLanguagesService(
            ContentFilterLanguagesService contentFilterLanguagesService) {
        this.contentFilterLanguagesService = contentFilterLanguagesService;
    }

    /**
     * @return the editionService
     */
    protected final EditionService getEditionService() {
        if (editionService == null) {
            editionService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getEditionService();
        }
        return editionService;
    }

    /**
     * @param editionService the editionService to set
     */
    public final void setEditionService(EditionService editionService) {
        this.editionService = editionService;
    }


    /**
     * @return the versionService
     */
    protected final VersionService getVersionService() {
        if (versionService == null) {
            versionService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getVersionService();
        }
        return versionService;
    }

    /**
     * @param versionService the versionService to set
     */
    public final void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

}
