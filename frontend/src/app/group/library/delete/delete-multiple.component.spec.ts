import { ComponentRef, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import {
  ContentService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeleteMultipleComponent } from './delete-multiple.component';

describe('DeleteMultipleComponent', () => {
  let component: DeleteMultipleComponent;
  let componentRef: ComponentRef<DeleteMultipleComponent>;
  let fixture: ComponentFixture<DeleteMultipleComponent>;

  const mockSpaceService = {
    deleteSpace: vi.fn().mockReturnValue(of(null)),
    deleteSpaceAsync: vi.fn().mockResolvedValue(null),
  };
  const mockContentService = {
    deleteContent: vi.fn().mockReturnValue(of(null)),
    deleteContentAsync: vi.fn().mockResolvedValue(null),
  };
  const mockClipboardService = { removeItem: vi.fn() };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteMultipleComponent],
      providers: [
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: ContentService, useValue: mockContentService },
        { provide: ClipboardService, useValue: mockClipboardService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteMultipleComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('nodes', []);
    fixture.detectChanges();

    vi.clearAllMocks();
    mockSpaceService.deleteSpaceAsync.mockResolvedValue(null);
    mockContentService.deleteContentAsync.mockResolvedValue(null);
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnChanges', () => {
    it('should set progressMax to the number of nodes', () => {
      const nodes: ModelNode[] = [{ id: '1' }, { id: '2' }, { id: '3' }];
      component.ngOnChanges({
        nodes: new SimpleChange(null, nodes, true),
      });

      expect(component.progressMax).toBe(3);
      expect(component.progressValue()).toBe(0);
      expect(component.deleting).toBe(false);
    });

    it('should set progressMax to 0 when currentValue is null', () => {
      component.ngOnChanges({
        nodes: new SimpleChange([], null, false),
      });

      expect(component.progressMax).toBe(0);
    });
  });

  describe('cancelWizard', () => {
    it('should emit modalHide with CANCELED result and DELETE_ALL type', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('cancel');

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.DELETE_ALL,
      });
    });
  });

  describe('deleteAll', () => {
    it('should delete folders via spaceService', async () => {
      const nodes: ModelNode[] = [{ id: 'f1', type: 'folder' }];
      componentRef.setInput('nodes', nodes);
      fixture.detectChanges();

      await component.deleteAll();

      expect(mockSpaceService.deleteSpaceAsync).toHaveBeenCalledWith({
        id: 'f1',
        notify: true,
      });
      expect(mockClipboardService.removeItem).toHaveBeenCalledWith(nodes[0]);
    });

    it('should delete content via contentService', async () => {
      const nodes: ModelNode[] = [{ id: 'c1', type: 'content' }];
      componentRef.setInput('nodes', nodes);
      fixture.detectChanges();

      await component.deleteAll();

      expect(mockContentService.deleteContentAsync).toHaveBeenCalledWith({
        id: 'c1',
        notify: true,
      });
      expect(mockClipboardService.removeItem).toHaveBeenCalledWith(nodes[0]);
    });

    it('should handle mixed nodes and update progress', async () => {
      const nodes: ModelNode[] = [
        { id: 'f1', type: 'folder' },
        { id: 'c1', type: 'content' },
      ];
      componentRef.setInput('nodes', nodes);
      fixture.detectChanges();

      await component.deleteAll();

      expect(mockSpaceService.deleteSpaceAsync).toHaveBeenCalledWith({
        id: 'f1',
        notify: true,
      });
      expect(mockContentService.deleteContentAsync).toHaveBeenCalledWith({
        id: 'c1',
        notify: true,
      });
      expect(component.progressValue()).toBe(2);
    });

    it('should emit SUCCEED result when done', async () => {
      componentRef.setInput('nodes', [{ id: '1', type: 'content' }]);
      fixture.detectChanges();
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.deleteAll();

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.DELETE_ALL,
      });
    });

    it('should skip nodes without id', async () => {
      componentRef.setInput('nodes', [{ type: 'content' }]);
      fixture.detectChanges();

      await component.deleteAll();

      expect(mockContentService.deleteContent).not.toHaveBeenCalled();
      expect(mockClipboardService.removeItem).not.toHaveBeenCalled();
      expect(component.progressValue()).toBe(1);
    });

    it('should not notify when notify toggle is off', async () => {
      component.notifyFormGroup.controls.notify.setValue(false);
      componentRef.setInput('nodes', [{ id: 'c1', type: 'content' }]);
      fixture.detectChanges();

      await component.deleteAll();

      expect(mockContentService.deleteContentAsync).toHaveBeenCalledWith({
        id: 'c1',
        notify: false,
      });
    });

    it('should set deleting to true', async () => {
      componentRef.setInput('nodes', []);
      fixture.detectChanges();

      await component.deleteAll();

      expect(component.deleting).toBe(true);
    });
  });
});
