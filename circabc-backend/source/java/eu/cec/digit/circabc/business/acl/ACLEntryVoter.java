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
package eu.cec.digit.circabc.business.acl;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Acl entry voter in charge to check permissions on business wrappers that implements
 * AclAwareWrapper
 *
 * @author Yanick Pignot
 */
public class ACLEntryVoter extends org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoter {

    @Override
    public int vote(final Authentication authentication, final Object object,
                    final ConfigAttributeDefinition config) {
        // TODO to implements
        return AccessDecisionVoter.ACCESS_ABSTAIN;
    }
}
