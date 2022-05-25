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
package eu.cec.digit.circabc.repo.admin.patch;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Set;

/**
 * Applicatif patch that add the directory root container under each Interest group.
 *
 * @author yanick pignot
 */
public class AddNotificationRuleForNewsgroupsPatch extends AbstractPatch {

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(AddNotificationRuleForNewsgroupsPatch.class);

    private int totalCount = 0;

    private ManagementService managementService;
    private ActionService actionService;
    private RuleService ruleService;

    @Override
    protected String applyInternal() throws Exception {
        totalCount = 0;

        final NodeRef circabc = managementService.getCircabcNodeRef();

        if (circabc != null) {
            addRule(circabc);
        }
        // else it s a new installation and no script to launch.

        return "Add content notify aspect rule successfully applied "
                + "\n\tNewsgroups updated:   "
                + totalCount;
    }

    private void addRule(NodeRef ref) {
        final Set<QName> aspects = nodeService.getAspects(ref);

        if (aspects.contains(CircabcModel.ASPECT_CATEGORY)
                || aspects.contains(CircabcModel.ASPECT_CIRCABC_ROOT)
                || aspects.contains(CircabcModel.ASPECT_IGROOT)) {
            final List<ChildAssociationRef> childs = nodeService.getChildAssocs(ref);
            for (final ChildAssociationRef assoc : childs) {
                addRule(assoc.getChildRef());
            }
        } else if (aspects.contains(CircabcModel.ASPECT_NEWSGROUP_ROOT)) {
            totalCount++;

            // Add CircaManagement
            final CompositeAction compositeActionNotify = actionService.createCompositeAction();

            // Create Action
            final Action actionNotify = actionService.createAction(AddFeaturesActionExecuter.NAME);
            actionNotify.setParameterValue(
                    AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            compositeActionNotify.addAction(actionNotify);
            compositeActionNotify.setTitle("Add CircabcNotify Aspect");
            compositeActionNotify.setDescription("Add CircabcNotify Aspect Description");

            // Create Condition
            final ActionCondition actionConditionNotify =
                    actionService.createActionCondition(IsSubTypeEvaluator.NAME);
            actionConditionNotify.setParameterValue(
                    IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CONTENT);
            compositeActionNotify.addActionCondition(actionConditionNotify);

            // Create a rule
            final Rule rule = new Rule();
            rule.setRuleType(RuleType.INBOUND);

            rule.applyToChildren(true);
            rule.setExecuteAsynchronously(false);
            rule.setAction(compositeActionNotify);
            rule.setTitle(compositeActionNotify.getTitle());
            rule.setDescription(compositeActionNotify.getDescription());
            ruleService.saveRule(ref, rule);

            if (logger.isDebugEnabled()) {
                logger.debug("Rule successfully added to newsgroup " + ref);
            }
        }
    }

    /**
     * @return the actionService
     */
    protected final ActionService getActionService() {
        return actionService;
    }

    /**
     * @param actionService the actionService to set
     */
    public final void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * @return the managementService
     */
    protected final ManagementService getManagementService() {
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @return the ruleService
     */
    protected final RuleService getRuleService() {
        return ruleService;
    }

    /**
     * @param ruleService the ruleService to set
     */
    public final void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }
}
