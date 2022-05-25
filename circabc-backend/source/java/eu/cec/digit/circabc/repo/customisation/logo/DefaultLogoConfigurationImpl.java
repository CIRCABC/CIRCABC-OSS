/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.customisation.logo;

import eu.cec.digit.circabc.service.customisation.logo.DefaultLogoConfiguration;
import eu.cec.digit.circabc.service.customisation.logo.LogoDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;

/**
 * The base logo configuration wrapper
 *
 * @author yanick pignot
 */
public class DefaultLogoConfigurationImpl
        implements DefaultLogoConfiguration, Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = -4254883146093804060L;

    private LogoDefinitionImpl logo = null;
    private NodeRef configurationRef;
    private NodeRef configurationOn;

    private boolean logoDisplayedOnMainPage = Boolean.FALSE;
    private int mainPageLogoWidth = -1;
    private int mainPageLogoHeight = -1;
    private boolean mainPageSizeForced = Boolean.FALSE;
    private boolean mainPageLogoAtLeft = Boolean.TRUE;

    private boolean logoDisplayedOnAllPages = Boolean.FALSE;
    private int otherPagesLogoWidth = -1;
    private int otherPagesLogoHeight = -1;
    private boolean otherPagesSizeForced = Boolean.FALSE;

    /**
     * @param configurationRef
     */
    /*package*/ DefaultLogoConfigurationImpl(
            final NodeRef configurationOn, final NodeRef configurationRef) {
        super();
        this.configurationRef = configurationRef;
        this.configurationOn = configurationOn;
    }

    /**
     * @return the logo
     */
    public final LogoDefinition getLogo() {
        return logo;
    }

    /*package*/
    final void setLogo(LogoDefinitionImpl logoDef) {
        this.logo = logoDef;
    }

    /*package*/
    final void setLogo(final NodeRef logoRef) {
        if (logoRef != null) {
            if (this.logo == null) {
                this.logo = new LogoDefinitionImpl();
            }

            this.logo.setReference(logoRef);
        }
    }

    /*package*/
    final void setLogo(final String logoRefStr) {
        if (notEmpty(logoRefStr)) {
            setLogo(new NodeRef(logoRefStr));
        }
    }

    /**
     * @return the logoDisplayedOnAllPages
     */
    public final boolean isLogoDisplayedOnAllPages() {
        return logoDisplayedOnAllPages;
    }

    /**
     * @param logoDisplayedOnAllPages the logoDisplayedOnAllPages to set
     */
    /*package*/
    final void setLogoDisplayedOnAllPages(String logoDisplayedOnAllPages) {
        if (notEmpty(logoDisplayedOnAllPages)) {
            this.logoDisplayedOnAllPages = Boolean.valueOf(logoDisplayedOnAllPages);
        }
    }

    /**
     * @param logoDisplayedOnAllPages the logoDisplayedOnAllPages to set
     */
    /*package*/
    final void setLogoDisplayedOnAllPages(boolean logoDisplayedOnAllPages) {
        this.logoDisplayedOnAllPages = logoDisplayedOnAllPages;
    }

    /**
     * @return the mainPageLogoAtLeft
     */
    public final boolean isMainPageLogoAtLeft() {
        return mainPageLogoAtLeft;
    }

    /**
     * @param mainPageLogoAtLeft the mainPageLogoAtLeft to set
     */
    /*package*/
    final void setMainPageLogoAtLeft(String mainPageLogoAtLeft) {
        if (notEmpty(mainPageLogoAtLeft)) {
            this.mainPageLogoAtLeft = Boolean.valueOf(mainPageLogoAtLeft);
        }
    }

    /**
     * @param mainPageLogoAtLeft the mainPageLogoAtLeft to set
     */
    /*package*/
    final void setMainPageLogoAtLeft(boolean mainPageLogoAtLeft) {
        this.mainPageLogoAtLeft = mainPageLogoAtLeft;
    }

    /**
     * @return the mainPageLogoHeight
     */
    public final int getMainPageLogoHeight() {
        return mainPageLogoHeight;
    }

    /**
     * @param mainPageLogoHeight the mainPageLogoHeight to set
     */
    /*package*/
    final void setMainPageLogoHeight(String mainPageLogoHeight) {
        if (notEmpty(mainPageLogoHeight)) {
            this.mainPageLogoHeight = Integer.valueOf(mainPageLogoHeight);
        }
    }

    /**
     * @param mainPageLogoHeight the mainPageLogoHeight to set
     */
    /*package*/
    final void setMainPageLogoHeight(int mainPageLogoHeight) {
        this.mainPageLogoHeight = mainPageLogoHeight;
    }

    /**
     * @return the mainPageLogoWidth
     */
    public final int getMainPageLogoWidth() {
        return mainPageLogoWidth;
    }

    /**
     * @param mainPageLogoWidth the mainPageLogoWidth to set
     */
    /*package*/
    final void setMainPageLogoWidth(String mainPageLogoWidth) {
        if (notEmpty(mainPageLogoWidth)) {
            this.mainPageLogoWidth = Integer.valueOf(mainPageLogoWidth);
        }
    }

    /**
     * @param mainPageLogoWidth the mainPageLogoWidth to set
     */
    /*package*/
    final void setMainPageLogoWidth(int mainPageLogoWidth) {
        this.mainPageLogoWidth = mainPageLogoWidth;
    }

    /**
     * @return the mainPageSizeForced
     */
    public final boolean isMainPageSizeForced() {
        return mainPageSizeForced;
    }

    /**
     * @param mainPageSizeForced the mainPageSizeForced to set
     */
    /*package*/
    final void setMainPageSizeForced(String mainPageSizeForced) {
        if (notEmpty(mainPageSizeForced)) {
            this.mainPageSizeForced = Boolean.valueOf(mainPageSizeForced);
        }
    }

    /**
     * @param mainPageSizeForced the mainPageSizeForced to set
     */
    /*package*/
    final void setMainPageSizeForced(boolean mainPageSizeForced) {
        this.mainPageSizeForced = mainPageSizeForced;
    }

    /**
     * @return the otherPagesLogoHeight
     */
    public final int getOtherPagesLogoHeight() {
        return otherPagesLogoHeight;
    }

    /**
     * @param otherPagesLogoHeight the otherPagesLogoHeight to set
     */
    /*package*/
    final void setOtherPagesLogoHeight(String otherPagesLogoHeight) {
        if (notEmpty(otherPagesLogoHeight)) {
            this.otherPagesLogoHeight = Integer.valueOf(otherPagesLogoHeight);
        }
    }

    /**
     * @param otherPagesLogoHeight the otherPagesLogoHeight to set
     */
    /*package*/
    final void setOtherPagesLogoHeight(int otherPagesLogoHeight) {
        this.otherPagesLogoHeight = otherPagesLogoHeight;
    }

    /**
     * @return the otherPagesLogoWidth
     */
    public final int getOtherPagesLogoWidth() {
        return otherPagesLogoWidth;
    }

    /**
     * @param otherPagesLogoWidth the otherPagesLogoWidth to set
     */
    /*package*/
    final void setOtherPagesLogoWidth(String otherPagesLogoWidth) {
        if (notEmpty(otherPagesLogoWidth)) {
            this.otherPagesLogoWidth = Integer.valueOf(otherPagesLogoWidth);
        }
    }

    /**
     * @param otherPagesLogoWidth the otherPagesLogoWidth to set
     */
    /*package*/
    final void setOtherPagesLogoWidth(int otherPagesLogoWidth) {
        this.otherPagesLogoWidth = otherPagesLogoWidth;
    }

    /**
     * @return the otherPagesSizeForced
     */
    public final boolean isOtherPagesSizeForced() {
        return otherPagesSizeForced;
    }

    /**
     * @param otherPagesSizeForced the otherPagesSizeForced to set
     */
    /*package*/
    final void setOtherPagesSizeForced(String otherPagesSizeForced) {
        if (notEmpty(otherPagesSizeForced)) {
            this.otherPagesSizeForced = Boolean.valueOf(otherPagesSizeForced);
        }
    }

    /**
     * @param otherPagesSizeForced the otherPagesSizeForced to set
     */
    /*package*/
    final void setOtherPagesSizeForced(boolean otherPagesSizeForced) {
        this.otherPagesSizeForced = otherPagesSizeForced;
    }

    public boolean isLogoDisplayedOnMainPage() {
        return logoDisplayedOnMainPage;
    }

    /**
     * @param logoDisplayedOnMainPage the logoDisplayedOnMainPage to set
     */
    /*package*/
    final void setLogoDisplayedOnMainPage(String logoDisplayedOnMainPage) {
        if (notEmpty(logoDisplayedOnMainPage)) {
            this.logoDisplayedOnMainPage = Boolean.valueOf(logoDisplayedOnMainPage);
        }
    }

    /**
     * @param logoDisplayedOnMainPage the logoDisplayedOnMainPage to set
     */
    /*package*/
    final void setLogoDisplayedOnMainPage(boolean logoDisplayedOnMainPage) {
        this.logoDisplayedOnMainPage = logoDisplayedOnMainPage;
    }

    public NodeRef getConfiguredOn() {
        return configurationOn;
    }

    @Override
    protected DefaultLogoConfigurationImpl clone() throws CloneNotSupportedException {
        final DefaultLogoConfigurationImpl clone = (DefaultLogoConfigurationImpl) super.clone();

        if (this.logo != null) {
            clone.logo = new LogoDefinitionImpl();
            clone.logo.setReference(this.logo.getReference());
            clone.logo.setDefinedOn(this.logo.getDefinedOn());
            clone.logo.setDescription(this.logo.getDescription());
            clone.logo.setName(this.logo.getName());
            clone.logo.setTitle(this.logo.getTitle());
        }

        return clone;
    }

    /*package*/
    final void setLogo(
            final NodeRef logoRef,
            final NodeRef definedOn,
            final String name,
            final String title,
            final String description) {
        this.logo = new LogoDefinitionImpl();

        this.logo.setReference(logoRef);
        this.logo.setName(name);
        this.logo.setTitle(title);
        this.logo.setDescription(description);
        this.logo.setDefinedOn(definedOn);
    }

    /*package*/
    final void setMainPageLogoConfig(
            boolean display, int height, int width, boolean sizeForced, boolean logoAtLeft) {
        this.logoDisplayedOnMainPage = display;
        this.mainPageLogoAtLeft = logoAtLeft;
        this.mainPageLogoHeight = height;
        this.mainPageLogoWidth = width;
        this.mainPageSizeForced = sizeForced;
    }

    /*package*/
    final void setOtherPagesLogoConfig(boolean display, int height, int width, boolean sizeForced) {
        this.logoDisplayedOnAllPages = display;
        this.otherPagesLogoHeight = height;
        this.otherPagesLogoWidth = width;
        this.otherPagesSizeForced = sizeForced;
    }

    /**
     * @return the configurationRef
     */
    /*package*/
    final NodeRef getConfigurationRef() {
        return configurationRef;
    }

    /**
     * @return the configurationRef
     */
    /*package*/
    final void setConfigurationRef(final NodeRef nodeRef) {
        configurationRef = nodeRef;
    }

    /**
     * @param configurationRef the configurationRef to set
     */
    /*package*/
    final void setConfigurationRef(final NodeRef configurationOn, final NodeRef configurationRef) {
        this.configurationRef = configurationRef;
        this.configurationOn = configurationOn;
    }

    private boolean notEmpty(String str) {
        return str != null && str.length() > 0;
    }
}
