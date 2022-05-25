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
package eu.cec.digit.circabc.web.bean.surveys;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.util.CommonUtils;
import eu.cec.digit.circabc.util.Language;
import eu.cec.digit.circabc.web.ui.component.UISurveyLangs;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.webservice.IpmWebServiceFactory;
import ipm.webservice.IpmService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Bean providing properties and behaviour for the surveys screens.
 *
 * @author Matthieu Sprunck
 */
public class SurveysBean extends BaseWaiDialog implements IContextListener {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "SurveysBean";
    private static final long serialVersionUID = -1233096380811702324L;
    /**
     * The logger
     */
    private static final Log logger = LogFactory.getLog(SurveysBean.class);

    // ------------------------------------------------------------------------------
    // Construction
    /**
     * The base url property's name
     */
    private static final String BASE_URL_PROPERTY = "ipm.baseurl";

    // ------------------------------------------------------------------------------
    // Navigation action event handlers
    /**
     * Parameters that are always sent to IPM
     */
    private static final String PARAM_INTEREST_GROUP = "ig";
    private static final String PARAM_VIRTUAL_CIRCABC = "vc";

    // ------------------------------------------------------------------------------
    // Bean property getters and setters
    private static final String PARAM_SURVEY = "form";

    // ------------------------------------------------------------------------------
    // IContextListener implementation
    private static final String PARAM_LANG = "lang";
    private static final String PARAM_USERNAME = "username";
    /**
     * The IPM path
     */
    private static String baseUrl;
    /**
     * Component reference
     */
    protected UIRichList surveysRichList;
    /**
     * The survey list
     */
    private List<Survey> surveys;
    /**
     * The short name of the current survey
     */
    private String survey;
    /**
     * The selected language for the current survey
     */
    private String lang;
    /**
     * Parameters to send to IPM
     */
    private Map<String, String[]> paramsMap = new HashMap<>();
    /**
     * Tell if we are using the wai pages
     */
    private boolean isInWAI = false;

    /**
     * Default Constructor
     */
    public SurveysBean() {
        // For test
        // surveys = new ArrayList<Survey>();
        // List<String> translations = new ArrayList<String>();
        // translations.add("de");
        // translations.add("en");
        // translations.add("fr");
        // List<String> translations2 = new ArrayList<String>();
        // translations2.add("de");
        // translations2.add("en");
        // Date today = new Date();
        // surveys.add(new Survey("peche", "La peche et vous", "Open",
        // today, today, translations, "en"));
        // surveys.add(new Survey("confiture", "Confiture ou miel?", "Open",
        // today, today, translations, "en"));
        // surveys.add(new Survey("animaux", "Animaux domestiques", "Close",
        // today, today, translations2, "en"));
        // surveys.add(new Survey("Form4RemoteAnonym", "Formulaire de test",
        // "Open", today, today, translations2, "en"));

        UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);

    }

    /**
     * Gets the IPM base url (ex: http://server:8081/ipm/dispatch)
     *
     * @return the ipm base url
     */
    private static String getIpmBaseUrl() {
        if (baseUrl == null) {
            baseUrl = CircabcConfiguration.getProperty(BASE_URL_PROPERTY);
        }
        return baseUrl;
    }

    /**
     * Action called from an Action Link component. Launch the encoding of a survey.
     */
    public void clickSurvey(ActionEvent event) {
        UIActionLink link = (UIActionLink) event.getComponent();
        Map<String, String> params = link.getParameterMap();
        survey = params.get(UISurveyLangs.PARAM_SURVEY);
        lang = params.get(UISurveyLangs.PARAM_LANG);
        // Initialize the map containing the url parameters
        paramsMap.clear();
        if (logger.isDebugEnabled()) {
            logger.debug("Click on the survey " + survey + " with the lang "
                    + lang);
        }
    }

    /**
     * Action called from an Action link component. Close the survey, redirect the user to the survey
     * space
     */
    public void closeSurvey(ActionEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("Close the survey " + survey + " with the lang "
                    + lang);
        }
        survey = null;
        lang = null;
    }

    @SuppressWarnings("unchecked")
    public List<Survey> getSurveys() {
        if (surveys == null || surveys.size() == 0) {
            String uilang = Application.getLanguage(
                    FacesContext.getCurrentInstance()).getLanguage();
            try {
                IpmService ipmService = IpmWebServiceFactory.getIpmService();
                surveys = SurveyHelper.transform(ipmService.getCircabcSurveys(
                        getVirtualCirca(), getInterestGroup(), uilang));
            } catch (Throwable e) {
                surveys = Collections.EMPTY_LIST;
                logger.error("Unable to get the list of surveys from the IPM server", e);
            }
        }
        return surveys;
    }

    // ------------------------------------------------------------------------------
    // Private data

    /**
     * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
     */
    public void contextUpdated() {
        if (logger.isDebugEnabled()) {
            logger.debug("Invalidating surveys components...");
        }

        // clear the value for the list components - will cause re-bind to it's
        // data and refresh
        if (this.surveysRichList != null) {
            this.surveysRichList.setValue(null);
        }

        // reset the list
        this.surveys = null;
    }

    /**
     * Getter for langs file
     *
     * @return the langs
     */
    public List<String> getLangs() throws IOException, SAXException {
        return Language.codesList(CommonUtils.getLanguages());
    }

    public UIRichList getSurveysRichList() {
        return surveysRichList;
    }

    public void setSurveysRichList(UIRichList surveysRichList) {
        this.surveysRichList = surveysRichList;
    }

    /**
     * Getter for lang file
     *
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * Getter for survey file
     *
     * @return the survey
     */
    public String getSurvey() {
        return survey;
    }

    /**
     * Gets the virtual circa name
     *
     * @return the virtual circa name
     */
    public String getVirtualCirca() {
        return getNavigator().getCurrentCategory().getName();
    }

    /**
     * Gets the current interest group name
     *
     * @return the name of the current interest group
     */
    public String getInterestGroup() {
        return getNavigator().getCurrentIGRoot().getName();
    }

    /**
     * Adds the specified map to the parameters map. This parameters are sent to IPM
     */
    @SuppressWarnings("unchecked")
    public void setParameters(Map<String, String[]> parameters) {
        paramsMap.clear();
        paramsMap.putAll(parameters);
    }

    /**
     * Adds the parameters wich are always sent to IPM.
     */
    private Map<String, String[]> getDefaultParameters() {
        User user = Application.getCurrentUser(FacesContext.getCurrentInstance());

        Map<String, String[]> defaultParamsMap = new HashMap<>();
        defaultParamsMap.put(PARAM_INTEREST_GROUP,
                new String[]{getInterestGroup()});
        defaultParamsMap.put(PARAM_VIRTUAL_CIRCABC,
                new String[]{getVirtualCirca()});
        defaultParamsMap.put(PARAM_SURVEY, new String[]{survey});
        defaultParamsMap.put(PARAM_USERNAME, new String[]{user.getUserName()});
        defaultParamsMap.put(PARAM_LANG, new String[]{lang});
        return defaultParamsMap;
    }

    /**
     * Builds the url to call
     *
     * @return the url
     */
    public String getUrl() {
        StringBuilder sb = new StringBuilder();
        Map<String, String[]> params = new HashMap<>();
        params.putAll(getDefaultParameters());
        params.putAll(paramsMap);
        String[] values = null;
        String name = null;
        for (Entry<String, String[]> entry : params.entrySet()) {
            values = entry.getValue();
            name = entry.getKey();
            for (String value : values) {
                if (value != null) {
                    if (sb.length() == 0) {
                        sb.append('?');
                    } else {
                        sb.append('&');
                    }
                    sb.append(name);
                    sb.append('=');
                    try {
                        sb.append(URLEncoder.encode(value, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        logger.error("Unsupported character set", e);
                    }
                }

            }
        }
        sb.insert(0, getIpmBaseUrl());
        return sb.toString();
    }

    /**
     * Getter for wai interface state use
     *
     * @return True if wai interface is in use
     */
    public boolean isInWAI() {
        return isInWAI;
    }

    /**
     * Setter for use state of wai interface
     *
     * @param isInWAI The state to set
     */
    public void setInWAI(boolean isInWAI) {
        this.isInWAI = isInWAI;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.app.context.IContextListener#areaChanged()
     */
    public void areaChanged() {

    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.app.context.IContextListener#spaceChanged()
     */
    public void spaceChanged() {

    }

    @Override
    public boolean getFinishButtonDisabled() {
        return true;
    }

    public String getBrowserTitle() {
        return "*****************************************";
    }

    public String getPageIconAltText() {
        return "*****************************************";
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        // its not a true dialog yet...
        return outcome;
    }
}
