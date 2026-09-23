import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { ArchiveService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { PurgeItemComponent } from './purge-item.component';

const mockArchiveService = {
  deleteDeletedDocumentAsync: vi.fn().mockResolvedValue(undefined),
};

describe('PurgeItemComponent', () => {
  let component: PurgeItemComponent;
  let componentRef: ComponentRef<PurgeItemComponent>;
  let fixture: ComponentFixture<PurgeItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PurgeItemComponent],
      providers: [
        { provide: ArchiveService, useValue: mockArchiveService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PurgeItemComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('currentIg', { id: 'ig1' });
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('isFolder', () => {
    it('should return true when type includes folder', () => {
      expect(component.isFolder({ type: 'cm:folder' })).toBe(true);
    });

    it('should return false when type does not include folder', () => {
      expect(component.isFolder({ type: 'cm:content' })).toBe(false);
    });

    it('should return false when type is undefined', () => {
      expect(component.isFolder({})).toBe(false);
    });
  });

  describe('purge', () => {
    it('should call deleteDeletedDocument for each node with id', async () => {
      componentRef.setInput('purgeableNodes', [
        { id: 'node1' },
        { id: 'node2' },
      ]);
      fixture.detectChanges();

      const spy = vi.fn();
      component.finishPurge.subscribe(spy);

      await component.purge();

      expect(
        mockArchiveService.deleteDeletedDocumentAsync
      ).toHaveBeenCalledTimes(2);
      expect(
        mockArchiveService.deleteDeletedDocumentAsync
      ).toHaveBeenCalledWith({ id: 'ig1', nodeId: 'node1' });
      expect(
        mockArchiveService.deleteDeletedDocumentAsync
      ).toHaveBeenCalledWith({ id: 'ig1', nodeId: 'node2' });
      expect(spy).toHaveBeenCalledWith({
        type: ActionType.PURGE_CONTENT,
        result: ActionResult.SUCCEED,
      });
      expect(component.processing()).toBe(false);
    });

    it('should skip nodes without id', async () => {
      componentRef.setInput('purgeableNodes', [{ name: 'no-id' }]);
      fixture.detectChanges();

      await component.purge();

      expect(
        mockArchiveService.deleteDeletedDocumentAsync
      ).not.toHaveBeenCalled();
    });

    it('should emit FAILED result on error', async () => {
      mockArchiveService.deleteDeletedDocumentAsync.mockRejectedValue(
        new Error('fail')
      );
      componentRef.setInput('purgeableNodes', [{ id: 'node1' }]);
      fixture.detectChanges();

      const spy = vi.fn();
      component.finishPurge.subscribe(spy);

      await component.purge();

      expect(spy).toHaveBeenCalledWith({
        type: ActionType.PURGE_CONTENT,
        result: ActionResult.FAILED,
      });
      expect(component.processing()).toBe(false);
    });
  });

  describe('onCancel', () => {
    it('should emit CANCELED result', () => {
      const spy = vi.fn();
      component.cancelPurge.subscribe(spy);

      component.onCancel();

      expect(spy).toHaveBeenCalledWith({
        type: ActionType.PURGE_CONTENT,
        result: ActionResult.CANCELED,
      });
    });
  });
});
