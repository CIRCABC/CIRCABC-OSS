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
package eu.cec.digit.circabc.web.wai;

import eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.wai.dialog.content.edit.CreateContentBaseDialog.AttachementWrapper;
import eu.cec.digit.circabc.web.wai.wizard.BaseWaiWizard;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.bean.TemplateSupportBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.mail.MessagingException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Stephane Clinckart
 */
public abstract class AbstractMailToUsersBean extends BaseWaiWizard {

    public static final String ATTACHED_FILES = "attached_films";
    private static final Log logger = LogFactory.getLog(AbstractMailToUsersBean.class);
    private static final String MESSAGE_ID_NO_SUBJECT = "mail_no_subject_error";
    private static final String MESSAGE_ID_NO_BODY = "mail_no_body_error";
    /**
     * dialog state
     */
    private String subject = null;
    private String body = null;
    private String template = null;
    private String usingTemplate = null;

    private List<MailWrapper> templates = null;


    private String callBackMethodName;

    /**
     * personService bean reference
     */
    private transient PersonService personService;

    /**
     * AuthorityService bean reference
     */
    private transient AuthorityService authorityService;

    private transient TemplateService templateService;

    private transient TransactionService transactionService;

    private transient NavigationBusinessSrv navigationBusinessSrv;

    /**
     * Initialises the wizard
     */
    @Deprecated
    public void init(final ActionEvent event) {
        // Get the id of the node and the selected version
        final UIActionLink link = (UIActionLink) event.getComponent();
        final Map<String, String> params = link.getParameterMap();
        this.init(params);
    }

    /**
     * Initialises the wizard
     */
    public void init(final Map<String, String> params) {

        super.init(params);

        template = null;
        usingTemplate = null;
        templates = null;
    }

    protected abstract String finishImpl(final FacesContext context, final String outcome)
            throws Exception;

    /**
     * Send an email notification to the specified User authority
     *
     * @param person           Person node representing the user
     * @param node             Node they are invited too
     * @param from             From text message
     * @param extraBodyParams  Parameters added to the mail body defined in the text area (used if
     *                         this.usingTemplate == null)
     * @param extraModelParams Parameters added to the mail template model (used if this.usingTemplate
     *                         != null)
     */
    protected void mailToUser(final NodeRef person, final NodeRef node, final String from,
                              final Map<String, Object> extraModelParams, final Map<String, String> extraBodyParams) {
        final Map<QName, Serializable> personProperties = getNodeService().getProperties(person);
        final String to = (String) personProperties.get(ContentModel.PROP_EMAIL);
        String noReply = getMailService().getNoReplyEmailAddress();

        if (to != null && to.length() != 0) {
            String bodyToSend;

            if (this.usingTemplate != null) {
                final Map<String, Object> model = getMailPreferencesService()
                        .buildDefaultModel(node, person, null);
                model.putAll(extraModelParams);
                final NodeRef templateRef = new NodeRef(Repository.getStoreRef(), this.usingTemplate);
                bodyToSend = getTemplateService()
                        .processTemplate("freemarker", templateRef.toString(), model);
            } else {
                final String tempBody = applyParams(getBody(), extraBodyParams);
                bodyToSend = tempBody.replace("\n", "").replace("\r", "");

                if (extraModelParams.containsKey(ATTACHED_FILES)) {

                    List<AttachementWrapper> listW = (List<AttachementWrapper>) extraModelParams
                            .get(ATTACHED_FILES);

                    if (listW.size() > 0) {

                        bodyToSend += "<h3>" + translate("attachment_links_label") + "</h3>";
                        bodyToSend += "<ul>";

                        for (AttachementWrapper aw : listW) {
                            bodyToSend += "<li>" + buildLinkFromAttachmentWrapper(aw) + "</li>";
                        }
                        bodyToSend += "</ul>";

                    }
                }
            }

            try {
                // Send the message
                getMailService()
                        .send(noReply, to, from, applyParams(getSubject(), extraBodyParams), bodyToSend,
                                isMailHtml(), false);
            } catch (final MessagingException e) {
                // the parameters should be false
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to send email to " + to, e);
                }
            }
        }
    }

    private String buildLinkFromAttachmentWrapper(AttachementWrapper aw) {

        Node n = new Node(aw.getAttachRef());
        String result = "<a href=\"";
        result += WebClientHelper.getGeneratedWaiFullUrl(n, ExtendedURLMode.HTTP_WAI_BROWSE);
        result += "\">" + aw.getName() + "</a>";

        return result;
    }

    protected boolean validateMailData() {
        boolean valid = true;

        if (getSubject() == null || getSubject().trim().length() < 1) {
            valid = false;
            Utils.addErrorMessage(translate(MESSAGE_ID_NO_SUBJECT));
        }
        if (getBody() == null || getBody().trim().length() < 1) {
            valid = false;
            Utils.addErrorMessage(translate(MESSAGE_ID_NO_BODY));
        }

        return valid;
    }

    protected String applyParams(final String body, final Map<String, String> extraBodyParams) {
        String bodyToUpdate = body;
        for (final Entry<String, String> entry : extraBodyParams.entrySet()) {
            if (entry.getValue() != null) {
                bodyToUpdate = bodyToUpdate.replace(entry.getKey(), entry.getValue());
            }
        }
        return bodyToUpdate;
    }

    protected boolean isMailHtml() {
        return true;
    }

    /**
     * Action handler called to insert a template as the email body
     */
    public void insertTemplate(final ActionEvent event) {
        if (this.template != null && !this.template.equals(TemplateSupportBean.NO_SELECTION)) {
            // get the content of the template so the user can get a basic preview of it
            try {
                for (final MailWrapper wrap : templates) {
                    if (wrap.getTemplateNodeRef().getId().equals(this.template)) {
                        String mailBody = wrap.getBody(null);
                        mailBody = cleanBody(mailBody);
                        setBody(mailBody);
                        setSubject(wrap.getSubject(null));

                        break;
                    }
                }

                this.usingTemplate = this.template;
            } catch (final Throwable err) {
                Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, err.getMessage()), err);
            }
        }
    }

    /**
     * Action handler called to discard the template from the email body
     */
    public void discardTemplate(final ActionEvent event) {
        // don't reset the template
        usingTemplate = null;
    }

    /**
     * @return Returns the email body text.
     */
    public String getBody() {
        return this.body;
    }

    /**
     * @param body The email body text to set.
     */
    public void setBody(final String body) {

        this.body = body;
    }

    public String cleanBody(final String bdy) {
        Whitelist basicWhitelist = new Whitelist();
        basicWhitelist.addTags("p", "span", "strong", "b", "i", "u", "br", "sub", "sup", "a");
        basicWhitelist.addAttributes(":all", "style", "href");
        return Jsoup.clean(bdy, basicWhitelist);
    }

    /**
     * @return Returns the email subject text.
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * @param subject The email subject text to set.
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * @return Returns the email template Id
     */
    public String getTemplate() {
        return this.template;
    }

    /**
     * @param template The email template to set.
     */
    public void setTemplate(final String template) {
        this.template = template;
    }

    /**
     * @return Returns if a template has been inserted by a user for email body.
     */
    public String getUsingTemplate() {
        return this.usingTemplate;
    }

    /**
     * @param usingTemplate Template that has been inserted by a user for the email body.
     */
    public void setUsingTemplate(final String usingTemplate) {
        this.usingTemplate = usingTemplate;
    }

    /**
     * @return the list of available Email Templates.
     */
    public List<SelectItem> getEmailTemplates() {

        if (template == null) {
            templates = getMailPreferencesService()
                    .getMailTemplates(getActionNode().getNodeRef(), getMailTemplateDefinition());
        }

        final List<SelectItem> items = new ArrayList<>(templates.size());

        for (final MailWrapper wrap : templates) {
            if (!wrap.isOriginalTemplate()) {
                items.add(new SelectItem(wrap.getTemplateNodeRef().getId(), wrap.getName()));
            }
        }
        return items;
    }

    public String getBuildTextMessage() {
        final RetryingTransactionHelper txnHelper = getTransactionService()
                .getRetryingTransactionHelper();

        final RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
            public String execute() throws Throwable {
                final NodeRef person = getTemplatePerson();
                final MailWrapper mail = getMailPreferencesService()
                        .getDefaultMailTemplate(getActionNode().getNodeRef(), getMailTemplateDefinition());
                final Map<String, Object> model = getMailPreferencesService()
                        .buildDefaultModel(getActionNode().getNodeRef(),
                                person, null);

                model.putAll(getDisplayModelToAdd());
                // remove template noise
                String htmlBody = mail.getBody(model).replace("\n", "").replace("\r", "");

                // the following is not necessary anymore when using tinymce
                // the input text area doesn't escape html. Make the display user friendly.
                //final String textBody = htmlBody.replace("<br />", "\n").replace("<br/>", "\n");

                setBody(htmlBody);
                setSubject(mail.getSubject(model));

                return "true";
            }
        };

        return txnHelper.doInTransaction(callback, false, true);
    }


    protected abstract MailTemplate getMailTemplateDefinition();

    protected abstract Map<String, Object> getDisplayModelToAdd();

    /**
     * @return the personService
     */
    protected final PersonService getPersonService() {
        if (personService == null) {
            personService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getPersonService();
        }
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public final void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    /**
     * @return the authorityService
     */
    protected final AuthorityService getAuthorityService() {
        if (authorityService == null) {
            authorityService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getAuthorityService();
        }
        return authorityService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public final void setAuthorityService(final AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    protected CircabcRootProfileManagerService getCircabcRootProfileManagerService() {
        return getProfileManagerServiceFactory().getCircabcRootProfileManagerService();
    }

    protected CategoryProfileManagerService getCategoryProfileManagerService() {
        return getProfileManagerServiceFactory().getCategoryProfileManagerService();
    }

    protected IGRootProfileManagerService getIGRootProfileManagerService() {
        return getProfileManagerServiceFactory().getIGRootProfileManagerService();
    }

    public boolean isFormProvided() {
        return false;
    }

    public String getCallBackMethodName() {
        return callBackMethodName;
    }

    public void setCallBackMethodName(final String callBackMethodName) {
        this.callBackMethodName = "#{" + callBackMethodName + "}";
    }


    /**
     * @return the templateService
     */
    protected final TemplateService getTemplateService() {
        if (templateService == null) {
            templateService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getTemplateService();
        }
        return templateService;
    }

    /**
     * @param templateService the templateService to set
     */
    public final void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * @return the transactionService
     */
    protected final TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getTransactionService();
        }
        return transactionService;
    }

    /**
     * @param transactionService the transactionService to set
     */
    public final void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * @return
     */
    protected NodeRef getTemplatePerson() {
        final String templateUser = getMailPreferencesService().getTemplateUserDetails().getUserName();
        return getPersonService().getPerson(templateUser);
    }

    /**
     * @return
     */
    protected NodeRef getCurrentPerson() {
        final String currentUsername = AuthenticationUtil.getFullyAuthenticatedUser();
        final NodeRef person = getPersonService().getPerson(currentUsername);
        return person;
    }

    public NavigationBusinessSrv getNavigationBusinessSrv() {
        return navigationBusinessSrv;
    }

    public void setNavigationBusinessSrv(final NavigationBusinessSrv navigationBusinessSrv) {
        this.navigationBusinessSrv = navigationBusinessSrv;
    }

}
