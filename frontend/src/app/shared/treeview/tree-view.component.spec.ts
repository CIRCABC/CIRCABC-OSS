import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { TreeNode } from './tree-node';
import { TreeNodeComponent } from './tree-node.component';
import { TreeViewComponent } from './tree-view.component';

describe('TreeViewComponent', () => {
  let component: TreeViewComponent;
  let componentRef: ComponentRef<TreeViewComponent>;
  let fixture: ComponentFixture<TreeViewComponent>;
  const root = new TreeNode('Root', 'root-id');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TreeViewComponent],
    })
      .overrideComponent(TreeViewComponent, {
        set: { imports: [], template: '' },
      })
      .compileComponents();

    fixture = TestBed.createComponent(TreeViewComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('root', root);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('selectNode', () => {
    it('should emit selectedNodeEmitter with the node', () => {
      const emitSpy = vi.spyOn(component.selectedNodeEmitter, 'emit');
      const node = new TreeNode('Child', 'child-id');

      component.selectNode(node);

      expect(emitSpy).toHaveBeenCalledWith(node);
    });
  });

  describe('clickNode', () => {
    it('should emit clickedNodeEmitter with the node', () => {
      const emitSpy = vi.spyOn(component.clickedNodeEmitter, 'emit');
      const node = new TreeNode('Child', 'child-id');

      component.clickNode(node);

      expect(emitSpy).toHaveBeenCalledWith(node);
    });
  });

  describe('reload', () => {
    it('should call reload on the treeNodeComponent', async () => {
      const mockTreeNodeComponent = {
        reload: vi.fn().mockResolvedValue(undefined),
      } as unknown as TreeNodeComponent;

      vi.spyOn(component, 'treeNodeComponent').mockReturnValue(
        mockTreeNodeComponent
      );

      await component.reload();

      expect(mockTreeNodeComponent.reload).toHaveBeenCalled();
    });
  });
});
