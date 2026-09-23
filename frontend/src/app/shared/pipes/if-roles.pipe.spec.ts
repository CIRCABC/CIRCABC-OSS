import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { PermissionEvaluator } from '../../core/evaluator/permission-evaluator';
import { AllPermission } from '../../core/evaluator/permissions';
import { Node as ModelNode } from '../../core/generated/circabc';
import { IfRolesPipe } from './if-roles.pipe';

describe('IfRolesPipe', () => {
  let pipe: IfRolesPipe;
  const mockEvaluator = {
    hasAnyOfPermissions: vi.fn(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        IfRolesPipe,
        { provide: PermissionEvaluator, useValue: mockEvaluator },
      ],
    });
    pipe = TestBed.inject(IfRolesPipe);
    mockEvaluator.hasAnyOfPermissions.mockReset();
  });

  it('should return false when value is false', () => {
    const node: ModelNode = { permissions: { LibAdmin: 'ALLOWED' } };
    const permissions: AllPermission[] = ['LibAdmin'];
    const ownerPermissions: AllPermission[] = [];

    expect(pipe.transform(false, [node, permissions, ownerPermissions])).toBe(
      false
    );
    expect(mockEvaluator.hasAnyOfPermissions).not.toHaveBeenCalled();
  });

  it('should return true when value is true and user has permission', () => {
    mockEvaluator.hasAnyOfPermissions.mockReturnValue(true);
    const node: ModelNode = { permissions: { LibAdmin: 'ALLOWED' } };
    const permissions: AllPermission[] = ['LibAdmin'];
    const ownerPermissions: AllPermission[] = [];

    expect(pipe.transform(true, [node, permissions, ownerPermissions])).toBe(
      true
    );
    expect(mockEvaluator.hasAnyOfPermissions).toHaveBeenCalledWith(
      node,
      permissions,
      ownerPermissions
    );
  });

  it('should return false when value is true but user lacks permission', () => {
    mockEvaluator.hasAnyOfPermissions.mockReturnValue(false);
    const node: ModelNode = { permissions: {} };
    const permissions: AllPermission[] = ['LibAdmin'];
    const ownerPermissions: AllPermission[] = [];

    expect(pipe.transform(true, [node, permissions, ownerPermissions])).toBe(
      false
    );
  });
});
