package eu.cec.digit.circabc.service.dynamic.authority;

import java.util.List;

public interface CircabcDynamicAuthorityDAO {
  List<CircabcPermission> getGroupPermission(
    String groupNodeRef,
    String userName
  );
}
