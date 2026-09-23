import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { PermissionEvaluator } from '../../core/evaluator/permission-evaluator';
import { Node as ModelNode } from '../../core/generated/circabc';
import { IfRoleGePipe } from './if-role-ge.pipe';

describe('IfRoleGePipe', () => {
  let pipe: IfRoleGePipe;
  const mockEvaluator = {
    hasStrongerPermission: vi.fn(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        IfRoleGePipe,
        { provide: PermissionEvaluator, useValue: mockEvaluator },
      ],
    });
    pipe = TestBed.inject(IfRoleGePipe);
    mockEvaluator.hasStrongerPermission.mockReset();
  });

  it('should return false when value is false regardless of permission', () => {
    const node: ModelNode = { permissions: { LibAdmin: 'ALLOWED' } };
    const result = pipe.transform(false, [node, 'LibAdmin', []]);
    expect(result).toBe(false);
    expect(mockEvaluator.hasStrongerPermission).not.toHaveBeenCalled();
  });

  it('should return true when value is true and permission check passes', () => {
    const node: ModelNode = { permissions: { LibAdmin: 'ALLOWED' } };
    mockEvaluator.hasStrongerPermission.mockReturnValue(true);
    const result = pipe.transform(true, [node, 'LibAdmin', []]);
    expect(result).toBe(true);
    expect(mockEvaluator.hasStrongerPermission).toHaveBeenCalledWith(
      node,
      'LibAdmin',
      []
    );
  });

  it('should return false when value is true but permission check fails', () => {
    const node: ModelNode = { permissions: {} };
    mockEvaluator.hasStrongerPermission.mockReturnValue(false);
    const result = pipe.transform(true, [
      node,
      'LibFullEdit',
      ['LibManageOwn'],
    ]);
    expect(result).toBe(false);
  });

  it('should throw when node is undefined', () => {
    expect(() => pipe.transform(true, [undefined, 'LibAdmin', []])).toThrow();
  });
});
