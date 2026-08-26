package eu.cec.digit.circabc.service.dynamic.authority.ibatis;

import eu.cec.digit.circabc.service.dynamic.authority.CircabcDynamicAuthorityDAO;
import eu.cec.digit.circabc.service.dynamic.authority.CircabcPermission;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.spring.SqlSessionTemplate;

public class CircabcDynamicAuthorityDAOImpl
  implements CircabcDynamicAuthorityDAO {

  private static final String SELECT_GROUP_PERMISSION =
    "dynamic-authority.select_GroupPermission";

  private SqlSessionTemplate template;

  public final void setSqlSessionTemplate(
    SqlSessionTemplate sqlSessionTemplate
  ) {
    this.template = sqlSessionTemplate;
  }

  @Override
  public List<CircabcPermission> getGroupPermission(
    String groupNodeRef,
    String userName
  ) {
    Map<String, Object> params = new HashMap<>(1);

    params.put("nodeRef", groupNodeRef);

    params.put("userName", userName);

    return (List<CircabcPermission>) template.selectList(
      SELECT_GROUP_PERMISSION,
      params
    );
  }

  @Override
  public boolean isCategoryAdmin(String groupNodeRef, String userName) {
    Map<String, Object> params = new HashMap<>(2);

    params.put("nodeRef", groupNodeRef);
    params.put("userName", userName);

    int result = (int) template.selectOne(
      "dynamic-authority.select_IsCategoryAdmin",
      params
    );
    return result > 0;
  }
}
