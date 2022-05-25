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
package eu.cec.digit.circabc.business.helper;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;

/**
 * Manage rules and add facilities to create common circabc rules (ie: Add LibraryAspect to all
 * childs, Add content notify aspect, ...)
 *
 * @author Yanick Pignot
 */
public class RulesManager {

    private ActionService actionService;
    private RuleService ruleService;

    public void addAspectToAllChilds(final NodeRef nodeRef, final QName aspectQName) {
        addAspectToAllChilds(nodeRef, aspectQName, null);
    }

    public void addAspectToAllChilds(final NodeRef nodeRef, final QName aspectQName,
                                     final QName childTypeQName) {
        // create the action
        final CompositeAction compositeAction = actionService.createCompositeAction();

        final Action action = actionService.createAction(AddFeaturesActionExecuter.NAME);
        compositeAction.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME,
                aspectQName);

        compositeAction.addAction(action);
        compositeAction.setTitle("Add " + aspectQName.getLocalName() + " Aspect");
        compositeAction.setDescription(compositeAction.getTitle() + " to any child ref");

        //create the action Condition
        final ActionCondition condition;
        if (childTypeQName == null) {
            condition = actionService.createActionCondition(NoConditionEvaluator.NAME);
        } else {
            condition = actionService.createActionCondition(IsSubTypeEvaluator.NAME);
            condition.setParameterValue(IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CONTENT);

            compositeAction.setDescription(
                    compositeAction.getDescription() + " of type " + childTypeQName.getLocalName());
        }
        compositeAction.addActionCondition(condition);

        // create a rule
        final Rule rule = new Rule();
        rule.setRuleType(RuleType.INBOUND);

        rule.applyToChildren(true);
        rule.setExecuteAsynchronously(false);
        rule.setAction(compositeAction);
        rule.setTitle(compositeAction.getTitle() + " Rule");
        rule.setDescription(compositeAction.getDescription() + " synchronly to any sub level");
        ruleService.saveRule(nodeRef, rule);

    }

    //--------------
    //-- private helpers

    //--------------
    //-- IOC

    /**
     * @param actionService the actionService to set
     */
    public final void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * @param ruleService the ruleService to set
     */
    public final void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

}
