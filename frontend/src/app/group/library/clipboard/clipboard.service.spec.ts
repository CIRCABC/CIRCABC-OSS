import { TestBed } from '@angular/core/testing';
import { Node } from 'app/core/generated/circabc';
import { vi } from 'vitest';
import { ClipboardService } from './clipboard.service';

describe('ClipboardService', () => {
  let service: ClipboardService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [ClipboardService] });
    service = TestBed.inject(ClipboardService);
    sessionStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  describe('addItem', () => {
    it('should emit the node on itemsAdded$', () => {
      const node: Node = { id: '1', name: 'test' };
      const spy = vi.fn();
      service.itemsAdded$.subscribe(spy);

      service.addItem(node);

      expect(spy).toHaveBeenCalledWith(node);
    });

    it('should save to sessionStorage when saveInSession is true', () => {
      const node: Node = { id: '1', name: 'test' };

      service.addItem(node, true);

      const stored = JSON.parse(
        sessionStorage.getItem('cbc-clipboard') as string
      ) as Node[];
      expect(stored).toEqual([node]);
    });

    it('should use groupId as sessionStorage key suffix', () => {
      const node: Node = { id: '1', name: 'test' };

      service.addItem(node, true, 'g1');

      expect(sessionStorage.getItem('cbc-clipboardg1')).toBeDefined();
    });

    it('should not add duplicate nodes to sessionStorage', () => {
      const node: Node = { id: '1', name: 'test' };

      service.addItem(node, true);
      service.addItem(node, true);

      const stored = JSON.parse(
        sessionStorage.getItem('cbc-clipboard') as string
      ) as Node[];
      expect(stored).toHaveLength(1);
    });

    it('should still emit on itemsAdded$ even when duplicate is skipped in session', () => {
      const node: Node = { id: '1', name: 'test' };
      const spy = vi.fn();
      service.itemsAdded$.subscribe(spy);

      service.addItem(node, true);
      service.addItem(node, true);

      // First call emits, second call returns early before emit
      expect(spy).toHaveBeenCalledTimes(1);
    });

    it('should append multiple different nodes to sessionStorage', () => {
      const node1: Node = { id: '1', name: 'a' };
      const node2: Node = { id: '2', name: 'b' };

      service.addItem(node1, true);
      service.addItem(node2, true);

      const stored = JSON.parse(
        sessionStorage.getItem('cbc-clipboard') as string
      ) as Node[];
      expect(stored).toEqual([node1, node2]);
    });
  });

  describe('removeItem', () => {
    it('should emit the node on itemsRemoved$', () => {
      const node: Node = { id: '1', name: 'test' };
      const spy = vi.fn();
      service.itemsRemoved$.subscribe(spy);

      service.removeItem(node);

      expect(spy).toHaveBeenCalledWith(node);
    });
  });
});
