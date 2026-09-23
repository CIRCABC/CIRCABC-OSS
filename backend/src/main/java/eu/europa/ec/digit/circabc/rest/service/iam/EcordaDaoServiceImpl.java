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
package eu.europa.ec.digit.circabc.rest.service.iam;

import java.util.List;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * Data access object (DAO) implementation for retrieving ECORDA-related data.
 *
 * <p>This service uses MyBatis (via a Spring-managed {@link SqlSessionTemplate}) to execute the
 * mapped SQL statements defined under the {@code CircabcEcorda} namespace. It exposes read-only
 * lookups that associate CIRCABC Interest Group nodes with their corresponding ECORDA thema
 * identifiers.
 *
 * <p>The {@link SqlSessionTemplate} dependency is injected through the Spring context via
 * {@link #setSqlSessionTemplate(SqlSessionTemplate)}.
 */
public class EcordaDaoServiceImpl {

  /**
   * MyBatis session template used to execute the mapped SQL statements. Injected by the Spring
   * container through {@link #setSqlSessionTemplate(SqlSessionTemplate)}.
   */
  private SqlSessionTemplate sqlSessionTemplate = null;

  /**
   * Retrieves the ECORDA thema identifiers associated with the given Interest Group node.
   *
   * <p>Executes the {@code CircabcEcorda.select_ecorda_thema_ids_by_ig_node_ref} mapped statement,
   * passing the supplied node reference as the query parameter.
   *
   * @param nodeRef the reference of the Interest Group node whose ECORDA thema identifiers are
   *     requested
   * @return the list of ECORDA thema identifiers linked to the given node; an empty list if none
   *     are found
   */
  public List<String> getEcordaThemaID(String nodeRef) {
    return sqlSessionTemplate.selectList(
      "CircabcEcorda.select_ecorda_thema_ids_by_ig_node_ref",
      nodeRef
    );
  }

  /**
   * Sets the MyBatis session template used to execute the mapped SQL statements.
   *
   * @param sqlSessionTemplate the sqlSessionTemplate to set
   */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }
}
