/**
 *
 */
package eu.cec.digit.circabc.web.wai.dialog.admin;

import eu.cec.digit.circabc.service.customisation.ApplicationCustomisationService;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author beaurpi
 */
public class CircabcCustomisationDialog extends BaseWaiDialog {

    final static Log logger = LogFactory.getLog(CircabcCustomisationDialog.class);
    /**
     *
     */
    private static final long serialVersionUID = -5210758041740856385L;
    private ApplicationCustomisationService applicationCustomisationService;

    private UploadedFile appLogo, template, disclaimer, bannerLogo;
    private String pictureUrl, disclaimerUrl, bannerLogoUrl;
    private List<NodeRef> listOfTemplates;
    private List<UrlBean> listOfTemplateUrls;
    private List<SelectItem> selectListOfTemplateUrls;
    private String selectedTemplate, contactLink, eLearningLink, errorMessageContent;
    private Boolean searchLinkDisplayed, legalLinkDisplayed, eLearningLinkDisplayed;

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        setListOfTemplates(applicationCustomisationService.getListOfTemplates());

    }

    @Override
    public String getPageIconAltText() {

        return translate("customisation_console_description");
    }

    @Override
    public String getBrowserTitle() {

        return translate("customisation_console_title");
    }

    @Override
    public boolean isCancelButtonVisible() {

        return false;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {

        return outcome;
    }

    public void uploadAppLogo(ActionEvent event) {
        if (appLogo != null) {

            File tempFile = TempFileProvider.createTempFile("circabc_logo.gif", "tmp");
            FileOutputStream fs = null;

            try {

                fs = new FileOutputStream(tempFile);
                fs.write(appLogo.getBytes());

            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("error creating temp file when updating the ig logo", e);
                }
            } finally {
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e) {

                    }
                }
            }

            //this is required as the logo will be created with a new version.
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            AuthenticationUtil.setRunAsUserSystem();
            applicationCustomisationService.updateDefaultLogo(tempFile);
            AuthenticationUtil.setRunAsUser(username);
            tempFile.delete();
        }
    }

    public void uploadTemplate(ActionEvent event) {
        if (template != null) {

            File tempFile = TempFileProvider.createTempFile("default.ftl", "tmp");
            FileOutputStream fs = null;

            try {

                fs = new FileOutputStream(tempFile);
                fs.write(template.getBytes());

            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("error creating temp file when updating the template", e);
                }
            } finally {
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e) {

                    }
                }
            }
            //this is required as the template will be created with a new version.
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            AuthenticationUtil.setRunAsUserSystem();
            applicationCustomisationService.updateTemplate(tempFile, getSelectedTemplateNodeRef());
            AuthenticationUtil.setRunAsUser(username);
            tempFile.delete();
        }
    }

    public void uploadDisclaimerLogo(ActionEvent event) {
        if (disclaimer != null) {

            File tempFile = TempFileProvider.createTempFile("circabc_logo.gif", "tmp");
            FileOutputStream fs = null;

            try {

                fs = new FileOutputStream(tempFile);
                fs.write(disclaimer.getBytes());

            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("error creating temp file when updating the disclaimer logo", e);
                }
            } finally {
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e) {

                    }
                }
            }

            //this is required as the logo will be created with a new version.
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            AuthenticationUtil.setRunAsUserSystem();
            applicationCustomisationService.updateDefaultLogoDisclaimer(tempFile);
            AuthenticationUtil.setRunAsUser(username);
            tempFile.delete();
        }
    }

    private NodeRef getSelectedTemplateNodeRef() {

        NodeRef result = null;

        for (UrlBean ub : listOfTemplateUrls) {
            if (ub.getName().equals(selectedTemplate)) {
                result = ub.getNodeRef();
            }
        }

        return result;
    }

    /**
     * @return the appLogo
     */
    public UploadedFile getAppLogo() {
        return appLogo;
    }

    /**
     * @param appLogo the appLogo to set
     */
    public void setAppLogo(UploadedFile appLogo) {
        if (appLogo != null) {
            this.appLogo = appLogo;
        }
    }

    /**
     * @return the applicationCustomisationService
     */
    public ApplicationCustomisationService getApplicationCustomisationService() {
        return applicationCustomisationService;
    }

    /**
     * @param applicationCustomisationService the applicationCustomisationService to set
     */
    public void setApplicationCustomisationService(
            ApplicationCustomisationService applicationCustomisationService) {
        this.applicationCustomisationService = applicationCustomisationService;
    }

    /**
     * @return the pictureUrl
     */
    public String getPictureUrl() {

        if (pictureUrl == null) {
            pictureUrl = WebClientHelper
                    .getGeneratedWaiUrl(applicationCustomisationService.getDefaultLogoNodeRef(),
                            ExtendedURLMode.HTTP_DOWNLOAD, true);
        }

        return pictureUrl;
    }

    /**
     * @param pictureUrl the pictureUrl to set
     */
    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    /**
     * @return the listOfTemplates
     */
    public List<NodeRef> getListOfTemplates() {
        return listOfTemplates;
    }

    /**
     * @param listOfTemplates the listOfTemplates to set
     */
    public void setListOfTemplates(List<NodeRef> listOfTemplates) {
        this.listOfTemplates = listOfTemplates;
    }

    /**
     * @return the listOfTemplateUrls
     */
    public List<UrlBean> getListOfTemplateUrls() {

        listOfTemplateUrls = new ArrayList<>();

        for (NodeRef nRef : listOfTemplates) {
            if (nRef != null) {
                NodeRef folderRef = getNodeService().getParentAssocs(nRef).get(0).getParentRef();
                UrlBean ub = new UrlBean();
                ub.setName(getNodeService().getProperty(folderRef, ContentModel.PROP_NAME).toString());
                ub.setUrl(WebClientHelper.getGeneratedWaiUrl(nRef, ExtendedURLMode.HTTP_DOWNLOAD, true));
                ub.setNodeRef(nRef);

                listOfTemplateUrls.add(ub);
            }
        }

        return listOfTemplateUrls;
    }

    /**
     * @param listOfTemplateUrls the listOfTemplateUrls to set
     */
    public void setListOfTemplateUrls(List<UrlBean> listOfTemplateUrls) {
        this.listOfTemplateUrls = listOfTemplateUrls;
    }

    /**
     * @return the selectListOfTemplateUrls
     */
    public List<SelectItem> getSelectListOfTemplateUrls() {

        selectListOfTemplateUrls = new ArrayList<>();

        for (UrlBean ub : listOfTemplateUrls) {
            selectListOfTemplateUrls.add(new SelectItem(ub.getName(), ub.getName()));
        }

        return selectListOfTemplateUrls;
    }

    /**
     * @param selectListOfTemplateUrls the selectListOfTemplateUrls to set
     */
    public void setSelectListOfTemplateUrls(List<SelectItem> selectListOfTemplateUrls) {
        this.selectListOfTemplateUrls = selectListOfTemplateUrls;
    }

    /**
     * @return the selectedTemplate
     */
    public String getSelectedTemplate() {
        return selectedTemplate;
    }

    /**
     * @param selectedTemplate the selectedTemplate to set
     */
    public void setSelectedTemplate(String selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    /**
     * @return the template
     */
    public UploadedFile getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(UploadedFile template) {
        if (template != null) {
            this.template = template;
        }
    }

    /**
     * @return the disclaimer
     */
    public UploadedFile getDisclaimer() {
        return disclaimer;
    }

    /**
     * @param disclaimer the disclaimer to set
     */
    public void setDisclaimer(UploadedFile disclaimer) {
        this.disclaimer = disclaimer;
    }

    /**
     * @return the disclaimerUrl
     */
    public String getDisclaimerUrl() {

        if (disclaimerUrl == null) {
            disclaimerUrl = WebClientHelper
                    .getGeneratedWaiUrl(applicationCustomisationService.getDefaultLogoDisclaimerNodeRef(),
                            ExtendedURLMode.HTTP_DOWNLOAD, true);
        }

        return disclaimerUrl;
    }

    /**
     * @param disclaimerUrl the disclaimerUrl to set
     */
    public void setDisclaimerUrl(String disclaimerUrl) {
        this.disclaimerUrl = disclaimerUrl;
    }

    public void updateContactLink(ActionEvent event) {
        String[] schemes = {"http", "https"};
        UrlValidator uValid = new UrlValidator(schemes);

        if (contactLink == null || contactLink.equals("")) {
            applicationCustomisationService.updateContactLink("");
        } else if (uValid.isValid(contactLink) || contactLink.startsWith("mailto")) {
            applicationCustomisationService
                    .updateContactLink(getSecurityService().getCleanHTML(contactLink, true));
        } else {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    translate("customisation_console_invalid_contact_link"));
            logger.warn("Invalid contact link provided:" + contactLink);
        }
    }

    /**
     * @return the contactLink
     */
    public String getContactLink() {
        return (applicationCustomisationService.getContactlink() != null
                ? applicationCustomisationService.getContactlink() : "");
    }

    /**
     * @param contactLink the contactLink to set
     */
    public void setContactLink(String contactLink) {
        this.contactLink = contactLink;
    }

    /**
     * @return the bannerLogo
     */
    public UploadedFile getBannerLogo() {
        return bannerLogo;
    }

    /**
     * @param bannerLogo the bannerLogo to set
     */
    public void setBannerLogo(UploadedFile bannerLogo) {
        this.bannerLogo = bannerLogo;
    }

    /**
     * @return the bannerLogoUrl
     */
    public String getBannerLogoUrl() {

        if (bannerLogoUrl == null && applicationCustomisationService.getBannerLogoRef() != null) {
            bannerLogoUrl = WebClientHelper
                    .getGeneratedWaiUrl(applicationCustomisationService.getBannerLogoRef(),
                            ExtendedURLMode.HTTP_DOWNLOAD, true);
        }

        return bannerLogoUrl;
    }

    /**
     * @param bannerLogoUrl the bannerLogoUrl to set
     */
    public void setBannerLogoUrl(String bannerLogoUrl) {
        this.bannerLogoUrl = bannerLogoUrl;
    }

    public void uploadBannerLogo(ActionEvent event) {

        if (bannerLogo != null) {

            File tempFile = TempFileProvider.createTempFile("circabc_banner.png", "tmp");
            FileOutputStream fs = null;

            try {

                fs = new FileOutputStream(tempFile);
                fs.write(bannerLogo.getBytes());

            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("error creating temp file when updating the banner logo", e);
                }
            } finally {
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e) {

                    }
                }
            }

            //this is required as the logo will be created with a new version.
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            AuthenticationUtil.setRunAsUserSystem();
            applicationCustomisationService.updateBannerLogoRef(tempFile);
            AuthenticationUtil.setRunAsUser(username);
            tempFile.delete();
        }
    }

    public void removeBannerLogo(ActionEvent event) {
        this.bannerLogoUrl = null;
        applicationCustomisationService.removeBannerLogoRef();
    }

    /**
     * @return the searchLinkDisplayed
     */
    public Boolean getSearchLinkDisplayed() {

        searchLinkDisplayed = false;

        if (applicationCustomisationService.getDisplaySearchLink() != null) {
            searchLinkDisplayed = applicationCustomisationService.getDisplaySearchLink();
        }

        return searchLinkDisplayed;
    }

    /**
     * @param searchLinkDisplayed the searchLinkDisplayed to set
     */
    public void setSearchLinkDisplayed(Boolean searchLinkDisplayed) {
        this.searchLinkDisplayed = searchLinkDisplayed;
    }

    /**
     * @return the legalLinkDisplayed
     */
    public Boolean getLegalLinkDisplayed() {

        legalLinkDisplayed = false;

        if (applicationCustomisationService.getDisplayLegalLink() != null) {
            legalLinkDisplayed = applicationCustomisationService.getDisplayLegalLink();
        }

        return legalLinkDisplayed;
    }

    /**
     * @param legalLinkDisplayed the legalLinkDisplayed to set
     */
    public void setLegalLinkDisplayed(Boolean legalLinkDisplayed) {
        this.legalLinkDisplayed = legalLinkDisplayed;
    }

    public void displaySearchLink(ActionEvent event) {
        UIActionLink uButton = (UIActionLink) event.getComponent();
        if (uButton.getParameterMap().containsKey("status")) {
            String status = uButton.getParameterMap().get("status");
            if (status.equals("show")) {
                applicationCustomisationService.setDisplaySearchLink(true);
            } else {
                applicationCustomisationService.setDisplaySearchLink(false);
            }
        }

    }

    public void displayLegalLink(ActionEvent event) {
        UIActionLink uButton = (UIActionLink) event.getComponent();
        if (uButton.getParameterMap().containsKey("status")) {
            String status = uButton.getParameterMap().get("status");
            if (status.equals("show")) {
                applicationCustomisationService.setDisplayLegalLink(true);
            } else {
                applicationCustomisationService.setDisplayLegalLink(false);
            }
        }
    }

    /**
     * @return the eLearningLink
     */
    public String geteLearningLink() {

        eLearningLink = "";

        if (applicationCustomisationService.geteLearningLink() != null) {
            eLearningLink = applicationCustomisationService.geteLearningLink();
        }

        return eLearningLink;
    }

    /**
     * @param eLearningLink the eLearningLink to set
     */
    public void seteLearningLink(String eLearningLink) {
        this.eLearningLink = eLearningLink;
    }

    /**
     * @return the eLearningLinkDisplayed
     */
    public Boolean geteLearningLinkDisplayed() {

        eLearningLinkDisplayed = false;

        if (applicationCustomisationService.getDisplayeLearningLink() != null) {
            eLearningLinkDisplayed = applicationCustomisationService.getDisplayeLearningLink();
        }

        return eLearningLinkDisplayed;
    }

    /**
     * @param eLearningLinkDisplayed the eLearningLinkDisplayed to set
     */
    public void seteLearningLinkDisplayed(Boolean eLearningLinkDisplayed) {
        this.eLearningLinkDisplayed = eLearningLinkDisplayed;
    }

    public void displayeLearningLink(ActionEvent event) {
        UIActionLink uButton = (UIActionLink) event.getComponent();
        if (uButton.getParameterMap().containsKey("status")) {
            String status = uButton.getParameterMap().get("status");
            if (status.equals("show")) {
                applicationCustomisationService.setDisplayeLearningLink(true);
            } else {
                applicationCustomisationService.setDisplayeLearningLink(false);
            }
        }
    }

    public void updateeLearningLink(ActionEvent event) {
        String[] schemes = {"http", "https"};
        UrlValidator uValid = new UrlValidator(schemes);

        if (uValid.isValid(eLearningLink)) {
            applicationCustomisationService
                    .seteLearningLink(getSecurityService().getCleanHTML(eLearningLink, true));
        } else {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    translate("customisation_console_invalid_elearning_link"));
            logger.warn("Invalid eLearning link provided:" + eLearningLink);
        }
    }

    /**
     * @return the errorMessageContent
     */
    public String getErrorMessageContent() {

        if (errorMessageContent == null) {
            errorMessageContent = applicationCustomisationService.getErrorMessageContent();
        }

        return errorMessageContent;
    }

    /**
     * @param errorMessageContent the errorMessageContent to set
     */
    public void setErrorMessageContent(String errorMessageContent) {
        this.errorMessageContent = errorMessageContent;
    }

    public void updateErrorMessageContent(ActionEvent event) {
        if (errorMessageContent != null) {
            String cleanString = getCleanHTML(errorMessageContent, true);
            applicationCustomisationService.setErrorMessageContent(cleanString);
        }
    }
}
