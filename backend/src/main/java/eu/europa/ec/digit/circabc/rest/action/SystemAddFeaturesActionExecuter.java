/*
 * Copyright 2006 European Community
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */
package eu.europa.ec.digit.circabc.rest.action;

import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Variant of Alfresco's AddFeaturesActionExecuter that always executes as the
 * Alfresco system user to avoid permission issues when adding aspects via rules.
 */
public class SystemAddFeaturesActionExecuter extends AddFeaturesActionExecuter {

  /** Action name used when registering and invoking this executer. */
  public static final String NAME = "system-add-features";

  /**
   * Executes the underlying "add features" (add aspect) action while running as
   * the Alfresco system user. Delegating to the superclass implementation inside
   * {@link AuthenticationUtil#runAsSystem} ensures the aspect can be applied even
   * when the currently authenticated user lacks the required permissions, which
   * commonly happens when the action is triggered by a rule.
   *
   * @param ruleAction the action being executed, carrying the parameters that
   *     define which aspect(s) and default property values to add
   * @param actionedUponNodeRef the node to which the aspect(s) will be applied
   */
  @Override
  public void executeImpl(
    final Action ruleAction,
    final NodeRef actionedUponNodeRef
  ) {
    // Elevate to system user for the duration of the action to avoid "Access Denied" issues
    AuthenticationUtil.runAsSystem(() -> {
      SystemAddFeaturesActionExecuter.super.executeImpl(
        ruleAction,
        actionedUponNodeRef
      );
      return null;
    });
  }
}
