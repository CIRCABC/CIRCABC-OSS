import {
  AgendaPermissions,
  agendaPermissionKeys,
} from 'app/core/evaluator/agenda-permissions';

describe('AgendaPermissions', () => {
  it('should have correct numeric values', () => {
    expect(AgendaPermissions.EveNoAccess).toBe(0);
    expect(AgendaPermissions.EveAccess).toBe(1);
    expect(AgendaPermissions.EveAdmin).toBe(2);
  });

  it('should expose all permission keys', () => {
    expect(agendaPermissionKeys).toEqual([
      'EveNoAccess',
      'EveAccess',
      'EveAdmin',
    ]);
  });

  it('should have exactly 3 permissions', () => {
    expect(agendaPermissionKeys).toHaveLength(3);
  });
});
