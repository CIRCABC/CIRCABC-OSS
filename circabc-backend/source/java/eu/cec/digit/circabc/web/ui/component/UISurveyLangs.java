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
package eu.cec.digit.circabc.web.ui.component;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.util.CommonUtils;
import eu.cec.digit.circabc.util.Language;
import eu.cec.digit.circabc.web.bean.surveys.Survey;
import eu.cec.digit.circabc.web.ui.component.evaluator.ListContainsEvaluator;
import eu.cec.digit.circabc.web.ui.tag.ListContainsEvaluatorTag;
import eu.cec.digit.circabc.web.ui.tag.SurveyLangsTag;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import java.io.IOException;
import java.util.Map;

/**
 * Component to display the list of translations for a survey.
 *
 * @author Matthieu Sprunck
 * @author Guillaume
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 SelfRenderingComponent was moved to Spring.
 * ConstantMethodBinding was removed. This class seems to be developed for CircaBC
 */
public class UISurveyLangs extends SelfRenderingComponent {

    //  ------------------------------------------------------------------------------
    // Component implementation

    public static final String PARAM_LANG = "lang";
    public static final String PARAM_SURVEY = "survey";
    public static final String SURVEY_CONTEXT = "contextSurvey";
    public static final String ACTION_TOOLTIP = "surveys_action_tooltip";
    private static final Log logger = LogFactory.getLog(UISurveyLangs.class);
    private static final String COMPONENT_ACTIONLINK = "org.alfresco.faces.ActionLink";
    private static final String RENDERER_ACTIONLINK = "org.alfresco.faces.ActionLinkRenderer";
    private static final String COMPONENT_ACTIONLINK_WAI = "eu.cec.digit.circabc.faces.ActionLink";
    private static final String RENDERER_ACTIONLINK_WAI = "eu.cec.digit.circabc.faces.ActionLinkRenderer";
    private static final Class ACTION_CLASS_ARGS[] = {javax.faces.event.ActionEvent.class};
    private static final String ACTION_LISTENER = "#{SurveysBean.clickSurvey}";
    private static final String ACTION_OUTCOME = "surveyEncoder";
    private static final String ACTION_OUTCOME_WAI = "surveyEncoderWAI";
    private static final String COMPONENT_LISTEVAL = "eu.cec.digit.circabc.faces.ListContainsEvaluator";
    private static short id = 0;
    /**
     * The survey
     */
    private Survey value;
    /**
     * The wai status
     */
    private Boolean wai;

    /**
     * @return a unique ID for a JSF component
     */
    private static String createUniqueId() {
        return "_UISurveyLangs_" + Short.toString(++id);
    }

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    @Override
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.SurveyLangs";
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (isRendered() == false) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("encodeBegin() for <circa:surveyLangs/> Id: " + getId());
        }

        // put the context object into the requestMap so it is accessable
        // by any child component value binding expressions
        Map requestMap = getFacesContext().getExternalContext().getRequestMap();
        requestMap.put(SURVEY_CONTEXT, getValue());

        if (getChildCount() != 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("---already built component tree for langs.");
            }
            return;
        }

        //SurveysBean surveysBean = (SurveysBean) FacesHelper.getManagedBean(context, SurveysBean.BEAN_NAME);

        buildLinks(context);
        //surveysBean.setInWAI(true);
    }

    /**
     * @see javax.faces.component.UIComponentBase#getRendersChildren()
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
     */
    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        for (Object o : getChildren()) {
            UIComponent child = (UIComponent) o;
            Utils.encodeRecursive(context, child);
        }
    }

    /**
     * Builds Wwai compliant links as reusable UIActionLink components.
     */
    @SuppressWarnings("unchecked")
    private void buildLinks(FacesContext context) {
        Application facesApp = context.getApplication();

        // Tuen perf
        ListContainsEvaluator evaluator = null;
        HtmlOutputLink control = null;
        HtmlOutputText control2 = null;

        String ipmBaseUrl = CircabcConfiguration.getProperties().getProperty("ipm.baseurl");

        for (String code : Language.codesList(CommonUtils.getLanguages())) {
            // Create the evaluator which check if the translation is available for the current survey
            evaluator = (ListContainsEvaluator) facesApp.createComponent(COMPONENT_LISTEVAL);
            evaluator.setValueBinding(ListContainsEvaluatorTag.ATTR_LIST,
                    facesApp.createValueBinding("#{" + SURVEY_CONTEXT + ".translations}"));
            evaluator.setValue(code);
            evaluator.setId(createUniqueId() + "eval");

            getChildren().add(evaluator);

            control = (HtmlOutputLink) facesApp.createComponent(HtmlOutputLink.COMPONENT_TYPE);
            control.setValueBinding("value", facesApp
                    .createValueBinding(ipmBaseUrl + "?form=#{" + SURVEY_CONTEXT + ".name}&lang=" + code));
            control.setTarget("blank");
            //control.setValue(ipmBaseUrl + "dispatch?form=" + "&nbsp;lang="+ code);
            control2 = (HtmlOutputText) facesApp.createComponent(HtmlOutputText.COMPONENT_TYPE);
            control2.setValue(code);
            control.getChildren().add(control2);
            evaluator.getChildren().add(control);
            HtmlOutputText control3 = (HtmlOutputText) facesApp
                    .createComponent(HtmlOutputText.COMPONENT_TYPE);
            control3.setValue(" ");
            evaluator.getChildren().add(control3);
        }
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
     */
    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("encodeEnd() for <circa:surveyLangs/> Id: " + getId());
        }

        Map requestMap = getFacesContext().getExternalContext().getRequestMap();
        requestMap.remove(SURVEY_CONTEXT);
    }

    /**
     * Get the value (for this component the value is an object used as the DataModel)
     *
     * @return the value
     */
    public Survey getValue() {
        ValueBinding vb = getValueBinding(SurveyLangsTag.ATTR_VALUE);
        if (vb != null) {
            this.value = (Survey) vb.getValue(getFacesContext());
        }
        return this.value;
    }

    //  ------------------------------------------------------------------------------
    // Private data

    /**
     * Set the value
     *
     * @param value the value
     */
    public void setValue(Survey value) {
        this.value = value;
        if (logger.isDebugEnabled()) {
            logger.debug("value setted " + value);
        }
    }

    /**
     * Get the wai status
     *
     * @return the wai status
     */
    public boolean getWai() {
        ValueBinding vb = getValueBinding(SurveyLangsTag.ATTR_WAI);
        if (vb != null) {
            this.wai = (Boolean) vb.getValue(getFacesContext());
        }

        if (this.wai != null) {
            return this.wai;
        } else {
            // return default
            return false;
        }
    }

    /**
     * Set the wai status
     *
     * @param wai True if compoment should use a wai compliant renderer
     */
    public void setWai(boolean wai) {
        this.wai = wai;
    }
}
