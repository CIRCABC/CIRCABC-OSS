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

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.bean.ml.AddTranslationBean;
import eu.cec.digit.circabc.web.wai.bean.ml.MachineTranslationContentDialog;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Dialog bean to make a node multilingual for the WAI
 *
 * @author Yanick PIgnot
 */
public class MakeMultilingualDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "CircabcMakeMultilingualDialog";
    private static final String NO_LANGUAGE_SELECTED = "null";
    private static final long serialVersionUID = -5462740078894071426L;
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(MakeMultilingualDialog.class);
    private String author;
    private String language;

    private boolean machineTranslationEnabled;


    private String actionAfterClose;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("Node id parameter is mandatory");
        }

        if (this.parameters != null) {
            final NodeRef nodeRef = getActionNode().getNodeRef();

            //propose the author and the language of the content for the properties of the MLContainer
            if (getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_AUTHOR) == true
                    && getNodeService().getProperty(nodeRef, ContentModel.PROP_AUTHOR) != null) {
                setAuthor((String) getNodeService().getProperty(nodeRef, ContentModel.PROP_AUTHOR));
            } else {
                setAuthor("");
            }

            if (getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_LOCALIZED) == true
                    && getNodeService().getProperty(nodeRef, ContentModel.PROP_LOCALE) != null) {
                setLanguage(
                        ((Locale) getNodeService().getProperty(nodeRef, ContentModel.PROP_LOCALE)).toString());
            } else {
                setLanguage(null);
            }
        }
        actionAfterClose = "nothing";

        machineTranslationEnabled = false;
        if (!"".equals(CircabcConfiguration.getProperty(CircabcConfiguration.MT_ENABLED))) {
            machineTranslationEnabled = Boolean
                    .parseBoolean(CircabcConfiguration.getProperty(CircabcConfiguration.MT_ENABLED));
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        isFinished = true;

        if (getLanguage() == null || getAuthor() == null) {
            // error - Piracy
            if (logger.isErrorEnabled()) {
                logger.error("finishImpl : Piracy warning : null value from form");
            }
            return null;
        }
        boolean isError = false;

        if (getLanguage() == null || getLanguage().equalsIgnoreCase(NO_LANGUAGE_SELECTED)) {
            // Invalid data for language
            Utils.addStatusMessage(FacesMessage.SEVERITY_ERROR,
                    Application.getMessage(context, "make_multilingual_error_language"));
            isError = true;
        }
        if (getAuthor().equals("")) {
            // Invalid data for author
            Utils.addStatusMessage(FacesMessage.SEVERITY_ERROR,
                    Application.getMessage(context, "make_multilingual_error_author"));
            isError = true;
        }

        // Test if previous error
        if (isError) {
            this.isFinished = false;
            return null;
        } else {
            // Added to warn about integrity errors instead of throwing the
            // exception (Alfresco 4)
            if (!IntegrityChecker.isWarnInTransaction()) {
                IntegrityChecker.setWarnInTransaction();
            }

            //Do the jobs
            final Locale locale = I18NUtil.parseLocale(getLanguage());

            final NodeRef nodeRef = getActionNode().getNodeRef();

            //https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2290
            getNodeService().removeAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE);

            // make this node multilingual
            getMultilingualContentService().makeTranslation(nodeRef, locale);
            final NodeRef mlContainer = getMultilingualContentService().getTranslationContainer(nodeRef);

            // if the author of the node is not set, set it with the default author name of
            // the new ML Container
            String nodeAuthor = (String) getNodeService().getProperty(nodeRef, ContentModel.PROP_AUTHOR);

            if (nodeAuthor == null || nodeAuthor.length() < 1) {
                getNodeService().setProperty(nodeRef, ContentModel.PROP_AUTHOR, getAuthor());
            }

            // set properties of the ml container
            getNodeService().setProperty(mlContainer, ContentModel.PROP_AUTHOR, getAuthor());

            //https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2290
            getNodeService().addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
            return outcome;
        }
    }

    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        super.doPostCommitProcessing(context, outcome);

        if (actionAfterClose.equals("machineTranslation")) {
            final Map<String, String> parameters = new HashMap<>(3);
            parameters.put("id", getActionNode().getId());
            parameters.put("service", "Machine translation");
            parameters.put("activity", "Document translation");

            Beans.getMachineTranslationContentBean().init(parameters);
            return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                    + CircabcNavigationHandler.OUTCOME_SEPARATOR
                    + CircabcNavigationHandler.WAI_DIALOG_PREFIX
                    + MachineTranslationContentDialog.DIALOG_NAME;
        }

        if (actionAfterClose.equals("addTranslation")) {
            final Map<String, String> parameters = new HashMap<>(3);
            parameters.put("id", getActionNode().getId());
            parameters.put("service", "Library");
            parameters.put("activity", "AddTranslation");

            Beans.getAddTranslationBean().init(parameters);

            return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                    + CircabcNavigationHandler.OUTCOME_SEPARATOR
                    + CircabcNavigationHandler.WAI_PREFIX
                    + AddTranslationBean.DIALOG_NAME;
        } else {
            // close the dialog
            return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME;
        }
    }

    /**
     * @return the complete list of available languages for the multilinguism
     */
    public SelectItem[] getFilterLanguages() {
        return getUserPreferencesBean().getContentFilterLanguages(false);
    }

    // ------------------------------------------------------------------------------
    // Bean getters and setters


    public String getBrowserTitle() {
        return Application.getMessage(FacesContext.getCurrentInstance(), "make_multilingual_title_wai");
    }

    public String getPageIconAltText() {
        return Application
                .getMessage(FacesContext.getCurrentInstance(), "make_multilingual_icon_tooltip");
    }


    /**
     * @return the author
     */
    public final String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public final void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the language
     */
    public final String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public final void setLanguage(String language) {
        this.language = language;
    }

    public String getActionAfterClose() {
        return actionAfterClose;
    }

    public void setActionAfterClose(String actionAfterClose) {
        this.actionAfterClose = actionAfterClose;
    }

    public boolean isMachineTranslationEnabled() {
        return machineTranslationEnabled && CircabcConfig.ENT;
    }

    public void setMachineTranslationEnabled(boolean machineTranslationEnabled) {
        this.machineTranslationEnabled = machineTranslationEnabled;
    }


}
