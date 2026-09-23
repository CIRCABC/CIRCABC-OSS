import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { PermissionEvaluator } from '../../core/evaluator/permission-evaluator';
import { AllPermission } from '../../core/evaluator/permissions';
import { Node as ModelNode } from '../../core/generated/circabc';
import { IfOrRolesPipe } from './if-or-roles.pipe';

describe('IfOrRolesPipe', () => {
  let pipe: IfOrRolesPipe;
  const mockEvaluator = {
    hasAnyOfPermissions: vi.fn(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        IfOrRolesPipe,
        { provide: PermissionEvaluator, useValue: mockEvaluator },
      ],
    });
    pipe = TestBed.inject(IfOrRolesPipe);
    mockEvaluator.hasAnyOfPermissions.mockReset();
  });

  const node: ModelNode = { id: '1', permissions: { LibAdmin: 'ALLOWED' } };
  const permissions: AllPermission[] = ['LibAdmin'];
  const ownerPermissions: AllPermission[] = [];

  it('should return true when value is true regardless of permissions', () => {
    expect(pipe.transform(true, [node, permissions, ownerPermissions])).toBe(
      true
    );
    expect(mockEvaluator.hasAnyOfPermissions).not.toHaveBeenCalled();
  });

  it('should delegate to PermissionEvaluator when value is false', () => {
    mockEvaluator.hasAnyOfPermissions.mockReturnValue(true);
    expect(pipe.transform(false, [node, permissions, ownerPermissions])).toBe(
      true
    );
    expect(mockEvaluator.hasAnyOfPermissions).toHaveBeenCalledWith(
      node,
      permissions,
      ownerPermissions
    );
  });

  it('should return false when value is false and user lacks permissions', () => {
    mockEvaluator.hasAnyOfPermissions.mockReturnValue(false);
    expect(pipe.transform(false, [node, permissions, ownerPermissions])).toBe(
      false
    );
  });
});
