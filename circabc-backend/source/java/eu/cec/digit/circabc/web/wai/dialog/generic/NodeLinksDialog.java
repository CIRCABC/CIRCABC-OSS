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
package eu.cec.digit.circabc.web.wai.dialog.generic;

import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.wai.bean.navigation.ResolverHelper;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import java.text.MessageFormat;
import java.util.Map;

/**
 * Bean to handle the display of the different links available for the current node.
 *
 * @author yanick pignot
 */
public class NodeLinksDialog extends BaseWaiDialog {

    private static final String LINK_PATTERN = "<a href=\"{0}{1}\" target=\"{2}\" title=\"{3}\">{4}{5}</a>";
    private static final String IMAGE_PATTERN = "<img src=\"{0}{1}\" />&nbsp;";

    private static final String DIRECTORY_ICONS = "/images/icons/";

    private static final long serialVersionUID = 5511047111192880111L;

    private static final String URL_TYPE_RELATIVE = "relative";
    private static final String URL_TARGET_THIS = "_self";

    private boolean isContent = false;
    private boolean isLink = false;

    private String reference;
    private String path;
    private String browseUrl;
    private String downloadUrl;

    private String urlType;
    private String urlMode;
    private String urlIcon;
    private String urlText;
    private String urlTitle;
    private String urlTarget;

    private String circabcLogo;
    private String igLogo;

    private String rootUrl;
    private String contextPath;


    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        final MapNode node = (MapNode) getActionNode();
        if (node == null) {
            throw new NullPointerException("The node id is a mandatory parameter");
        }

        if (parameters != null) {
            // run as system user to ensure the current user can get the name of the current node.
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
                public Object doWork() {
                    final QName type = node.getType();
                    isContent =
                            ContentModel.TYPE_CONTENT.equals(type) || ApplicationModel.TYPE_FILELINK.equals(type)
                                    || getDictionaryService().isSubClass(type, ContentModel.TYPE_CONTENT);
                    isLink = ApplicationModel.TYPE_FOLDERLINK.equals(type) || ApplicationModel.TYPE_FILELINK
                            .equals(type);
                    rootUrl = WebClientHelper.getCircabcUrl();
                    contextPath = FacesContext.getCurrentInstance().getExternalContext()
                            .getRequestContextPath();

                    if (contextPath.equals("/")) {
                        contextPath = "";
                    }

                    final NodeRef rootLogoRef = getMailPreferencesService()
                            .getDisclamerLogo(getNavigator().getCircabcHomeNode().getNodeRef());
                    final NodeRef igLogoRef = Beans.getInterestGroupLogoBean().getLogoRefrence();
                    final String description = (String) node.get("cm:description");
                    final String iconUrl;
                    final String smallIconUrl;

                    if (isContent) {
                        iconUrl = (String) getBrowseBean().resolverFileType32.get(node);
                        smallIconUrl = (String) getBrowseBean().resolverFileType16.get(node);

                        if (isLink) {
                            downloadUrl = (String) getBrowseBean().resolverLinkDownload.get(node);
                        } else {
                            downloadUrl = (String) getBrowseBean().resolverDownload.get(node);
                        }

                    } else {
                        iconUrl = DIRECTORY_ICONS + getBrowseBean().resolverSpaceIcon.get(node) + ".gif";
                        smallIconUrl = DIRECTORY_ICONS + getBrowseBean().resolverSmallIcon.get(node) + ".gif";

                        downloadUrl = "N/A";
                    }

                    if (rootLogoRef != null) {
                        circabcLogo = (String) getBrowseBean().resolverDownload.get(new Node(rootLogoRef));
                    } else {
                        circabcLogo = "_";
                    }

                    if (igLogoRef != null) {
                        igLogo = (String) getBrowseBean().resolverDownload.get(new Node(igLogoRef));
                    } else {
                        igLogo = "_";
                    }

                    reference = node.getNodeRefAsString();
                    browseUrl = WebClientHelper
                            .getGeneratedWaiRelativeUrl(node, ExtendedURLMode.HTTP_WAI_BROWSE);

                    urlIcon = "_";
                    urlType = URL_TYPE_RELATIVE;
                    urlMode = browseUrl;
                    urlText = node.getName();
                    urlTitle = getBestTitle(node);
                    urlTarget = URL_TARGET_THIS;

                    try {
                        path = getManagementService().getNodePath(node.getNodeRef()).toString();
                    } catch (Exception e) {
                        path = "error retrying path...";
                    }

                    node.put("path", path);
                    node.put("bestTitle", urlTitle);
                    node.put("iconUrl", iconUrl);
                    node.put("smallIconUrl", smallIconUrl);
                    node.put("description",
                            description == null || description.length() == 0 ? "&nbsp;" : description);
                    node.put(ResolverHelper.DOWNLOAD_URL, downloadUrl);

                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        // nothing to do
        return outcome;
    }

    public String getGeneratedUrl() {
        final String imageElem;
        final String linkPrefix;

        // relative url or not?
        if (URL_TYPE_RELATIVE.equals(this.urlType)) {
            linkPrefix = contextPath;
        } else {
            linkPrefix = rootUrl;
        }

        // use an image or not?
        if (this.urlIcon != null && this.urlIcon.equals("_") == false) {
            imageElem = MessageFormat.format(IMAGE_PATTERN, linkPrefix, this.urlIcon);
        } else {
            imageElem = "";
        }

        if (this.urlText.equals("_")) {
            this.urlText = "";
        }

        return MessageFormat
                .format(LINK_PATTERN, linkPrefix, this.urlMode, this.urlTarget, this.urlTitle, imageElem,
                        this.urlText);
    }

    /**
     * @param rootUrl the rootUrl to set
     */
    public final void setGeneratedUrl(String generatedUrl) {
        // Read only
    }

    public String getBrowserTitle() {
        return translate("node_links_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("node_links_dialog_icon_tooltip");
    }

    @Override
    public String getContainerTitle() {
        return translate("node_links_dialog_title", getBestTitle(getActionNode()));
    }

    @Override
    public String getContainerDescription() {
        return translate("node_links_dialog_description");
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    /**
     * @return the browseUrl
     */
    public final String getBrowseUrl() {
        return browseUrl;
    }

    /**
     * @param browseUrl the browseUrl to set
     */
    public final void setBrowseUrl(String browseUrl) {
        // Read only
    }

    /**
     * @return the downloadUrl
     */
    public final String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * @param downloadUrl the downloadUrl to set
     */
    public final void setDownloadUrl(String downloadUrl) {
        // Read only
    }

    /**
     * @return the path
     */
    public final String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public final void setPath(String path) {
        // Read only
    }

    /**
     * @return the reference
     */
    public final String getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public final void setReference(String reference) {
        // Read only
    }

    /**
     * @return the urlMode
     */
    public final String getUrlMode() {
        return urlMode;
    }

    /**
     * @param urlMode the urlMode to set
     */
    public final void setUrlMode(String urlMode) {
        this.urlMode = urlMode;
    }

    /**
     * @return the urlTarget
     */
    public final String getUrlTarget() {
        return urlTarget;
    }

    /**
     * @param urlTarget the urlTarget to set
     */
    public final void setUrlTarget(String urlTarget) {
        this.urlTarget = urlTarget;
    }

    /**
     * @return the urlText
     */
    public final String getUrlText() {
        return urlText;
    }

    /**
     * @param urlText the urlText to set
     */
    public final void setUrlText(String urlText) {
        this.urlText = urlText;
    }

    /**
     * @return the urlTitle
     */
    public final String getUrlTitle() {
        return urlTitle;
    }

    /**
     * @param urlTitle the urlTitle to set
     */
    public final void setUrlTitle(String urlTitle) {
        this.urlTitle = urlTitle;
    }

    /**
     * @return the urlType
     */
    public final String getUrlType() {
        return urlType;
    }

    /**
     * @param urlType the urlType to set
     */
    public final void setUrlType(String urlType) {
        this.urlType = urlType;
    }

    /**
     * @return the urlIcon
     */
    public final String getUrlIcon() {
        return urlIcon;
    }

    /**
     * @param urlIcon the urlIcon to set
     */
    public final void setUrlIcon(String urlIcon) {
        this.urlIcon = urlIcon;
    }

    /**
     * @return the rootUrl
     */
    public final String getRootUrl() {
        return rootUrl;
    }

    /**
     * @param rootUrl the rootUrl to set
     */
    public final void setRootUrl(String rootUrl) {
        // Read only
    }

    /**
     * @return the isContent
     */
    public final boolean isContent() {
        return isContent;
    }

    /**
     * @return the contextPath
     */
    public final String getContextPath() {
        return contextPath;
    }

    /**
     * @param contextPath the contextPath to set
     */
    public final void setContextPath(String contextPath) {
        //		 Read only
    }

    /**
     * @return the circabcLogo
     */
    public final String getCircabcLogo() {
        return circabcLogo;
    }

    /**
     * @param circabcLogo the circabcLogo to set
     */
    public final void setCircabcLogo(String circabcLogo) {
        //		 Read only
    }

    /**
     * @return the igLogo
     */
    public final String getIgLogo() {
        return igLogo;
    }

    /**
     * @param igLogo the igLogo to set
     */
    public final void setIgLogo(String igLogo) {
        //		 Read only
    }


}
