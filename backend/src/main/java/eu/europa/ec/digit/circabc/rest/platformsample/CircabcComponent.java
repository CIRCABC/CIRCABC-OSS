/**
 * Copyright (C) 2017 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco SDK project.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europa.ec.digit.circabc.rest.platformsample;

import eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService;
import eu.europa.ec.digit.circabc.rest.service.translation.TranslationService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alfresco module bootstrap component that provisions CIRCABC-specific
 * repository structures the first time the module starts (or on version
 * upgrades).
 *
 * <p>Extending {@link AbstractModuleComponent}, its {@code executeInternal}
 * method is invoked once by the Alfresco module lifecycle. It ensures the
 * following artefacts exist, creating them if they are missing:</p>
 * <ul>
 *   <li>the {@code faqs} folder in the CIRCABC dictionary;</li>
 *   <li>the {@code faqsLinks} folder in the CIRCABC dictionary;</li>
 *   <li>the machine-translation (MT) user;</li>
 *   <li>the {@code MT} folder under Company Home, owned by the MT user.</li>
 * </ul>
 *
 * <p>Each provisioning step is executed independently and its failure is
 * logged without aborting the remaining steps, so a partial failure does not
 * prevent the rest of the module from starting.</p>
 */
@SuppressWarnings("java:S2160")
public class CircabcComponent extends AbstractModuleComponent {

  /** Name of the FAQ folder created under the CIRCABC dictionary. */
  private static final String FAQS = "faqs";

  /** Name of the FAQ Links folder created under the CIRCABC dictionary. */
  private static final String FAQS_LINKS = "faqsLinks";

  /** Logger for this component. */
  private static Log logger = LogFactory.getLog(CircabcComponent.class);

  @Autowired
  private NodeService nodeService;

  @Autowired
  private NodeLocatorService nodeLocatorService;

  @Autowired
  private CircabcConfig circabcConfig;

  @Autowired
  private CircabcApi circabcApi;

  @Autowired
  private NodePreferencesService nodePreferencesService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PersonService personService;

  @Autowired
  private TranslationService translationService;

  @Autowired
  private UserService userService;

  /**
   * Runs the module provisioning logic once during the Alfresco module
   * lifecycle.
   *
   * <p>Logs the current CIRCABC release and Hibernate dialect, refreshes the
   * cached root node reference, and then ensures the FAQ folder, FAQ Links
   * folder, machine-translation user and MT folder all exist. Each step is
   * wrapped in its own try/catch block so that a failure in one step is
   * logged and does not prevent the subsequent steps from running.</p>
   *
   * @throws Throwable if the module component framework signals an
   *                   unrecoverable error (individual provisioning failures
   *                   are caught and logged internally)
   */
  @Override
  protected void executeInternal() throws Throwable {
    logger.info("CIRCABC release");
    logger.info(circabcConfig.getBuildRelease());
    logger.info("Hibernate dialect");
    logger.info(circabcConfig.getHibernateDialect());
    try {
      nodePreferencesService.updateRootReference();
    } catch (Exception e) {
      logger.error("Error while updating root reference", e);
    }

    try {
      createFaqsFolder();
    } catch (Exception e) {
      logger.error("Error while creating FAQ folder", e);
    }

    try {
      createFaqLinksFolder();
    } catch (Exception e) {
      logger.error("Error while creating FAQ Links folder", e);
    }
    try {
      createMTUser();
    } catch (Exception e) {
      logger.error("Error while creating MT user", e);
    }
    try {
      createMTFolder();
    } catch (Exception e) {
      logger.error("Error while creating MT folder", e);
    }
  }

  /**
   * Creates the {@code faqs} folder under the CIRCABC dictionary if it does
   * not already exist, and grants the {@code guest} authority
   * {@code Consumer} access to it.
   */
  private void createFaqsFolder() {
    NodeRef ddRef = circabcApi.getCircabcDictionaryNodeRef();
    NodeRef faqsRef = nodeService.getChildByName(
      ddRef,
      ContentModel.ASSOC_CONTAINS,
      FAQS
    );

    if (faqsRef == null) {
      ChildAssociationRef faqsAssocRef = nodeService.createNode(
        ddRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(NamespaceService.ALFRESCO_URI, FAQS),
        ContentModel.TYPE_FOLDER
      );
      nodeService.setProperty(
        faqsAssocRef.getChildRef(),
        ContentModel.PROP_NAME,
        FAQS
      );
      permissionService.setPermission(
        faqsAssocRef.getChildRef(),
        "guest",
        "Consumer",
        true
      );
    }
  }

  /**
   * Creates the {@code faqsLinks} folder under the CIRCABC dictionary if it
   * does not already exist, and grants the {@code guest} authority
   * {@code Consumer} access to it.
   */
  private void createFaqLinksFolder() {
    NodeRef ddRef = circabcApi.getCircabcDictionaryNodeRef();
    NodeRef faqLinksRef = nodeService.getChildByName(
      ddRef,
      ContentModel.ASSOC_CONTAINS,
      FAQS_LINKS
    );

    if (faqLinksRef == null) {
      ChildAssociationRef faqsAssocRef = nodeService.createNode(
        ddRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(NamespaceService.ALFRESCO_URI, FAQS_LINKS),
        ContentModel.TYPE_FOLDER
      );
      nodeService.setProperty(
        faqsAssocRef.getChildRef(),
        ContentModel.PROP_NAME,
        FAQS_LINKS
      );
      permissionService.setPermission(
        faqsAssocRef.getChildRef(),
        "guest",
        "Consumer",
        true
      );
    }
  }

  /**
   * Creates the {@code MT} folder under Company Home if it does not already
   * exist. When created, permission inheritance is disabled and the
   * machine-translation user is granted {@code Contributor} access.
   *
   * @return the {@link NodeRef} of the existing or newly created MT folder
   */
  private NodeRef createMTFolder() {
    // Implementation of the createMTNodeRef abstract method
    NodeRef companyHome = nodeLocatorService.getNode("companyhome", null, null);

    NodeRef mtNode = nodeService.getChildByName(
      companyHome,
      ContentModel.ASSOC_CONTAINS,
      "MT"
    );

    if (mtNode == null) {
      QName mtQname = QName.createQName(
        "{http://www.alfresco.org/model/content/1.0}MT"
      );

      mtNode = nodeService
        .createNode(
          companyHome,
          ContentModel.ASSOC_CONTAINS,
          mtQname,
          ContentModel.TYPE_FOLDER
        )
        .getChildRef();

      nodeService.setProperty(mtNode, ContentModel.PROP_NAME, "MT");
      permissionService.setInheritParentPermissions(mtNode, false);
      final CircabcUserDataBean mtUser = translationService.getMTUserDetails();
      permissionService.setPermission(
        mtNode,
        mtUser.getUserName(),
        PermissionService.CONTRIBUTOR,
        true
      );
    }

    return mtNode;
  }

  /**
   * Creates the machine-translation (MT) user account if a person with the
   * configured MT user name does not already exist, and sets its password.
   */
  private void createMTUser() {
    final CircabcUserDataBean mtUser = translationService.getMTUserDetails();
    if (!personService.personExists(mtUser.getUserName())) {
      userService.createUser(mtUser, true);
      userService.setPassword(
        mtUser.getUserName(),
        mtUser.getPassword().toCharArray()
      );
    }
  }
}
