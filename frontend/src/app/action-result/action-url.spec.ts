import { ActionType } from 'app/action-result/action-type';
import { getActionType } from 'app/action-result/action-url';

describe('getActionType', () => {
  const uuid = '12345678-1234-1234-1234-123456789abc';

  it('should return ActionType when url and method match a shown action', () => {
    const result = getActionType(`/categories/${uuid}/logos`, 'POST');
    expect(result).toBe(ActionType.ADD_CATEGORY_LOGO);
  });

  it('should return undefined when url matches but show is false', () => {
    const result = getActionType('/autoupload', 'POST');
    expect(result).toBeUndefined();
  });

  it('should return undefined when url matches but method does not', () => {
    const result = getActionType(`/categories/${uuid}/logos`, 'DELETE');
    expect(result).toBeUndefined();
  });

  it('should return undefined when no url matches', () => {
    const result = getActionType('/nonexistent/path', 'GET');
    expect(result).toBeUndefined();
  });

  it('should match DELETE method correctly', () => {
    const result = getActionType(`/groups/${uuid}/logos`, 'DELETE');
    expect(result).toBe(ActionType.DELETE_GROUP_LOGO);
  });

  it('should match PUT method correctly', () => {
    const result = getActionType(`/groups/${uuid}`, 'PUT');
    expect(result).toBe(ActionType.UPDATE_INTEREST_GROUP);
  });

  it('should return the first matching action when multiple could match', () => {
    // /headers matches ADD_HEADERS (POST, show: true)
    const result = getActionType('/headers', 'POST');
    expect(result).toBe(ActionType.ADD_HEADERS);
  });

  it('should handle urls with query parameters', () => {
    const result = getActionType(`/groups/${uuid}?purgedata=true`, 'DELETE');
    expect(result).toBe(ActionType.DELETE_GROUP);
  });
});
