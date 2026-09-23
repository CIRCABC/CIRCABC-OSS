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
package eu.europa.ec.digit.circabc.rest.service.mail;

import eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.CustomizationException;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link MailPreferencesService}.
 *
 * <p>This service manages the customizable e-mail templates and related visual assets (header
 * logos, disclaimer logo) that CIRCABC uses to render notification e-mails. Templates are stored as
 * FreeMarker ({@code .ftl}) files inside the Alfresco Data Dictionary customization structure, under
 * a {@code templates/mails/&lt;template-directory&gt;} hierarchy, and can be provided in several
 * languages through Alfresco's multilingual content support.
 *
 * <p>Besides adding, replacing and retrieving templates, the service is responsible for building the
 * default FreeMarker model used when rendering a mail (current user, target node, surrounding
 * CIRCABC structure such as the Interest Group and Category, dates, image resolver, etc.).
 *
 * @author Yanick Pignot
 */
public class MailPreferencesServiceImpl implements MailPreferencesService {

  /** Parameter-check message used when validating a mandatory mail template argument. */
  private static final String THE_MAIL_TEMPLATE = "The mail template";
  /** Parameter-check message used when validating a mandatory node reference argument. */
  private static final String THE_NODE_REFERENCE = "The node reference";
  /** Name of the customization sub-folder holding header assets. */
  private static final String HEADER = "header";
  /** Name of the customization sub-folder holding logo images. */
  private static final String LOGO_FOLDER = "logo";
  /** Name of the customization sub-folder holding disclaimer assets. */
  private static final String DISCLAMER_FOLDER = "disclamer";
  /** File extension used by FreeMarker template files. */
  private static final String FTL_EXTENSION = ".ftl";
  /**
   * The folder name in which all templates are inclue (welcome page, mail, icons, list elements)
   */
  private static final String TEMPLATES_ROOT_FOLDER = "templates";

  /** Name of the customization sub-folder that groups all mail templates. */
  private static final String MAILS_TEMPLATE_FOLDER = "mails";
  /** Common suffix appended to error messages hinting at a stale customization folder. */
  private static final String DATA_DICTIONARY_CUSTOMIZATION =
    ". Is the datadictionary customization folder up to date?";

  /** Service used to locate and create customization files/folders on a configurable node. */
  @Autowired
  private NodePreferencesService nodePreferencesService;

  /** Alfresco service used to read and write node properties and associations. */
  @Autowired
  private NodeService nodeService;

  /** Alfresco service used to process FreeMarker templates. */
  @Autowired
  private TemplateService templateService;

  /** Alfresco service used to resolve person nodes from user names. */
  @Autowired
  private PersonService personService;

  /** Alfresco service used to inspect the content model type hierarchy. */
  @Autowired
  private DictionaryService dictionaryService;

  /** CIRCABC API used to resolve well-known repository nodes (Company Home, CIRCABC root, etc.). */
  @Autowired
  private CircabcApi circabcApi;

  /** Alfresco service used to manage multilingual (translated) template files. */
  @Autowired
  private MultilingualContentService multilingualContentService;

  /** CIRCABC configuration, used to enrich the template model with application-level values. */
  @Autowired
  private CircabcConfig circabcConfig;

  /** Utility helper used to resolve the current Interest Group and Category of a node. */
  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Adds a new mail template file (in a given language) to the customization structure of a node.
   *
   * <p>If the target node is not yet configurable it is made configurable first. When a template
   * with the same name already exists, the new file is registered as an additional language
   * translation of the existing pivot translation, generating a unique file name if required;
   * otherwise a brand new multilingual template is created. The provided subject is stored as the
   * {@code cm:title} property of the created file.
   *
   * @param ref the node whose customization structure receives the template; must not be {@code null}
   * @param forTemplate the mail template type determining the target sub-folder; must not be {@code null}
   * @param name the template file name (a {@code .ftl} extension is appended if missing); must not be blank
   * @param body the FreeMarker content of the template; must not be blank
   * @param subject the mail subject, stored as the file title; must not be blank
   * @param language the locale of this template variant; must not be {@code null}
   * @throws CustomizationException if a template with the same name and language already exists
   * @throws IllegalStateException if the template cannot be created in the customization folder
   */
  public void addMailTemplate(
    final NodeRef ref,
    final MailTemplate forTemplate,
    final String name,
    final String body,
    final String subject,
    final Locale language
  ) throws CustomizationException {
    ParameterCheck.mandatory(THE_NODE_REFERENCE, ref);
    ParameterCheck.mandatory(THE_MAIL_TEMPLATE, forTemplate);
    ParameterCheck.mandatoryString("The mail name", name);
    ParameterCheck.mandatoryString("The mail body", body);
    ParameterCheck.mandatoryString("The mail subject", subject);
    ParameterCheck.mandatory("The mail language", language);

    final NodeRef mailRef;
    try {
      if (!nodePreferencesService.isNodeConfigurable(ref)) {
        nodePreferencesService.makeConfigurable(ref);
      }

      final String cleanName;
      if (!name.endsWith(FTL_EXTENSION)) {
        cleanName = name + FTL_EXTENSION;
      } else {
        cleanName = name;
      }

      if (
        nodePreferencesService.customizationFileExists(
          ref,
          TEMPLATES_ROOT_FOLDER,
          MAILS_TEMPLATE_FOLDER,
          forTemplate.getTemplateDirectoryName(),
          cleanName
        )
      ) {
        final NodeRef existingRef = nodePreferencesService.getCustomization(
          ref,
          TEMPLATES_ROOT_FOLDER,
          MAILS_TEMPLATE_FOLDER,
          forTemplate.getTemplateDirectoryName(),
          name
        );

        Map<Locale, NodeRef> translations =
          multilingualContentService.getTranslations(existingRef);
        if (translations.containsKey(language)) {
          throw new CustomizationException(
            "A template with name " +
              name +
              " and language " +
              language +
              " already exists."
          );
        }

        final NodeRef pivot = multilingualContentService.getPivotTranslation(
          existingRef
        );

        int tries = -1;
        String translationName = cleanName;
        while (
          nodePreferencesService.customizationFileExists(
            ref,
            TEMPLATES_ROOT_FOLDER,
            MAILS_TEMPLATE_FOLDER,
            forTemplate.getTemplateDirectoryName(),
            name
          )
        ) {
          translationName = computeName(translationName, ++tries, language);
        }

        mailRef = nodePreferencesService.addCustomizationFile(
          ref,
          TEMPLATES_ROOT_FOLDER,
          MAILS_TEMPLATE_FOLDER,
          forTemplate.getTemplateDirectoryName(),
          translationName,
          body
        );

        multilingualContentService.addTranslation(mailRef, pivot, language);
      } else {
        mailRef = nodePreferencesService.addCustomizationFile(
          ref,
          TEMPLATES_ROOT_FOLDER,
          MAILS_TEMPLATE_FOLDER,
          forTemplate.getTemplateDirectoryName(),
          cleanName,
          body
        );

        multilingualContentService.makeTranslation(mailRef, language);
      }

      nodeService.setProperty(mailRef, ContentModel.PROP_TITLE, subject);
    } catch (CustomizationException e) {
      throw new IllegalStateException(
        "Problem during adding mail with name " +
          forTemplate.getTemplateDirectoryName() +
          "/" +
          name +
          DATA_DICTIONARY_CUSTOMIZATION
      );
    }
  }

  /**
   * Replaces the default mail template of a given type with a new body, subject and language.
   *
   * <p>Delegates to {@link #addMailTemplate(NodeRef, MailTemplate, String, String, String, Locale)}
   * using the default template name of the supplied template type.
   *
   * @param ref the node whose customization structure holds the template; must not be {@code null}
   * @param forTemplate the mail template type whose default template is replaced; must not be {@code null}
   * @param body the new FreeMarker content of the template; must not be blank
   * @param subject the new mail subject, stored as the file title; must not be blank
   * @param language the locale of the template variant; must not be {@code null}
   * @throws CustomizationException if the default template cannot be replaced
   */
  public void replaceDefaultMailTemplate(
    final NodeRef ref,
    final MailTemplate forTemplate,
    final String body,
    final String subject,
    final Locale language
  ) throws CustomizationException {
    addMailTemplate(
      ref,
      forTemplate,
      forTemplate.getDefaultTemplateName(),
      body,
      subject,
      language
    );
  }

  /**
   * Returns all mail templates of a given type defined on a node's customization structure.
   *
   * <p>Each returned {@link MailWrapper} represents a single logical template, aggregating its
   * language translations. Non-pivot translations are collapsed into their pivot wrapper and are not
   * returned as separate entries.
   *
   * @param ref the node whose customization structure is inspected; must not be {@code null}
   * @param mailTemplate the mail template type determining the sub-folder to list; must not be {@code null}
   * @return the list of mail template wrappers found (possibly empty)
   * @throws IllegalStateException if the templates cannot be read from the customization folder
   */
  public List<MailWrapper> getMailTemplates(
    final NodeRef ref,
    final MailTemplate mailTemplate
  ) {
    ParameterCheck.mandatory(THE_NODE_REFERENCE, ref);
    ParameterCheck.mandatory(THE_MAIL_TEMPLATE, mailTemplate);

    try {
      final List<NodeRef> templatesNodes =
        nodePreferencesService.getConfigurationFiles(
          ref,
          TEMPLATES_ROOT_FOLDER,
          MAILS_TEMPLATE_FOLDER,
          mailTemplate.getTemplateDirectoryName()
        );

      final Map<NodeRef, MailWrapper> mailMap = HashMap.newHashMap(
        templatesNodes.size()
      );
      MailWrapper wrapper;
      for (final NodeRef nodeRef : templatesNodes) {
        wrapper = computeWrapper(mailTemplate, nodeRef);

        if (wrapper != null) {
          mailMap.put(nodeRef, wrapper);
        }
      }

      final List<MailWrapper> mails = new ArrayList<>(mailMap.size());
      mails.addAll(mailMap.values());

      return mails;
    } catch (CustomizationException ex) {
      throw new IllegalStateException(
        "Problem during getting mails in folder: " +
          mailTemplate.getTemplateDirectoryName() +
          DATA_DICTIONARY_CUSTOMIZATION,
        ex
      );
    }
  }

  /**
   * Builds a {@link MailWrapper} for a single template node, taking multilingual state into account.
   *
   * <p>For a non-translated node a plain single-language wrapper is created. For the pivot
   * translation of a multilingual node a wrapper aggregating all translations is created. For a
   * non-pivot translation {@code null} is returned, since it is already represented through its
   * pivot.
   *
   * @param mailTemplate the mail template type the node belongs to
   * @param nodeRef the template node to wrap
   * @return the corresponding wrapper, or {@code null} if the node is a non-pivot translation
   * @throws InvalidNodeRefException if the node reference is invalid
   */
  private MailWrapper computeWrapper(
    final MailTemplate mailTemplate,
    final NodeRef nodeRef
  ) throws InvalidNodeRefException {
    MailWrapper wrapper;
    if (!multilingualContentService.isTranslation(nodeRef)) {
      wrapper = new MailWrapperImpl(
        nodeRef,
        mailTemplate,
        templateService,
        nodeService
      );
    } else if (
      multilingualContentService.getPivotTranslation(nodeRef).equals(nodeRef)
    ) {
      final Locale pivot = (Locale) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_LOCALE
      );
      wrapper = new MailWrapperImpl(
        multilingualContentService.getTranslations(nodeRef),
        mailTemplate,
        templateService,
        nodeService,
        pivot
      );
    } else {
      // added throught the pivot.
      wrapper = null;
    }
    return wrapper;
  }

  /**
   * Returns the default mail template of a given type for a node.
   *
   * <p>The default configuration file is resolved and, if it is a translation, its pivot is used to
   * build the wrapper.
   *
   * @param ref the node whose customization structure is inspected; must not be {@code null}
   * @param mailTemplate the mail template type determining the sub-folder; must not be {@code null}
   * @return the wrapper for the default template of the given type
   * @throws IllegalStateException if no default template exists or it cannot be read
   */
  public MailWrapper getDefaultMailTemplate(
    final NodeRef ref,
    final MailTemplate mailTemplate
  ) {
    ParameterCheck.mandatory(THE_NODE_REFERENCE, ref);
    ParameterCheck.mandatory(THE_MAIL_TEMPLATE, mailTemplate);

    NodeRef templateNodeRef;
    try {
      templateNodeRef = nodePreferencesService.getDefaultConfigurationFile(
        ref,
        TEMPLATES_ROOT_FOLDER,
        MAILS_TEMPLATE_FOLDER,
        mailTemplate.getTemplateDirectoryName()
      );

      // should never occurs...
      if (templateNodeRef == null) {
        throw new CustomizationException(
          "No default mail mail template in folder: " +
            mailTemplate.getTemplateDirectoryName() +
            DATA_DICTIONARY_CUSTOMIZATION
        );
      }

      if (multilingualContentService.isTranslation(templateNodeRef)) {
        templateNodeRef = multilingualContentService.getPivotTranslation(
          templateNodeRef
        );
      }

      return computeWrapper(mailTemplate, templateNodeRef);
    } catch (CustomizationException ex) {
      throw new IllegalStateException(
        "Problem during getting mails in folder: " +
          mailTemplate.getTemplateDirectoryName() +
          DATA_DICTIONARY_CUSTOMIZATION,
        ex
      );
    }
  }

  /**
   * Builds the default FreeMarker model used to render a mail template.
   *
   * <p>The model is populated with the application name, an optional image resolver, the current
   * date, the current user ({@code me}), the target person, and—when a context node is supplied—the
   * node itself together with the surrounding CIRCABC structure (document/space, Company Home,
   * CIRCABC root, Interest Group and Category).
   *
   * @param currentRef the context node providing document/space and structural information, or {@code null}
   * @param otherPerson the person the mail concerns; falls back to the current user when {@code null}
   * @param imageResolver the resolver used to render images in the template, or {@code null}
   * @return the mutable model map to be passed to the template engine
   */
  public Map<String, Object> buildDefaultModel(
    final NodeRef currentRef,
    final NodeRef otherPerson,
    final TemplateImageResolver imageResolver
  ) {
    final Map<String, Object> model = new HashMap<>(12, 1.0f);

    circabcConfig.addApplicationNameToModel(model);
    addImageResolver(model, imageResolver);
    model.put(MailTemplate.KEY_DATE, new Date());

    NodeRef me = resolveCurrentUser();
    if (me != null) model.put(MailTemplate.KEY_ME, me);
    model.put(MailTemplate.KEY_PERSON, otherPerson != null ? otherPerson : me);

    if (currentRef != null) {
      populateNodeModel(model, currentRef);
    }

    model.put(TemplateService.KEY_IMAGE_RESOLVER, null);
    return model;
  }

  private void addImageResolver(
    Map<String, Object> model,
    TemplateImageResolver imageResolver
  ) {
    if (imageResolver != null) {
      model.put(MailTemplate.KEY_IMAGE_RESOLVER, imageResolver);
    }
  }

  private NodeRef resolveCurrentUser() {
    String currentUsername = AuthenticationUtil.getFullyAuthenticatedUser();
    return isValidPerson(currentUsername)
      ? personService.getPerson(currentUsername)
      : null;
  }

  private void populateNodeModel(
    Map<String, Object> model,
    NodeRef currentRef
  ) {
    QName type = nodeService.getType(currentRef);
    model.put(MailTemplate.KEY_LOCATION, currentRef);

    if (CircabcModel.TYPE_INFORMATION_NEWS.equals(type)) {
      model.put(MailTemplate.KEY_DOCUMENT, currentRef);
    } else if (
      ContentModel.TYPE_FOLDER.equals(type) ||
      dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER)
    ) {
      model.put(MailTemplate.KEY_SPACE, currentRef);
    } else if (
      ContentModel.TYPE_CONTENT.equals(type) ||
      dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)
    ) {
      model.put(MailTemplate.KEY_DOCUMENT, currentRef);
      model.put(
        MailTemplate.KEY_SPACE,
        nodeService.getPrimaryParent(currentRef).getParentRef()
      );
    }

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );
    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());
    populateCircabcStructure(model, currentRef);
  }

  private void populateCircabcStructure(
    Map<String, Object> model,
    NodeRef currentRef
  ) {
    NodeRef igRef = apiToolBox.getCurrentInterestGroup(currentRef);
    if (igRef != null) {
      model.put(MailTemplate.KEY_INTEREST_GROUP, igRef);
      model.put(
        MailTemplate.KEY_CATEGORY,
        apiToolBox.getCurrentCategory(igRef)
      );
    } else {
      NodeRef catRef = apiToolBox.getCurrentCategory(currentRef);
      if (catRef != null) model.put(MailTemplate.KEY_CATEGORY, catRef);
    }
  }

  /**
   * Builds a {@link CircabcUserDataBean} filled with placeholder values.
   *
   * <p>Every user field is set to a self-descriptive placeholder token (for example {@code
   * &lt;USERNAME&gt;}) so that the resulting bean can be used to preview a mail template without a
   * real user, showing which value would appear at each position.
   *
   * @return a user data bean populated with placeholder tokens
   */
  public CircabcUserDataBean getTemplateUserDetails() {
    final CircabcUserDataBean templateUser = new CircabcUserDataBean();
    templateUser.setUserName("<USERNAME>");
    templateUser.setFirstName("<USER_FIRST_NAME>");
    templateUser.setLastName("<USER_LAST_NAME>");
    templateUser.setEmail("<USER_EMAIL>");
    templateUser.setCompanyId("<USER_COMPANY_ID>");
    templateUser.setTitle("<USER_TITLE>");
    templateUser.setPhone("<USER_PHONE>");
    templateUser.setFax("<USER_PHONE>");
    templateUser.setURL("<USER_URL>");
    templateUser.setPostalAddress("<USER_POSTAL_ADDRESS>");
    templateUser.setDescription("<USER_DESCRIPTION>");
    templateUser.setDomain("<USER_DOMAIN>");
    templateUser.setOrgdepnumber("<USER_ORG_DEP_NUMBER>");
    templateUser.setVisibility(true);
    templateUser.setGlobalNotification(true);
    templateUser.setEcasUserName("<USER_ECAS_USERNAME>");

    return templateUser;
  }

  /**
   * Returns the disclaimer logo image configured for a node.
   *
   * @param ref the node whose customization structure holds the disclaimer logo; must not be {@code null}
   * @return the node reference of the default disclaimer logo image
   * @throws IllegalStateException if no disclaimer logo exists or it cannot be read
   */
  public NodeRef getDisclamerLogo(final NodeRef ref) {
    ParameterCheck.mandatory(THE_NODE_REFERENCE, ref);

    NodeRef templateNodeRef;
    try {
      templateNodeRef = nodePreferencesService.getDefaultConfigurationFile(
        ref,
        TEMPLATES_ROOT_FOLDER,
        DISCLAMER_FOLDER,
        LOGO_FOLDER
      );

      // should never occurs...
      if (templateNodeRef == null) {
        throw new CustomizationException(
          "No default mail disclamer logo in folder: " +
            DISCLAMER_FOLDER +
            "/" +
            LOGO_FOLDER +
            DATA_DICTIONARY_CUSTOMIZATION
        );
      }

      return templateNodeRef;
    } catch (CustomizationException ex) {
      throw new IllegalStateException(
        "Problem during getting mail disclamer logo in folder: " +
          DISCLAMER_FOLDER +
          "/" +
          LOGO_FOLDER +
          DATA_DICTIONARY_CUSTOMIZATION,
        ex
      );
    }
  }

  private boolean isValidPerson(final String userName) {
    return (
      userName != null &&
      !userName.equals("guest") &&
      !userName.equals(AuthenticationUtil.getSystemUserName())
    );
  }

  private String computeName(
    final String originalName,
    final int tries,
    final Locale locale
  ) {
    final int pointIdx = originalName.lastIndexOf('.');
    final int langIdx = originalName.lastIndexOf('_');

    final String extension = originalName.substring(pointIdx);
    final String fileName;

    if (langIdx > -1) {
      fileName = originalName.substring(0, langIdx);
    } else {
      fileName = originalName.substring(0, pointIdx);
    }

    return (
      fileName +
      "_" +
      locale.getLanguage() +
      ((tries == 0) ? "" : "(" + tries + ")") +
      extension
    );
  }

  /**
   * @param nodeService the nodeService to set
   */
  public final void setNodeService(final NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * @param templateService the templateService to set
   */
  public final void setTemplateService(final TemplateService templateService) {
    this.templateService = templateService;
  }

  /**
   * @param dictionaryService the dictionaryService to set
   */
  public final void setDictionaryService(
    final DictionaryService dictionaryService
  ) {
    this.dictionaryService = dictionaryService;
  }

  /**
   * @param personService the personService to set
   */
  public final void setPersonService(final PersonService personService) {
    this.personService = personService;
  }

  /**
   * @param nodePreferencesService the nodePreferencesService to set
   */
  public final void setNodePreferencesService(
    NodePreferencesService nodePreferencesService
  ) {
    this.nodePreferencesService = nodePreferencesService;
  }

  /**
   * @param multilingualContentService the multilingualContentService to set
   */
  public final void setMultilingualContentService(
    MultilingualContentService multilingualContentService
  ) {
    this.multilingualContentService = multilingualContentService;
  }

  /**
   * Returns the header logo image ({@code header-mail.jpg}) from the CIRCABC Data Dictionary.
   *
   * @param ref the reference node used to validate the request; must not be {@code null}
   * @return the node reference of the header mail logo image
   */
  public NodeRef getHeaderLogo(final NodeRef ref) {
    ParameterCheck.mandatory(THE_NODE_REFERENCE, ref);

    NodeRef dicoRef = circabcApi.getCircabcDictionaryNodeRef();
    NodeRef templatesRef = nodeService.getChildByName(
      dicoRef,
      ContentModel.ASSOC_CONTAINS,
      TEMPLATES_ROOT_FOLDER
    );
    NodeRef headerRef = nodeService.getChildByName(
      templatesRef,
      ContentModel.ASSOC_CONTAINS,
      HEADER
    );
    NodeRef logoRef = nodeService.getChildByName(
      headerRef,
      ContentModel.ASSOC_CONTAINS,
      "logo"
    );
    return nodeService.getChildByName(
      logoRef,
      ContentModel.ASSOC_CONTAINS,
      "header-mail.jpg"
    );
  }

  /**
   * Returns the header background image ({@code header-background.jpg}) from the CIRCABC Data
   * Dictionary.
   *
   * @param circabcRootRef the CIRCABC root reference used to validate the request; must not be {@code null}
   * @return the node reference of the header background image
   */
  @Override
  public NodeRef getHeaderBackground(NodeRef circabcRootRef) {
    ParameterCheck.mandatory(THE_NODE_REFERENCE, circabcRootRef);

    NodeRef dicoRef = circabcApi.getCircabcDictionaryNodeRef();
    NodeRef templatesRef = nodeService.getChildByName(
      dicoRef,
      ContentModel.ASSOC_CONTAINS,
      TEMPLATES_ROOT_FOLDER
    );
    NodeRef headerRef = nodeService.getChildByName(
      templatesRef,
      ContentModel.ASSOC_CONTAINS,
      HEADER
    );
    NodeRef logoRef = nodeService.getChildByName(
      headerRef,
      ContentModel.ASSOC_CONTAINS,
      "logo"
    );
    return nodeService.getChildByName(
      logoRef,
      ContentModel.ASSOC_CONTAINS,
      "header-background.jpg"
    );
  }

  /**
   * Returns the EU/CIRCABC header logo image ({@code header-eu-circabc-logo.png}) from the CIRCABC
   * Data Dictionary.
   *
   * @param circabcRootRef the CIRCABC root reference used to validate the request; must not be {@code null}
   * @return the node reference of the EU CIRCABC header logo image
   */
  @Override
  public NodeRef getHeaderEULogo(NodeRef circabcRootRef) {
    ParameterCheck.mandatory(THE_NODE_REFERENCE, circabcRootRef);

    NodeRef dicoRef = circabcApi.getCircabcDictionaryNodeRef();
    NodeRef templatesRef = nodeService.getChildByName(
      dicoRef,
      ContentModel.ASSOC_CONTAINS,
      TEMPLATES_ROOT_FOLDER
    );
    NodeRef headerRef = nodeService.getChildByName(
      templatesRef,
      ContentModel.ASSOC_CONTAINS,
      HEADER
    );
    NodeRef logoRef = nodeService.getChildByName(
      headerRef,
      ContentModel.ASSOC_CONTAINS,
      "logo"
    );
    return nodeService.getChildByName(
      logoRef,
      ContentModel.ASSOC_CONTAINS,
      "header-eu-circabc-logo.png"
    );
  }
}
