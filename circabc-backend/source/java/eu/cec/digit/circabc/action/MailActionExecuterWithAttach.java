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
package eu.cec.digit.circabc.action;

import eu.cec.digit.circabc.service.mail.MailService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.template.DateCompareMethod;
import org.alfresco.repo.template.HasAspectMethod;
import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Mail action executor implementation.
 *
 * @author Ph Dubois
 * @author Roy Wetherall
 */
public class MailActionExecuterWithAttach extends ActionExecuterAbstractBase {

    /**
     * Action executor constants
     */
    public static final String NAME = "mailwithattach";
    public static final String PARAM_TO = "to";
    public static final String PARAM_TO_MANY = "to_many";
    public static final String PARAM_SUBJECT = "subject";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_FROM = "from";
    public static final String PARAM_TEMPLATE = "template";
    public static final String PARAM_SENDDOCUMENT = "senddocument";
    /**
     * From address
     */
    public static final String FROM_ADDRESS = "alfresco_repository@alfresco.org";
    private static final Log logger = LogFactory.getLog(MailActionExecuter.class);
    /**
     * The Template service
     */
    private TemplateService templateService;

    /**
     * The Person service
     */
    private PersonService personService;

    /**
     * The Authentication service
     */
    private AuthenticationService authService;

    /**
     * The Node Service
     */
    private NodeService nodeService;

    /**
     * The Authority Service
     */
    private AuthorityService authorityService;

    /**
     * The mail service
     */
    private MailService mailService;

    /**
     * The Service registry
     */
    private ServiceRegistry serviceRegistry;


    /**
     * @param templateService the TemplateService
     */
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * @param personService the PersonService
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @param authService the AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * @param serviceRegistry the ServiceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @param authorityService the AuthorityService
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @param nodeService the NodeService to set.
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the mailService
     */
    public MailService getMailService() {
        return mailService;
    }

    /**
     * @param mailService the mailService to set
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @Override
    protected void executeImpl(final Action ruleAction, final NodeRef actionedUponNodeRef) {
        final List<String> tos = new ArrayList<>();

        try {
            final String to = (String) ruleAction.getParameterValue(PARAM_TO);
            if (to != null && to.length() != 0) {
                tos.add(to);
            } else {
                // see if multiple recipients have been supplied - as a list of authorities
                final List<String> authorities = (List<String>) ruleAction.getParameterValue(PARAM_TO_MANY);

                if (authorities != null && authorities.size() != 0) {
                    AuthorityType authType;
                    NodeRef person;
                    String address;
                    Set<String> users;
                    for (final String authority : authorities) {
                        authType = AuthorityType.getAuthorityType(authority);

                        if (authType.equals(AuthorityType.USER)) {
                            if (this.personService.personExists(authority)) {
                                person = this.personService.getPerson(authority);

                                address = (String) this.nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                                if (address != null && address.length() != 0) {
                                    tos.add(to);
                                }
                            }
                        } else if (authType.equals(AuthorityType.GROUP)) {
                            if (this.authorityService.authorityExists(authority)) {
                                // else notify all members of the group
                                users = this.authorityService
                                        .getContainedAuthorities(AuthorityType.USER, authority, false);

                                for (final String userAuth : users) {
                                    if (this.personService.personExists(userAuth)) {
                                        person = this.personService.getPerson(authority);
                                        address = (String) this.nodeService
                                                .getProperty(person, ContentModel.PROP_EMAIL);
                                        if (address != null && address.length() != 0) {
                                            tos.add(address);
                                        }
                                    }
                                }
                            } else {
                                if (logger.isErrorEnabled()) {
                                    logger.error("Authority does not exists: " + authority);
                                }
                            }
                        }
                    }
                }
            }

            final String subject = ((String) ruleAction.getParameterValue(PARAM_SUBJECT));

            // See if an email template has been specified
            String text = null;
            // NodeRef templateRef = (NodeRef) ruleAction.getParameterValue(PARAM_TEMPLATE);
            final Object templateRef = ruleAction.getParameterValue(PARAM_TEMPLATE);
            if (templateRef != null && !templateRef.toString().equals("none")) {
                // build the email template model
                final Map<String, Object> model = createEmailTemplateModel(actionedUponNodeRef);

                // process the template against the model
                text = templateService.processTemplate("freemarker", templateRef.toString(), model);
            }

            // set the text body of the message
            if (text == null) {
                text = (String) ruleAction.getParameterValue(PARAM_TEXT);
            }

            // set the from address - use the default if not set
            String from = getMailService().getNoReplyEmailAddress();
            if (from == null) {
                from = FROM_ADDRESS;
            }

            final Boolean senddocument = (Boolean) ruleAction.getParameterValue(PARAM_SENDDOCUMENT);

            if (senddocument) {
                getMailService().sendNode(actionedUponNodeRef, from, tos, null, subject, text, true);
            } else {
                getMailService().send(from, tos, null, subject, text, true, false);
            }
        } catch (final Throwable e) {
            if (logger.isErrorEnabled()) {
                // don't stop the action but let admins know email is not getting sent
                logger
                        .error("Failed to send email to " + (String) ruleAction.getParameterValue(PARAM_TO), e);
            }
        }
    }

    /**
     * @param ref The node representing the current document ref
     * @return Model map for email templates
     */
    private Map<String, Object> createEmailTemplateModel(final NodeRef ref) {
        final Map<String, Object> model = new HashMap<>(8, 1.0f);

        final NodeRef person = personService.getPerson(authService.getCurrentUserName());
        model.put("person", new TemplateNode(person, serviceRegistry, null));
        model.put("document", new TemplateNode(ref, serviceRegistry, null));
        final NodeRef parent = serviceRegistry.getNodeService().getPrimaryParent(ref).getParentRef();
        model.put("space", new TemplateNode(parent, serviceRegistry, null));

        // current date/time is useful to have and isn't supplied by FreeMarker
        // by default
        model.put("date", new Date());

        // add custom method objects
        model.put("hasAspect", new HasAspectMethod());
        model.put("message", new I18NMessageMethod());
        model.put("dateCompare", new DateCompareMethod());

        return model;
    }

    /**
     * Add the parameter definitions
     */
    protected void addParameterDefinitions(final List<ParameterDefinition> paramList) {
        paramList
                .add(new ParameterDefinitionImpl(PARAM_TO,
                        DataTypeDefinition.TEXT, false,
                        getParamDisplayLabel(PARAM_TO)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TO_MANY,
                DataTypeDefinition.ANY, false,
                getParamDisplayLabel(PARAM_TO_MANY)));
        paramList.add(new ParameterDefinitionImpl(PARAM_SENDDOCUMENT,
                DataTypeDefinition.BOOLEAN, false,
                getParamDisplayLabel(PARAM_SENDDOCUMENT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_SUBJECT,
                DataTypeDefinition.TEXT, true,
                getParamDisplayLabel(PARAM_SUBJECT)));
        paramList
                .add(new ParameterDefinitionImpl(PARAM_TEXT,
                        DataTypeDefinition.TEXT, true,
                        getParamDisplayLabel(PARAM_TEXT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FROM,
                DataTypeDefinition.TEXT, false,
                getParamDisplayLabel(PARAM_FROM)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE,
                DataTypeDefinition.NODE_REF, false,
                getParamDisplayLabel(PARAM_TEMPLATE)));
    }
}
