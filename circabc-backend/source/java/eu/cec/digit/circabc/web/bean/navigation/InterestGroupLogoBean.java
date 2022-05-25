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
package eu.cec.digit.circabc.web.bean.navigation;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.service.customisation.logo.DefaultLogoConfiguration;
import eu.cec.digit.circabc.service.customisation.logo.LogoPreferencesService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.FacesException;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.MessageFormat;

/**
 * @author Yanick Pignot
 */
public class InterestGroupLogoBean implements Serializable {

    public static final String BEAN_NAME = "InterestGroupLogoBean";
    protected static final String MSG_LOG_TITLE = "iglogo_bean_icon_title";
    protected static final String MSG_LOG_ALT = "iglogo_bean_icon_alt";
    /**
     *
     */
    private static final long serialVersionUID = -1164290878171997321L;
    private static final Log logger = LogFactory.getLog(InterestGroupLogoBean.class);
    private String mainPageDescCssImgLeft;
    private String mainPageDescCssImgRight;
    private String mainPageIconCssImgLeft;
    private String mainPageIconCssImgRight;
    private String otherPagesIconCssImgRight;
    private String maxSizeCssPattern;
    private String sizeCssPattern;

    private Pair<Node, DefaultLogoConfiguration> currentNodeConfig;

    private CircabcNavigationBean navigator;
    private transient LogoPreferencesService logoPreferencesService;

    public void reset() {
        currentNodeConfig = null;
    }

    public boolean isNavigationDisplay() {
        return NavigableNodeType.IG_ROOT.equals(getNavigator().getCurrentNodeType()) == false
                && displayOtherPagesIcon();
    }

    public boolean isMainPageDisplay() {
        return displayMainPageIcon();
    }

    public boolean isDialogDisplay() {
        return displayOtherPagesIcon();
    }

    public boolean isWizardDisplayed() {
        return displayOtherPagesIcon();
    }

    public NodeRef getLogoRefrence() {
        final DefaultLogoConfiguration logoConfig = getLogoConfig();
        if (logoConfig != null && logoConfig.getLogo() != null
                && logoConfig.getLogo().getReference() != null) {
            return logoConfig.getLogo().getReference();
        } else {
            return null;
        }
    }

    public String getIconTitle() {
        final DefaultLogoConfiguration logoConfig = getLogoConfig();
        final String title =
                (logoConfig != null && logoConfig.getLogo() != null) ? logoConfig.getLogo().getTitle()
                        : null;
        if (title != null && title.length() > 0) {
            return title;
        } else {
            final FacesContext fc = FacesContext.getCurrentInstance();
            return MessageFormat.format(Application.getMessage(fc, MSG_LOG_TITLE),
                    getNavigator().getCurrentIGRoot().getName());
        }
    }

    public String getIconDesc() {
        final DefaultLogoConfiguration logoConfig = getLogoConfig();
        final String desc =
                (logoConfig != null && logoConfig.getLogo() != null) ? logoConfig.getLogo().getDescription()
                        : null;
        if (desc != null && desc.length() > 0) {
            return desc;
        } else {
            final FacesContext fc = FacesContext.getCurrentInstance();
            return MessageFormat.format(Application.getMessage(fc, MSG_LOG_ALT),
                    getNavigator().getCurrentIGRoot().getName());
        }
    }

    public final void setMainPageBinding(HtmlGraphicImage iconBinding) {
    }

    public final HtmlGraphicImage getOtherPagesIconBinding() {
        return getIcon(false);
    }

    public final void setOtherPagesIconBinding(final HtmlOutputText mainPageDescBinding) {
    }

    public final HtmlGraphicImage getMainPageIconBinding() {
        return getIcon(true);
    }

    /**
     * @return the mainPageDescBinding
     */
    public final HtmlOutputText getMainPageDescBinding() {
        final DefaultLogoConfiguration logoConfig = getLogoConfig();
        final FacesContext fc = FacesContext.getCurrentInstance();
        final HtmlOutputText mainPageDescBinding = (HtmlOutputText) fc.getApplication()
                .createComponent(HtmlOutputText.COMPONENT_TYPE);

        if (logoConfig.isMainPageLogoAtLeft()) {
            mainPageDescBinding.setStyle(mainPageDescCssImgLeft);
        } else {
            mainPageDescBinding.setStyle(mainPageDescCssImgRight);
        }
        return mainPageDescBinding;
    }

    public final void setMainPageDescBinding(final HtmlOutputText htmlOutputText) {
    }

    //-------
    // Helpers

    private DefaultLogoConfiguration getLogoConfig() {
        // keep == and not equals(). A new click = new instance of current node and allow refre
        if (currentNodeConfig != null && currentNodeConfig.getFirst() == getNavigator()
                .getCurrentNode()) {
            return currentNodeConfig.getSecond();
        } else {
            currentNodeConfig = null;
            final Node currentNode = getNavigator().getCurrentNode();

            try {
                final DefaultLogoConfiguration config = getLogoPreferencesService()
                        .getDefault(currentNode.getNodeRef());
                currentNodeConfig = new Pair<>(currentNode, config);
                return config;
            } catch (CustomizationException e) {
                logger.error("Impossible to retreive logo definition for " + currentNode.getNodeRef(), e);

                return null;
            }
        }
    }

    private boolean displayOtherPagesIcon() {
        if (getNavigator().getCurrentIGRoot() != null) {
            final DefaultLogoConfiguration logoConfig = getLogoConfig();
            return logoConfig != null
                    && logoConfig.isLogoDisplayedOnAllPages()
                    && logoConfig.getLogo() != null
                    && logoConfig.getLogo().getReference() != null;
        } else {
            return Boolean.FALSE;
        }
    }


    private boolean displayMainPageIcon() {
        if (NavigableNodeType.IG_ROOT.equals(getNavigator().getCurrentNodeType())) {
            final DefaultLogoConfiguration logoConfig = getLogoConfig();
            return logoConfig != null
                    && logoConfig.isLogoDisplayedOnMainPage()
                    && logoConfig.getLogo() != null
                    && logoConfig.getLogo().getReference() != null;
        } else {
            return Boolean.FALSE;
        }
    }

    /**
     * @return
     * @throws FacesException
     */
    private HtmlGraphicImage getIcon(boolean forMainPage) throws FacesException {
        final FacesContext fc = FacesContext.getCurrentInstance();
        final HtmlGraphicImage iconBinding = (HtmlGraphicImage) fc.getApplication()
                .createComponent(HtmlGraphicImage.COMPONENT_TYPE);
        try {
            final DefaultLogoConfiguration logoConfig = getLogoConfig();
            String url = WebClientHelper
                    .getGeneratedWaiUrl(logoConfig.getLogo().getReference(), ExtendedURLMode.HTTP_DOWNLOAD,
                            true);
            url += "?property=" + CircabcModel.PROP_CONTENT.toString();
            iconBinding.setValue(url);
            iconBinding.setTitle(getIconTitle());
            iconBinding.setAlt(getIconDesc());
            if (!forMainPage) {
                iconBinding.setStyle(otherPagesIconCssImgRight + getIconsSizeStyle(logoConfig, false));
            } else {
                if (logoConfig.isMainPageLogoAtLeft()) {
                    iconBinding.setStyle(mainPageIconCssImgLeft + getIconsSizeStyle(logoConfig, true));
                } else {
                    iconBinding.setStyle(mainPageIconCssImgRight + getIconsSizeStyle(logoConfig, true));
                }
            }
        } catch (Exception e) {
            // This happens if guest user can't access icon graphics that are attached to nodes like CircaBc root node.
            // the call logoConfig.getLogo().getReference() causes the Exception.

            // Fix: Return a transparent image instead.
            String url = WebClientHelper.getCircabcUrl();
            url += "/faces/images/extension/banner/transparent.gif";
            iconBinding.setValue(url);
        }
        return iconBinding;
    }

    private String getIconsSizeStyle(final DefaultLogoConfiguration logoConfig,
                                     final boolean mainPage) {
        final boolean force =
                mainPage ? logoConfig.isMainPageSizeForced() : logoConfig.isOtherPagesSizeForced();
        final int width =
                mainPage ? logoConfig.getMainPageLogoWidth() : logoConfig.getOtherPagesLogoWidth();
        final int height =
                mainPage ? logoConfig.getMainPageLogoHeight() : logoConfig.getOtherPagesLogoHeight();

        final String css = force ? sizeCssPattern : maxSizeCssPattern;

        return MessageFormat.format(css, width, height);
    }

    //-------
    // IOC

    /**
     * @return the logoPreferencesService
     */
    protected final LogoPreferencesService getLogoPreferencesService() {
        if (logoPreferencesService == null) {
            logoPreferencesService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getLogoPreferencesService();
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
     * @return the navigator
     */
    protected final CircabcNavigationBean getNavigator() {
        if (this.navigator == null) {
            this.navigator = Beans.getWaiNavigator();
        }
        return navigator;
    }

    /**
     * @param navigator the navigator to set
     */
    public final void setNavigator(CircabcNavigationBean navigator) {
        this.navigator = navigator;
    }

    /**
     * @param mainPageDescCssImgLeft the mainPageDescCssImgLeft to set
     */
    public final void setMainPageDescCssImgLeft(String mainPageDescCssImgLeft) {
        this.mainPageDescCssImgLeft = mainPageDescCssImgLeft;
    }

    /**
     * @param mainPageDescCssImgRight the mainPageDescCssImgRight to set
     */
    public final void setMainPageDescCssImgRight(String mainPageDescCssImgRight) {
        this.mainPageDescCssImgRight = mainPageDescCssImgRight;
    }

    /**
     * @param mainPageIconCssImgLeft the mainPageIconCssImgLeft to set
     */
    public final void setMainPageIconCssImgLeft(String mainPageIconCssImgLeft) {
        this.mainPageIconCssImgLeft = mainPageIconCssImgLeft;
    }

    /**
     * @param mainPageIconCssImgRight the mainPageIconCssImgRight to set
     */
    public final void setMainPageIconCssImgRight(String mainPageIconCssImgRight) {
        this.mainPageIconCssImgRight = mainPageIconCssImgRight;
    }

    /**
     * @param maxSizeCssPattern the maxSizeCssPattern to set
     */
    public final void setMaxSizeCssPattern(String maxSizeCssPattern) {
        this.maxSizeCssPattern = maxSizeCssPattern;
    }

    /**
     * @param otherPagesIconCssImgRight the otherPagesIconCssImgRight to set
     */
    public final void setOtherPagesIconCssImgRight(String otherPagesIconCssImgRight) {
        this.otherPagesIconCssImgRight = otherPagesIconCssImgRight;
    }

    /**
     * @param sizeCssPattern the sizeCssPattern to set
     */
    public final void setSizeCssPattern(String sizeCssPattern) {
        this.sizeCssPattern = sizeCssPattern;
    }
}
