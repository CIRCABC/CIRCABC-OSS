package eu.europa.ec.digit.circabc.rest.dynamic.authority.ibatis;

import eu.europa.ec.digit.circabc.rest.dynamic.authority.CircabcDynamicAuthorityDAO;
import eu.europa.ec.digit.circabc.rest.dynamic.authority.CircabcPermission;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * MyBatis/iBATIS-backed implementation of {@link CircabcDynamicAuthorityDAO}.
 *
 * <p>This DAO resolves, at runtime, the permissions and administrative role that a user holds
 * within a CIRCABC group by executing SQL statements defined in the {@code dynamic-authority}
 * MyBatis mapping namespace through a Spring-managed {@link SqlSessionTemplate}.
 *
 * <p>The {@link SqlSessionTemplate} dependency is injected via {@link #setSqlSessionTemplate} (this
 * bean is wired through the Spring XML context rather than annotation scanning).
 */
public class CircabcDynamicAuthorityDAOImpl
  implements CircabcDynamicAuthorityDAO
{

  /**
   * Identifier of the MyBatis mapped statement that selects the permissions granted to a user for a
   * group's services, expressed as {@code <namespace>.<statementId>}.
   */
  private static final String SELECT_GROUP_PERMISSION =
    "dynamic-authority.select_GroupPermission";

  /** Spring-managed MyBatis session template used to execute the mapped SQL statements. */
  private SqlSessionTemplate template;

  /**
   * Injects the MyBatis session template used to run the mapped SQL statements.
   *
   * @param sqlSessionTemplate the Spring-managed {@link SqlSessionTemplate} to use for database
   *     access
   */
  public final void setSqlSessionTemplate(
    SqlSessionTemplate sqlSessionTemplate
  ) {
    this.template = sqlSessionTemplate;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the {@code dynamic-authority.select_GroupPermission} mapped statement, passing the
   * group node reference and user name as query parameters.
   *
   * @param groupNodeRef the Alfresco node reference of the group whose permissions are queried
   * @param userName the identifier of the user whose permissions are resolved
   * @return the list of {@link CircabcPermission} entries applicable to the user for the group; may
   *     be empty if no permission is granted
   */
  @Override
  public List<CircabcPermission> getGroupPermission(
    String groupNodeRef,
    String userName
  ) {
    Map<String, Object> params = HashMap.newHashMap(1);

    params.put("nodeRef", groupNodeRef);

    params.put("userName", userName);

    return template.selectList(SELECT_GROUP_PERMISSION, params);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the {@code dynamic-authority.select_IsCategoryAdmin} mapped statement, which returns
   * a count; the user is considered a category administrator when that count is greater than zero.
   *
   * @param groupNodeRef the Alfresco node reference of the group to check against
   * @param userName the identifier of the user whose administrative role is checked
   * @return {@code true} if the user is a category administrator for the group, {@code false}
   *     otherwise
   */
  @Override
  public boolean isCategoryAdmin(String groupNodeRef, String userName) {
    Map<String, Object> params = HashMap.newHashMap(2);

    params.put("nodeRef", groupNodeRef);
    params.put("userName", userName);

    int result = template.selectOne(
      "dynamic-authority.select_IsCategoryAdmin",
      params
    );
    return result > 0;
  }
}
