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
package eu.cec.digit.circabc.web.wai.dialog.customization;

import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.service.customisation.logo.DefaultLogoConfiguration;
import eu.cec.digit.circabc.service.customisation.logo.LogoDefinition;
import eu.cec.digit.circabc.service.customisation.logo.LogoPreferencesService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.InterestGroupLogoBean;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.repository.IGServicesNode;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.html.ext.SortableModel;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dialog for navigation preferences managing.
 *
 * @author Yanick Pignot
 */
public class ManageLogoPreference extends BaseWaiDialog {

    private static final String INFO_CLICKOK = "manage_iglogo_dialog_info_clickok";

    private static final Log logger = LogFactory.getLog(ManageLogoPreference.class);

    private static final String MSG_ERROR_GET_LOGO = "manage_iglogo_dialog_error_get_definition";
    private static final String MSG_ERROR_SET_LOGO = "manage_iglogo_dialog_error_set_definition";
    private static final String MSG_ERROR_BAD_WIDTH = "manage_iglogo_dialog_error_width";
    private static final String MSG_ERROR_BAD_HEIGHT = "manage_iglogo_dialog_error_height";

    private static final String MSG_ERROR_UPLOAD = "manage_iglogo_dialog_define_upload_logo_error";
    private static final String MSG_ERROR_UPLOAD_IO = "manage_iglogo_dialog_define_upload_logo_error_io";
    private static final String MSG_ERROR_UPLOAD_NAME = "manage_iglogo_dialog_define_upload_logo_error_name";
    private static final String MSG_ERROR_UPLOAD_NULL = "manage_iglogo_dialog_define_upload_logo_error_upload";
    private static final String MSG_ERROR_NOT_CONTENT = "manage_iglogo_dialog_define_upload_logo_error_not_content";

    private static final String MSG_UPLOAD_SUCC_LINK = "manage_iglogo_dialog_define_upload_logo_success_link";
    private static final String MSG_UPLOAD_SUCC_FILE = "manage_iglogo_dialog_define_upload_logo_success_file";

    private static final String MSG_LOGO = "manage_iglogo_dialog_info_logo";
    private static final String MSG_NO_LOGO = "manage_iglogo_dialog_info_nologo";

    /**
     *
     */
    private static final long serialVersionUID = -6666145542680105661L;

    private transient LogoPreferencesService logoPreferencesService;
    private transient ContentService contentService;
    private InterestGroupLogoBean interestGroupLogoBean;

    private Boolean logoDisplayedOnMainPage;
    private Boolean logoDisplayedOnAllPages;

    private String mainPageLogoWidth;
    private String mainPageLogoHeight;
    private Boolean mainPageSizeForced;
    private Boolean mainPageLogoAtLeft;

    private String otherPagesLogoWidth;
    private String otherPagesLogoHeight;
    private Boolean otherPagesSizeForced;

    private String selectedLogid;
    private String selectedLogname;

    transient private DataModel logoDataModel;
    transient private DataModel servicesDataModel;
    private List<LogoDefinition> logos;
    private List<ServiceConfigWrapper> serviceConfigs;

    private NodeRef iconLink;

    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            this.iconLink = null;
        }

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id parameter is mandatory");
        }

        if (parameters != null) {
            reset();
        }
    }

    @Override
    public void restored() {
        reset();
    }

    /**
     *
     */
    private void reset() throws IllegalStateException {
        this.logos = null;
        this.serviceConfigs = null;
        this.logoDisplayedOnMainPage = null;
        this.mainPageLogoWidth = null;
        this.mainPageLogoHeight = null;
        this.mainPageSizeForced = null;
        this.mainPageLogoAtLeft = null;
        this.logoDisplayedOnAllPages = null;
        this.otherPagesLogoWidth = null;
        this.otherPagesLogoHeight = null;
        this.otherPagesSizeForced = null;

        this.selectedLogid = null;
        this.selectedLogname = null;
        this.logoDataModel = null;
        this.servicesDataModel = null;

        final NodeRef ref = getActionNode().getNodeRef();

        try {
            final DefaultLogoConfiguration defaultConfiguration = getLogoPreferencesService()
                    .getDefault(ref);
            resetLogos(ref);

            this.logoDisplayedOnMainPage = defaultConfiguration.isLogoDisplayedOnMainPage();
            this.logoDisplayedOnAllPages = defaultConfiguration.isLogoDisplayedOnAllPages();
            this.mainPageSizeForced = defaultConfiguration.isMainPageSizeForced();
            this.mainPageLogoAtLeft = defaultConfiguration.isMainPageLogoAtLeft();
            this.otherPagesSizeForced = defaultConfiguration.isOtherPagesSizeForced();

            final int widthIgHome = defaultConfiguration.getMainPageLogoWidth();
            final int heightIgHome = defaultConfiguration.getMainPageLogoHeight();
            final int widthOthers = defaultConfiguration.getOtherPagesLogoWidth();
            final int heightOthers = defaultConfiguration.getOtherPagesLogoHeight();

            this.mainPageLogoWidth = (widthIgHome > 0) ? String.valueOf(widthIgHome) : "";
            this.mainPageLogoHeight = (heightIgHome > 0) ? String.valueOf(heightIgHome) : "";
            this.otherPagesLogoWidth = (widthOthers > 0) ? String.valueOf(widthOthers) : "";
            this.otherPagesLogoHeight = (heightOthers > 0) ? String.valueOf(heightOthers) : "";

            if (defaultConfiguration.getLogo() != null
                    && defaultConfiguration.getLogo().getReference() != null) {
                selectedLogid = defaultConfiguration.getLogo().getReference().getId();
                selectedLogname = defaultConfiguration.getLogo().getName();
            }

            if (isServiceConfigAllowed()) {
                final InterestGroupNode interestGroup = (InterestGroupNode) getNavigator()
                        .getCurrentIGRoot();
                this.serviceConfigs = new ArrayList<>();
                for (final NavigableNode service : interestGroup.getNavigableChilds()) {
                    this.serviceConfigs.add(new ServiceConfigWrapper(
                            getLogoPreferencesService().getDefault(service.getNodeRef()),
                            (IGServicesNode) service,
                            logos));
                }
            }

            // other definition properties are no taken in account yet.
        } catch (CustomizationException e) {
            throw new IllegalStateException(translate(MSG_ERROR_GET_LOGO, e.getMessage()), e);
        }
    }

    /**
     *
     */
    private void resetLogos(final NodeRef ref) throws CustomizationException {
        this.logoDataModel = null;
        logos = getLogoPreferencesService().getAllLogos(ref);
    }


    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        try {
            final Integer width = getSafeSize(this.mainPageLogoWidth);
            final Integer height = getSafeSize(this.mainPageLogoHeight);

            if (width == null || height == null) {
                if (width == null) {
                    Utils.addErrorMessage(translate(MSG_ERROR_BAD_WIDTH, this.mainPageLogoWidth));
                }
                if (height == null) {
                    Utils.addErrorMessage(translate(MSG_ERROR_BAD_HEIGHT, this.mainPageLogoHeight));
                }

                return null;
            } else {
                final NodeRef currentLocationRef = getActionNode().getNodeRef();

                final NodeRef defaultLogoRef;

                if (selectedLogid == null || selectedLogid.length() < 1) {
                    defaultLogoRef = null;
                } else {
                    defaultLogoRef = new NodeRef(Repository.getStoreRef(), selectedLogid);
                }

                getLogoPreferencesService().setDefault(currentLocationRef,
                        defaultLogoRef);

                getLogoPreferencesService().setMainPageLogoConfig(currentLocationRef,
                        this.logoDisplayedOnMainPage,
                        height,
                        width,
                        this.mainPageSizeForced,
                        this.mainPageLogoAtLeft);

                getLogoPreferencesService().setOtherPagesLogoConfig(currentLocationRef,
                        this.logoDisplayedOnAllPages,
                        getSafeSize(this.otherPagesLogoHeight),
                        getSafeSize(this.otherPagesLogoWidth),
                        this.otherPagesSizeForced);

                if (serviceConfigs != null && isServiceConfigAllowed()) {
                    for (final ServiceConfigWrapper wrapper : serviceConfigs) {
                        if (wrapper.needToBeDeleted(defaultLogoRef, this.otherPagesSizeForced)) {
                            getLogoPreferencesService().removeConfiguration(wrapper.getService().getNodeRef());
                        } else {
                            if (wrapper.needToChangeLogo()) {
                                getLogoPreferencesService()
                                        .setDefault(wrapper.getService().getNodeRef(), wrapper.getSelectedLogo());
                            }
                            if (wrapper.needToChangeDisplay()) {
                                getLogoPreferencesService().setOtherPagesLogoConfig(
                                        wrapper.getService().getNodeRef(),
                                        wrapper.getDisplay(),
                                        wrapper.getOriginalConfiguration().getMainPageLogoHeight(),
                                        wrapper.getOriginalConfiguration().getMainPageLogoWidth(),
                                        this.otherPagesSizeForced
                                );
                            }
                        }
                    }
                }

                // reset current settings
                getInterestGroupLogoBean().reset();

                return outcome;
            }

        } catch (final Throwable t) {
            Utils.addErrorMessage(translate(MSG_ERROR_SET_LOGO, t.getMessage()));

            if (logger.isErrorEnabled()) {
                logger.error("Unexpected error during settings of logo definition on " + getActionNode()
                        .getNodePath(), t);
            }

            return null;
        }

    }

    public String getLibraryId() {
        final NavigableNode library = (NavigableNode) getNavigator().getCurrentIGRoot()
                .get(InterestGroupNode.LIBRARY_SERVICE);

        // if library is null, the user probably has not rights to see it
        return library == null ? null : library.getId();
    }


    /**
     *
     */
    private Integer getSafeSize(String sizeStr) {
        sizeStr = sizeStr == null ? "" : sizeStr.trim();

        if (sizeStr.length() == 0 || sizeStr.equals("0")) {
            return -1;
        } else {
            try {
                return Integer.valueOf(sizeStr);
            } catch (final NumberFormatException nfe) {
                return null;
            }
        }
    }

    public DataModel getLogosDataModel() {
        if (this.logoDataModel == null) {
            this.logoDataModel = new SortableModel();
            this.logoDataModel.setWrappedData(getLogoDefinitions());
        }
        return this.logoDataModel;
    }

    public DataModel getServicesDataModel() {
        if (this.servicesDataModel == null) {
            this.servicesDataModel = new SortableModel();
            if (this.serviceConfigs != null) {
                this.servicesDataModel.setWrappedData(this.serviceConfigs);
            }
        }
        return this.servicesDataModel;
    }

    public List<LogoWrapper> getLogoDefinitions() {
        final Node currentNode = getActionNode();
        final NodeService nodeService = getNodeService();

        final List<LogoWrapper> wrappers = new ArrayList<>(logos.size());
        for (final LogoDefinition def : logos) {
            wrappers.add(new LogoWrapper(def, currentNode, nodeService, selectedLogid));
        }

        return wrappers;
    }

    public String getInfoMessage() {
        if (isLogoSelected()) {
            return translate(MSG_LOGO, selectedLogname);
        } else {
            return translate(MSG_NO_LOGO);
        }
    }

    public void deselect(final ActionEvent event) {
        reset();
        this.selectedLogid = null;
        this.selectedLogname = null;

        Utils.addStatusMessage(FacesMessage.SEVERITY_WARN, translate(INFO_CLICKOK));
    }

    public void select(final ActionEvent event) {
        final LogoWrapper wrapper = (LogoWrapper) this.logoDataModel.getRowData();

        reset();
        this.selectedLogid = wrapper.getReference().getId();
        this.selectedLogname = wrapper.getName();

        Utils.addStatusMessage(FacesMessage.SEVERITY_WARN, translate(INFO_CLICKOK));
    }

    public boolean isHomeConfigAllowed() {
        final NavigableNodeType type = getNavigator().getCurrentNodeType();
        return NavigableNodeType.IG_ROOT.equals(type) || NavigableNodeType.CATEGORY.equals(type);
    }

    public boolean isServiceConfigAllowed() {
        final NavigableNodeType type = getNavigator().getCurrentNodeType();
        return NavigableNodeType.IG_ROOT.equals(type);
    }

    public boolean isLogoSelected() {
        return getSelectedLogid() != null;
    }

    @Override
    public String getContainerTitle() {
        return translate("manage_iglogo_dialog_title", getActionNode().getName());
    }

    public String getBrowserTitle() {
        return translate("manage_iglogo_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("manage_iglogo_dialog_icon_tooltip");
    }

    /**
     * @return the selectedLogid
     */
    public final String getSelectedLogid() {
        return selectedLogid;
    }


    /**
     * @param selectedLogid the selectedLogid to set
     */
    public final void setSelectedLogid(String selectedLogid) {
        this.selectedLogid = selectedLogid;
    }


    /**
     * @return the logoDisplayedOnAllPages
     */
    public final Boolean getLogoDisplayedOnAllPages() {
        return logoDisplayedOnAllPages;
    }


    /**
     * @param logoDisplayedOnAllPages the logoDisplayedOnAllPages to set
     */
    public final void setLogoDisplayedOnAllPages(Boolean logoDisplayedOnAllPages) {
        this.logoDisplayedOnAllPages = logoDisplayedOnAllPages;
    }


    /**
     * @return the logoDisplayedOnMainPage
     */
    public final Boolean getLogoDisplayedOnMainPage() {
        return logoDisplayedOnMainPage;
    }


    /**
     * @param logoDisplayedOnMainPage the logoDisplayedOnMainPage to set
     */
    public final void setLogoDisplayedOnMainPage(Boolean logoDisplayedOnMainPage) {
        this.logoDisplayedOnMainPage = logoDisplayedOnMainPage;
    }


    /**
     * @return the mainPageLogoAtLeft
     */
    public final Boolean getMainPageLogoAtLeft() {
        return mainPageLogoAtLeft;
    }


    /**
     * @param mainPageLogoAtLeft the mainPageLogoAtLeft to set
     */
    public final void setMainPageLogoAtLeft(Boolean mainPageLogoAtLeft) {
        this.mainPageLogoAtLeft = mainPageLogoAtLeft;
    }


    /**
     * @return the mainPageLogoHeight
     */
    public final String getMainPageLogoHeight() {
        return mainPageLogoHeight;
    }


    /**
     * @param mainPageLogoHeight the mainPageLogoHeight to set
     */
    public final void setMainPageLogoHeight(String mainPageLogoHeight) {
        this.mainPageLogoHeight = mainPageLogoHeight;
    }


    /**
     * @return the mainPageLogoWidth
     */
    public final String getMainPageLogoWidth() {
        return mainPageLogoWidth;
    }


    /**
     * @param mainPageLogoWidth the mainPageLogoWidth to set
     */
    public final void setMainPageLogoWidth(String mainPageLogoWidth) {
        this.mainPageLogoWidth = mainPageLogoWidth;
    }


    /**
     * @return the mainPageSizeForced
     */
    public final Boolean getMainPageSizeForced() {
        return mainPageSizeForced;
    }


    /**
     * @param mainPageSizeForced the mainPageSizeForced to set
     */
    public final void setMainPageSizeForced(Boolean mainPageSizeForced) {
        this.mainPageSizeForced = mainPageSizeForced;
    }

    /**
     * @return the logoPreferencesService
     */
    protected final LogoPreferencesService getLogoPreferencesService() {
        if (this.logoPreferencesService == null) {
            this.logoPreferencesService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance()).getLogoPreferencesService();
        }
        return logoPreferencesService;
    }


    /**
     * @param logoPreferencesService the logoPreferencesService to set
     */
    public final void setLogoPreferencesService(LogoPreferencesService logoPreferencesService) {
        this.logoPreferencesService = logoPreferencesService;
    }


    /**
     * @return the otherPagesSizeForced
     */
    public final Boolean getOtherPagesSizeForced() {
        return otherPagesSizeForced;
    }


    /**
     * @param otherPagesSizeForced the otherPagesSizeForced to set
     */
    public final void setOtherPagesSizeForced(Boolean otherPagesSizeForced) {
        this.otherPagesSizeForced = otherPagesSizeForced;
    }


    /**
     * @return the otherPagesLogoHeight
     */
    public final String getOtherPagesLogoHeight() {
        return otherPagesLogoHeight;
    }


    /**
     * @param otherPagesLogoHeight the otherPagesLogoHeight to set
     */
    public final void setOtherPagesLogoHeight(String otherPagesLogoHeight) {
        // read only parameter
    }


    /**
     * @return the otherPagesLogoWidth
     */
    public final String getOtherPagesLogoWidth() {
        return otherPagesLogoWidth;
    }


    /**
     * @param otherPagesLogoWidth the otherPagesLogoWidth to set
     */
    public final void setOtherPagesLogoWidth(String otherPagesLogoWidth) {
        // read only parameter
    }


    /**
     * @return the interestGroupLogoBean
     */
    protected final InterestGroupLogoBean getInterestGroupLogoBean() {
        if (interestGroupLogoBean == null) {
            interestGroupLogoBean = (InterestGroupLogoBean) Beans
                    .getBean(InterestGroupLogoBean.BEAN_NAME);
        }
        return interestGroupLogoBean;
    }


    /**
     * @param interestGroupLogoBean the interestGroupLogoBean to set
     */
    public final void setInterestGroupLogoBean(InterestGroupLogoBean interestGroupLogoBean) {
        this.interestGroupLogoBean = interestGroupLogoBean;
    }


    /**
     * @return the iconLink
     */
    public final NodeRef getIconLink() {
        return iconLink;
    }

    /**
     * @param iconLink the iconLink to set
     */
    public final void setIconLink(NodeRef iconLink) {
        this.iconLink = iconLink;

        if (iconLink != null) {
            final String name = (String) getNodeService().getProperty(iconLink, ContentModel.PROP_NAME);
            final NodeRef currentRef = getActionNode().getNodeRef();
            final QName type = getNodeService().getType(iconLink);
            if (ContentModel.TYPE_CONTENT.equals(type) == false) {
                Utils.addErrorMessage(translate(MSG_ERROR_NOT_CONTENT));
            } else if (isDuplicateLogoName(currentRef, name) == false) {
                InputStream inputStream = null;

                try {
                    inputStream = getContentService().getReader(iconLink, ContentModel.PROP_CONTENT)
                            .getContentInputStream();
                    getLogoPreferencesService().addLogo(currentRef, name, inputStream);

                    // Return at the previous selection
                    this.iconLink = getNodeService().getPrimaryParent(iconLink).getParentRef();

                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_UPLOAD_SUCC_LINK, name));

                    resetLogos(currentRef);
                } catch (CustomizationException e) {
                    Utils.addErrorMessage(translate(MSG_ERROR_UPLOAD, name, e.getMessage()));
                } catch (IOException e) {
                    Utils.addErrorMessage(translate(MSG_ERROR_UPLOAD_IO, name, e.getMessage()));
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception ignore) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Error closing input stream.", ignore);
                        }
                    }
                }
            }
        }
    }

    /**
     *
     */
    private Boolean isDuplicateLogoName(final NodeRef currentRef, final String name) {
        Boolean duplicate = Boolean.FALSE;

        for (final LogoDefinition def : logos) {
            if (def.getName().equalsIgnoreCase(name) && def.getDefinedOn().equals(currentRef)) {
                Utils.addErrorMessage(translate(MSG_ERROR_UPLOAD_NAME, name));
                duplicate = Boolean.TRUE;
                break;
            }
        }

        return duplicate;
    }

    /**
     * @return the iconFile
     */
    public final String getIconFile() {
        return null;
    }

    /**
     * @param name the iconFile to set
     */
    public final void setIconFile(String name) {

        if (name == null || name.length() < 1) {
            // the dialog is submited, but not with
            return;
        }

        // we also need to keep the file upload bean in sync
        final FacesContext ctx = FacesContext.getCurrentInstance();
        final FileUploadBean fileBean = (FileUploadBean) ctx.getExternalContext().
                getSessionMap().get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);

        if (fileBean != null) {
            final NodeRef currentRef = getActionNode().getNodeRef();
            InputStream inputStream = null;
            if (isDuplicateLogoName(currentRef, name) == false) {
                try {
                    inputStream = new FileInputStream(fileBean.getFile());
                    getLogoPreferencesService().addLogo(currentRef, name, inputStream);

                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_UPLOAD_SUCC_FILE, name));

                    resetLogos(currentRef);
                } catch (CustomizationException e) {
                    Utils.addErrorMessage(translate(MSG_ERROR_UPLOAD, name, e.getMessage()));
                } catch (IOException e) {
                    Utils.addErrorMessage(translate(MSG_ERROR_UPLOAD_IO, name, e.getMessage()));
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception ignore) {
                    }

                    try {
                        fileBean.getFile().delete();
                    } catch (Exception ignore) {
                    }
                }

            }

        } else {
            Utils.addErrorMessage(translate(MSG_ERROR_UPLOAD_NULL));
        }
    }

    /**
     * @return the contentService
     */
    protected final ContentService getContentService() {
        if (contentService == null) {
            contentService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getContentService();
        }

        return contentService;
    }

    /**
     * @param contentService the contentService to set
     */
    public final void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

}
