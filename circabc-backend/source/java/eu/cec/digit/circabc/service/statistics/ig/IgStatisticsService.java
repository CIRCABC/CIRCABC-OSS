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
package eu.cec.digit.circabc.service.statistics.ig;

import eu.cec.digit.circabc.repo.log.ActivityCountDAO;
import eu.cec.digit.circabc.repo.statistics.ig.IgStatisticsParameter;
import eu.cec.digit.circabc.repo.statistics.ig.ServiceTreeRepresentation;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/** @author beaurpi */
public interface IgStatisticsService {
  IgStatisticsParameter buildStatsData(NodeRef igRoot);

  /**
   * @param igRoot
   * @return the title of the group.
   */
  String getIGTitle(NodeRef igRoot);

  /**
   * @param igRoot
   * @return the Tree structure of the library service
   */
  ServiceTreeRepresentation getLibraryStructure(NodeRef igRoot);

  /**
   * *
   *
   * @param igRoot
   * @return the Tree structure of the information service
   */
  ServiceTreeRepresentation getInformationStructure(NodeRef igRoot);

  /**
   * *
   *
   * @param currentNode
   * @return the Tree structure of the newsgroup service
   */
  ServiceTreeRepresentation getNewsgroupsStructure(NodeRef currentNode);

  /**
   * * get the list from audit database
   *
   * @param igRoot
   * @return
   */
  List<ActivityCountDAO> getListOfActivityCount(NodeRef igRoot);
}
