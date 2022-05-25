/**
 *
 */
package eu.cec.digit.circabc.repo.customisation;

import eu.cec.digit.circabc.business.api.content.ContentBusinessSrv;
import eu.cec.digit.circabc.error.CircabcRuntimeException;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.customisation.ApplicationCustomisationService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** @author beaurpi */
public class ApplicationCustomisationServiceImpl implements ApplicationCustomisationService {

    static final Log logger = LogFactory.getLog(ApplicationCustomisationServiceImpl.class);

    private NodeService nodeService;
    private ContentService contentService;
    private ManagementService managementService;
    private ContentBusinessSrv contentBusinessSrv;

    public ApplicationCustomisationServiceImpl() {
    }

    public ContentBusinessSrv getContentBusinessSrv() {
        return contentBusinessSrv;
    }

    public void setContentBusinessSrv(ContentBusinessSrv contentBusinessSrv) {
        this.contentBusinessSrv = contentBusinessSrv;
    }

    @Override
    public NodeRef getDefaultLogoNodeRef() {

        NodeRef dicoRef = managementService.getCircabcDictionaryNodeRef();
        NodeRef igLookNodeRef =
                nodeService.getChildByName(dicoRef, ContentModel.ASSOC_CONTAINS, "iglookAndFeel");
        NodeRef iconNodeRef =
                nodeService.getChildByName(igLookNodeRef, ContentModel.ASSOC_CONTAINS, "icon");
        NodeRef imageFolderNodeRef =
                nodeService.getChildByName(iconNodeRef, ContentModel.ASSOC_CONTAINS, "images");
        NodeRef imageNodeRef =
                nodeService.getChildByName(
                        imageFolderNodeRef, ContentModel.ASSOC_CONTAINS, "circabc_logo.gif");

        return imageNodeRef;
    }

    @Override
    public void updateDefaultLogo(File gif) throws CircabcRuntimeException {

        updateNodeContent(gif, getDefaultLogoNodeRef());
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /** @return the contentService */
    public ContentService getContentService() {
        return contentService;
    }

    /** @param contentService the contentService to set */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Override
    public List<NodeRef> getListOfTemplates() {

        List<NodeRef> listOfTemplates = new ArrayList<>();

        NodeRef dicoRef = managementService.getCircabcDictionaryNodeRef();
        NodeRef templatesNodeRef =
                nodeService.getChildByName(dicoRef, ContentModel.ASSOC_CONTAINS, "templates");
        NodeRef mailsNodeRef =
                nodeService.getChildByName(templatesNodeRef, ContentModel.ASSOC_CONTAINS, "mails");
        List<ChildAssociationRef> allTemplatesFolderNodeRef = nodeService.getChildAssocs(mailsNodeRef);

        for (ChildAssociationRef child : allTemplatesFolderNodeRef) {
            if (child.getTypeQName().equals(ContentModel.ASSOC_CONTAINS)) {
                NodeRef template =
                        nodeService.getChildByName(
                                child.getChildRef(), ContentModel.ASSOC_CONTAINS, "default.ftl");
                listOfTemplates.add(template);
            }
        }

        return listOfTemplates;
    }

    @Override
    public void updateTemplate(File tempFile, NodeRef templateRef) throws CircabcRuntimeException {

        updateNodeContent(tempFile, templateRef);
    }

    /**
     * @param tempFile
     * @param fileRef
     */
    private void updateNodeContent(File tempFile, NodeRef fileRef) throws CircabcRuntimeException {
        if (fileRef != null) {

            if (tempFile != null) {
                ContentWriter cw = contentService.getWriter(fileRef, ContentModel.PROP_CONTENT, true);
                cw.putContent(tempFile);
            }

        } else {
            throw new CircabcRuntimeException(
                    "Cannot update the default.ftl text, not any template NodeRef found !");
        }
    }

    @Override
    public NodeRef getDefaultLogoDisclaimerNodeRef() {

        NodeRef dicoRef = managementService.getCircabcDictionaryNodeRef();
        NodeRef igLookNodeRef =
                nodeService.getChildByName(dicoRef, ContentModel.ASSOC_CONTAINS, "templates");
        NodeRef iconNodeRef =
                nodeService.getChildByName(igLookNodeRef, ContentModel.ASSOC_CONTAINS, "disclamer");
        NodeRef imageFolderNodeRef =
                nodeService.getChildByName(iconNodeRef, ContentModel.ASSOC_CONTAINS, "logo");
        NodeRef imageNodeRef =
                nodeService.getChildByName(
                        imageFolderNodeRef, ContentModel.ASSOC_CONTAINS, "circabc_logo.gif");

        return imageNodeRef;
    }

    @Override
    public void updateDefaultLogoDisclaimer(File gif) throws CircabcRuntimeException {

        updateNodeContent(gif, getDefaultLogoDisclaimerNodeRef());
    }

    @Override
    public void updateContactLink(String link) {

        NodeRef circabcRef = managementService.getCircabcNodeRef();
        if (!(link == null || link.isEmpty())) {
            nodeService.setProperty(circabcRef, CircabcModel.PROP_CONTACT_LINK, link);

        } else {
            nodeService.removeProperty(circabcRef, CircabcModel.PROP_CONTACT_LINK);
        }
    }

    @Override
    public String getContactlink() {

        NodeRef circabcRef = managementService.getCircabcNodeRef();
        return (nodeService.getProperty(circabcRef, CircabcModel.PROP_CONTACT_LINK) != null
                ? nodeService.getProperty(circabcRef, CircabcModel.PROP_CONTACT_LINK).toString()
                : null);
    }

    @Override
    public NodeRef getBannerLogoRef() {
        NodeRef cbcRef = managementService.getCircabcNodeRef();
        NodeRef bannerLogoRef =
                nodeService.getChildByName(cbcRef, ContentModel.ASSOC_CONTAINS, "circabc_banner.png");

        return bannerLogoRef;
    }

    @Override
    public void removeBannerLogoRef() {
        NodeRef cbcRef = managementService.getCircabcNodeRef();
        NodeRef bannerLogoRef =
                nodeService.getChildByName(cbcRef, ContentModel.ASSOC_CONTAINS, "circabc_banner.png");
        nodeService.deleteNode(bannerLogoRef);
    }

    @Override
    public void updateBannerLogoRef(File png) {

        if (png != null) {
            if (getBannerLogoRef() != null) {
                updateNodeContent(png, getBannerLogoRef());
            } else {
                createBannerLogoRef(png);
            }
        }
    }

    private void createBannerLogoRef(File png) {
        NodeRef cbcRef = managementService.getCircabcNodeRef();
        contentBusinessSrv.addContent(cbcRef, "circabc_banner.png", png, true);
    }

    @Override
    public Boolean getDisplaySearchLink() {
        NodeRef cbcRef = managementService.getCircabcNodeRef();
        Boolean result = false;

        if (nodeService.getProperty(cbcRef, CircabcModel.PROP_SEARCH_LINK_DISPLAYED) != null) {
            result =
                    Boolean.valueOf(
                            nodeService.getProperty(cbcRef, CircabcModel.PROP_SEARCH_LINK_DISPLAYED).toString());
        }

        return result;
    }

    @Override
    public void setDisplaySearchLink(Boolean b) {
        NodeRef cbcRef = managementService.getCircabcNodeRef();
        if (b != null) {
            nodeService.setProperty(cbcRef, CircabcModel.PROP_SEARCH_LINK_DISPLAYED, b);
        }
    }

    @Override
    public Boolean getDisplayLegalLink() {

        NodeRef cbcRef = managementService.getCircabcNodeRef();
        Boolean result = false;

        if (nodeService.getProperty(cbcRef, CircabcModel.PROP_LEGAL_LINK_DISPLAYED) != null) {
            result =
                    Boolean.valueOf(
                            nodeService.getProperty(cbcRef, CircabcModel.PROP_LEGAL_LINK_DISPLAYED).toString());
        }

        return result;
    }

    @Override
    public void setDisplayLegalLink(Boolean b) {
        NodeRef cbcRef = managementService.getCircabcNodeRef();
        if (b != null) {
            nodeService.setProperty(cbcRef, CircabcModel.PROP_LEGAL_LINK_DISPLAYED, b);
        }
    }

    @Override
    public String geteLearningLink() {

        NodeRef cbcRef = managementService.getCircabcNodeRef();
        String result = null;

        if (nodeService.getProperty(cbcRef, CircabcModel.PROP_ELEARNING_LINK) != null) {
            result = nodeService.getProperty(cbcRef, CircabcModel.PROP_ELEARNING_LINK).toString();
        }

        return result;
    }

    @Override
    public void seteLearningLink(String s) {
        NodeRef cbcRef = managementService.getCircabcNodeRef();
        if (s != null) {
            nodeService.setProperty(cbcRef, CircabcModel.PROP_ELEARNING_LINK, s);
        }
    }

    @Override
    public Boolean getDisplayeLearningLink() {

        NodeRef cbcRef = managementService.getCircabcNodeRef();
        Boolean result = false;

        if (nodeService.getProperty(cbcRef, CircabcModel.PROP_ELEARNING_LINK_DISPLAYED) != null) {
            result =
                    Boolean.valueOf(
                            nodeService
                                    .getProperty(cbcRef, CircabcModel.PROP_ELEARNING_LINK_DISPLAYED)
                                    .toString());
        }

        return result;
    }

    @Override
    public void setDisplayeLearningLink(Boolean b) {
        NodeRef cbcRef = managementService.getCircabcNodeRef();
        if (b != null) {
            nodeService.setProperty(cbcRef, CircabcModel.PROP_ELEARNING_LINK_DISPLAYED, b);
        }
    }

    @Override
    public String getErrorMessageContent() {

        NodeRef cbcRef = managementService.getCircabcNodeRef();
        String result = "";

        if (nodeService.getProperty(cbcRef, CircabcModel.PROP_ERROR_MESSAGE_CONTENT) != null) {
            result = nodeService.getProperty(cbcRef, CircabcModel.PROP_ERROR_MESSAGE_CONTENT).toString();
        }

        return result;
    }

    @Override
    public void setErrorMessageContent(String s) {

        NodeRef cbcRef = managementService.getCircabcNodeRef();

        if (s != null) {
            nodeService.setProperty(cbcRef, CircabcModel.PROP_ERROR_MESSAGE_CONTENT, s);
        }
    }
}
