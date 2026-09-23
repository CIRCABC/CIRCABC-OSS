import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { PermissionEvaluator } from '../../core/evaluator/permission-evaluator';
import { AllPermission } from '../../core/evaluator/permissions';
import { Node as ModelNode } from '../../core/generated/circabc';
import { IfRolePipe } from './if-role.pipe';

describe('IfRolePipe', () => {
  let pipe: IfRolePipe;
  const mockEvaluator = {
    hasPermission: vi.fn(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        IfRolePipe,
        { provide: PermissionEvaluator, useValue: mockEvaluator },
      ],
    });
    pipe = TestBed.inject(IfRolePipe);
    mockEvaluator.hasPermission.mockReset();
  });

  const node: ModelNode = { id: '1', permissions: { LibAdmin: 'ALLOWED' } };
  const permission: AllPermission = 'LibAdmin';
  const ownerPermissions: AllPermission[] = [];

  it('should return false when value is false regardless of permission', () => {
    expect(pipe.transform(false, [node, permission, ownerPermissions])).toBe(
      false
    );
    expect(mockEvaluator.hasPermission).not.toHaveBeenCalled();
  });

  it('should return true when value is true and hasPermission returns true', () => {
    mockEvaluator.hasPermission.mockReturnValue(true);
    expect(pipe.transform(true, [node, permission, ownerPermissions])).toBe(
      true
    );
    expect(mockEvaluator.hasPermission).toHaveBeenCalledWith(
      node,
      permission,
      ownerPermissions
    );
  });

  it('should return false when value is true and hasPermission returns false', () => {
    mockEvaluator.hasPermission.mockReturnValue(false);
    expect(pipe.transform(true, [node, permission, ownerPermissions])).toBe(
      false
    );
  });
});
