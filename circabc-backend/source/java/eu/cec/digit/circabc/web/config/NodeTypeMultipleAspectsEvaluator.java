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
package eu.cec.digit.circabc.web.config;

import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.springframework.extensions.config.evaluator.Evaluator;

import java.util.Set;

/**
 * Evaluator that determines whether a given object has a particular node type and then has ALL
 * particular aspects applied. node-type must be in the first place
 *
 * @author patrice.coppens@trasys.lu
 */
public class NodeTypeMultipleAspectsEvaluator implements Evaluator {

    /**
     * condition separator.
     */
    public static final String SEPARATOR = ";";

    /**
     * "not" prefixe.
     */
    public static final String NOT = "!";

    /**
     * Determines whether the given aspects are applied to the given object.
     *
     * @param obj       to evaluate.
     * @param condition all aspects separate with ';' use '!' as "NOT" boolean
     * @return boolean true if ALL aspect are applied
     * @see org.alfresco.config.evaluator.Evaluator#applies(java.lang.Object, java.lang.String)
     */
    public boolean applies(Object obj, String condition) {
        String[] typeAspectCondition = condition.split(SEPARATOR);

        if (obj instanceof Node) {
            //evaluate if obj is the specified type
            QName type = ((Node) obj).getType();
            if (type != null) {
                if (!type.equals(Repository.resolveToQName(typeAspectCondition[0]))) {
                    return false;
                }

            }

            Set aspects = ((Node) obj).getAspects();
            if (aspects != null) {
                for (int i = 1, max = typeAspectCondition.length; i < max; i++) {

                    //check if ! is present
                    if (typeAspectCondition[i].startsWith(NOT)) {
                        //must not be present
                        final String aspect = typeAspectCondition[i].substring(1);
                        QName spaceQName = Repository.resolveToQName(aspect);
                        //return false if at least one aspect with '!" is present.
                        if (aspects.contains(spaceQName)) {
                            return false;
                        }
                    }

                    //must be present
                    else {
                        QName spaceQName = Repository.resolveToQName(typeAspectCondition[i]);
                        //return false if at least one aspect is not present.
                        if (!aspects.contains(spaceQName)) {
                            return false;
                        }
                    }


                }
                //all aspects are present.
                return true;
            }
        }
        //object has no aspect
        return false;
    }

}

