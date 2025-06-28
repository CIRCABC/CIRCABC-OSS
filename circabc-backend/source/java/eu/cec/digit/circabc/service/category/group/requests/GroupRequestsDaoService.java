/**
 *
 */
package eu.cec.digit.circabc.service.category.group.requests;

import io.swagger.model.GroupCreationRequest;
import io.swagger.model.GroupDeletionRequest;
import java.util.List;

/** @author beaurpi */
public interface GroupRequestsDaoService {
  public Integer getCountCategoryGroupCreationRequests(
    String categRef,
    String filter
  );

  public List<GroupCreationRequest> getCategoryGroupCreationRequests(
    String categRef,
    int limit,
    int page,
    String filter
  );

  public int saveRequest(GroupCreationRequest body);

  public void updateGroupCreationRequestApproval(
    String username,
    long id,
    int agreement,
    String argument
  );

  public GroupCreationRequest getCategoryGroupCreationRequests(
    String requestId
  );

  public void putCategoryGroupCreationRequest(
    String requestId,
    GroupCreationRequest body
  );

  public int saveRequestDeletion(GroupDeletionRequest body);

  public List<GroupDeletionRequest> getCategoryGroupDeletionRequests(
    String categoryRef,
    Integer limit,
    Integer page,
    String filter
  );

  public GroupDeletionRequest getCategoryGroupDeletionRequests(
    String requestId
  );

  public Long getCountCategoryGroupDeletionRequests(
    String categoryRef,
    String filter
  );

  public void updateRequestDeletion(GroupDeletionRequest body, String reviewer);

  public void deleteRequestDeletion(String group_id);

  public boolean existsGroupDeleteRequest(String groupId);
}
