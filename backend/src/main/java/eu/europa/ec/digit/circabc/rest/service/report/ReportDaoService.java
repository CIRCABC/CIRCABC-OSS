/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.report;

import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Data access service for CIRCABC reporting queries.
 *
 * <p>Defines low-level, read-only database operations used to build platform reports. Implementations
 * run direct SQL queries (via MyBatis) against the Alfresco repository schema rather than going through
 * the higher-level Alfresco service APIs, in order to compute aggregate figures and resolve
 * relationships efficiently.
 *
 * @author beaurpi
 */
public interface ReportDaoService {
  /**
   * Counts the total number of content documents stored in the repository.
   *
   * <p>Executes an aggregate SQL query that counts nodes of the {@code content} type, used for
   * platform-wide reporting metrics.
   *
   * @return the total number of content documents, or {@code null} if the count cannot be determined
   */
  Integer queryDbForNumberOfDocuments();

  /**
   * Retrieves the shared spaces available to the given interest group.
   *
   * <p>Looks up all "invited interest group" share-space nodes that reference the supplied interest
   * group node, resolving each match into a {@link NodeRef}. If the required repository metadata
   * identifiers cannot be resolved, an empty list is returned.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group whose shared spaces are requested
   * @return the list of {@link NodeRef}s for the shared spaces available to the interest group;
   *     never {@code null}, possibly empty
   */
  List<NodeRef> getAvailibleShareSpaces(NodeRef igNodeRef);
}
