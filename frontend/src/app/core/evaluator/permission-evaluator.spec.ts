import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { Node as ModelNode } from '../generated/circabc';
import { LoginService } from '../login.service';
import { PermissionEvaluator } from './permission-evaluator';

describe('PermissionEvaluator', () => {
  let evaluator: PermissionEvaluator;
  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('testuser'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PermissionEvaluator,
        { provide: LoginService, useValue: mockLoginService },
      ],
    });
    evaluator = TestBed.inject(PermissionEvaluator);
  });

  describe('hasPermission', () => {
    it('should return false when node is undefined', () => {
      expect(
        evaluator.hasPermission(
          undefined as unknown as ModelNode,
          'LibAccess',
          []
        )
      ).toBe(false);
    });

    it('should return false when node has no permissions', () => {
      const node: ModelNode = {};
      expect(evaluator.hasPermission(node, 'LibAccess', [])).toBe(false);
    });

    it('should return true when permission is ALLOWED and no owner check', () => {
      const node: ModelNode = { permissions: { LibAccess: 'ALLOWED' } };
      expect(evaluator.hasPermission(node, 'LibAccess', [])).toBe(true);
    });

    it('should return false when permission is not ALLOWED', () => {
      const node: ModelNode = { permissions: { LibAccess: 'DENIED' } };
      expect(evaluator.hasPermission(node, 'LibAccess', [])).toBe(false);
    });

    it('should return true when permission is ALLOWED and owner check passes', () => {
      mockLoginService.getCurrentUsername.mockReturnValue('owner1');
      const node: ModelNode = {
        permissions: { LibManageOwn: 'ALLOWED' },
        properties: { owner: 'owner1' },
      };
      expect(
        evaluator.hasPermission(node, 'LibManageOwn', ['LibManageOwn'])
      ).toBe(true);
    });

    it('should return false when permission is ALLOWED but owner check fails', () => {
      mockLoginService.getCurrentUsername.mockReturnValue('otheruser');
      const node: ModelNode = {
        permissions: { LibManageOwn: 'ALLOWED' },
        properties: { owner: 'owner1' },
      };
      expect(
        evaluator.hasPermission(node, 'LibManageOwn', ['LibManageOwn'])
      ).toBe(false);
    });

    it('should return true when permission is not ALLOWED but user is owner and ownerPermissions provided', () => {
      mockLoginService.getCurrentUsername.mockReturnValue('owner1');
      const node: ModelNode = {
        permissions: { LibManageOwn: 'DENIED' },
        properties: { owner: 'owner1' },
      };
      expect(
        evaluator.hasPermission(node, 'LibManageOwn', ['LibManageOwn'])
      ).toBe(true);
    });
  });

  describe('hasAnyOfPermissions', () => {
    it('should return false when node is undefined', () => {
      expect(
        evaluator.hasAnyOfPermissions(
          undefined as unknown as ModelNode,
          ['LibAccess'],
          []
        )
      ).toBe(false);
    });

    it('should return true when at least one permission is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibAdmin: 'ALLOWED' } };
      expect(
        evaluator.hasAnyOfPermissions(node, ['LibAccess', 'LibAdmin'], [])
      ).toBe(true);
    });

    it('should return false when no permissions are ALLOWED', () => {
      const node: ModelNode = { permissions: { LibAccess: 'DENIED' } };
      expect(
        evaluator.hasAnyOfPermissions(node, ['LibAccess', 'LibAdmin'], [])
      ).toBe(false);
    });
  });

  describe('hasStrongerPermission', () => {
    it('should return true when node has a stronger permission', () => {
      const node: ModelNode = { permissions: { LibAdmin: 'ALLOWED' } };
      expect(evaluator.hasStrongerPermission(node, 'LibAccess', [])).toBe(true);
    });

    it('should return true when node has the exact permission', () => {
      const node: ModelNode = { permissions: { LibAccess: 'ALLOWED' } };
      expect(evaluator.hasStrongerPermission(node, 'LibAccess', [])).toBe(true);
    });

    it('should return false when node has a weaker permission', () => {
      const node: ModelNode = { permissions: { LibAccess: 'ALLOWED' } };
      expect(evaluator.hasStrongerPermission(node, 'LibAdmin', [])).toBe(false);
    });

    it('should throw for unsupported permission type', () => {
      const node: ModelNode = { permissions: {} };
      expect(() =>
        evaluator.hasStrongerPermission(node, 'Unknown' as never, [])
      ).toThrow('unsupported permission type Unknown');
    });
  });
});
