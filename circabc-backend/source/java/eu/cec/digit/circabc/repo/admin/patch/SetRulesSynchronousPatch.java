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

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Set;

/**
 * Applicatif patch that set the content notify aspect on The newsgroups and libraries are being
 * exectued Synchronously
 *
 * @author yanick pignot
 */
public class SetRulesSynchronousPatch extends AbstractPatch {

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(SetRulesSynchronousPatch.class);

    private int updatedCount = 0;

    private ManagementService managementService;
    private RuleService ruleService;

    @Override
    protected String applyInternal() throws Exception {
        updatedCount = 0;

        final NodeRef circabc = managementService.getCircabcNodeRef();

        if (circabc != null) {
            modifRule(circabc);
        }
        // else it s a new installation and no script to launch.

        return "Rules successfully setted as being executed synchronously"
                + "\n\tAdd aspect Content Notify Rules modified:   "
                + updatedCount;
    }

    private void modifRule(NodeRef ref) {
        final Set<QName> aspects = nodeService.getAspects(ref);

        if (aspects.contains(CircabcModel.ASPECT_CATEGORY)
                || aspects.contains(CircabcModel.ASPECT_CIRCABC_ROOT)
                || aspects.contains(CircabcModel.ASPECT_IGROOT)) {
            final List<ChildAssociationRef> childs = nodeService.getChildAssocs(ref);
            for (final ChildAssociationRef assoc : childs) {
                modifRule(assoc.getChildRef());
            }
        } else if (aspects.contains(CircabcModel.ASPECT_LIBRARY_ROOT)
                || aspects.contains(CircabcModel.ASPECT_NEWSGROUP_ROOT)) {

            final List<Rule> rules = getRuleService().getRules(ref, false);

            for (Rule rule : rules) {
                if (rule.getExecuteAsynchronously()) {
                    rule.setExecuteAsynchronously(false);
                    updatedCount++;

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Rule successfully setted as being executed synchronously "
                                        + "\n\tRule title:       "
                                        + rule.getTitle()
                                        + "\n\tRule description: "
                                        + rule.getDescription()
                                        + "\n\tNoderef:          "
                                        + ref);
                    }
                }
            }
        }
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
