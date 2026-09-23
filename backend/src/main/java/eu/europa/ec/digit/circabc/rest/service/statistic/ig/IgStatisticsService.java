package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import io.swagger.model.db.ActivityCountDAO;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service contract for gathering statistical information about an Interest Group (IG).
 *
 * <p>Implementations aggregate data for a single IG identified by the {@link NodeRef} of its
 * root node. This includes the group title, the tree structure of its services (Library,
 * Information and Newsgroup) and activity counts collected from the audit database. The
 * assembled data is exposed through {@link IgStatisticsParameter} and
 * {@link ServiceTreeRepresentation} value objects for use by the statistics REST endpoints.
 */
public interface IgStatisticsService {
  /**
   * Builds the aggregated statistics model for the given Interest Group.
   *
   * @param igRoot the {@link NodeRef} of the Interest Group root node
   * @return the assembled {@link IgStatisticsParameter} holding the statistics data for the IG
   */
  IgStatisticsParameter buildStatsData(NodeRef igRoot);

  /**
   * Returns the title of the Interest Group.
   *
   * @param igRoot the {@link NodeRef} of the Interest Group root node
   * @return the title of the group
   */
  String getIGTitle(NodeRef igRoot);

  /**
   * Returns the tree structure of the Library service of the Interest Group.
   *
   * @param igRoot the {@link NodeRef} of the Interest Group root node
   * @return the {@link ServiceTreeRepresentation} describing the library service structure
   */
  ServiceTreeRepresentation getLibraryStructure(NodeRef igRoot);

  /**
   * Returns the tree structure of the Information service of the Interest Group.
   *
   * @param igRoot the {@link NodeRef} of the Interest Group root node
   * @return the {@link ServiceTreeRepresentation} describing the information service structure
   */
  ServiceTreeRepresentation getInformationStructure(NodeRef igRoot);

  /**
   * Returns the tree structure of the Newsgroup service of the Interest Group.
   *
   * @param currentNode the {@link NodeRef} of the newsgroup node to describe
   * @return the {@link ServiceTreeRepresentation} describing the newsgroup service structure
   */
  ServiceTreeRepresentation getNewsgroupsStructure(NodeRef currentNode);

  /**
   * Retrieves the list of activity counts for the Interest Group from the audit database.
   *
   * @param igRoot the {@link NodeRef} of the Interest Group root node
   * @return the list of {@link ActivityCountDAO} entries recorded for the IG
   */
  List<ActivityCountDAO> getListOfActivityCount(NodeRef igRoot);
}
