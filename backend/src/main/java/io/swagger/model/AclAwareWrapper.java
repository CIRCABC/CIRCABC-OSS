package io.swagger.model;

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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Since alfresco implementation of acl checks are performed on final objects (NodeRef, FileInfo,
 * ... ), this class simply helps to have one common interface for many kinds of wrappers.
 * <p>
 * All wrappers that implement getNodeRef method can be a candidate of Acegi ACL checks.
 *
 * @author Yanick Pignot
 * @see org.alfresco.repo.security.permissions.impl.acegi.ACLEntryAfterInvocationProvider
 * @see org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoter
 *
 */
public interface AclAwareWrapper {
  /**
   * Returns the node reference wrapped by this object, which is the target of Alfresco/Acegi ACL
   * permission checks.
   *
   * @return the {@link NodeRef} associated with this wrapper
   */
  NodeRef getNodeRef();
}
