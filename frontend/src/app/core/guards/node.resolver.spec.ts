import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Node as ModelNode, NodesService } from 'app/core/generated/circabc';
import { resolveNode } from 'app/core/guards/node.resolver';
import { Observable, of, throwError } from 'rxjs';
import { vi } from 'vitest';

describe('resolveNode', () => {
  const mockNodesService = { getNode: vi.fn() };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: NodesService, useValue: mockNodesService }],
    });
  });

  const runResolver = (route: ActivatedRouteSnapshot) =>
    TestBed.runInInjectionContext(() =>
      resolveNode(route, {} as RouterStateSnapshot)
    ) as Observable<ModelNode>;

  it('should return the node from NodesService', () => {
    const node: ModelNode = { id: '123', name: 'Test' };
    mockNodesService.getNode.mockReturnValue(of(node));

    const route = {
      params: { id: '123' },
    } as unknown as ActivatedRouteSnapshot;

    runResolver(route).subscribe((result) => {
      expect(result).toEqual(node);
    });
    expect(mockNodesService.getNode).toHaveBeenCalledWith({ id: '123' });
  });

  it('should return a fallback node with id on error', () => {
    mockNodesService.getNode.mockReturnValue(
      throwError(() => new Error('fail'))
    );

    const route = {
      params: { id: '456' },
    } as unknown as ActivatedRouteSnapshot;

    runResolver(route).subscribe((result) => {
      expect(result).toEqual({ id: '456' });
    });
  });
});
