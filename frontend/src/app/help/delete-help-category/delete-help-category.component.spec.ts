import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { HelpService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeleteHelpCategoryComponent } from './delete-help-category.component';

describe('DeleteHelpCategoryComponent', () => {
  let component: DeleteHelpCategoryComponent;
  let fixture: ComponentFixture<DeleteHelpCategoryComponent>;
  const mockHelpService = {
    deleteHelpCategory: vi.fn().mockReturnValue(of(undefined)),
    deleteHelpCategoryAsync: vi.fn().mockResolvedValue(undefined),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteHelpCategoryComponent],
      providers: [
        { provide: HelpService, useValue: mockHelpService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteHelpCategoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('cancel', () => {
    it('should reset state and emit showModalChange', () => {
      const showModalChangeSpy = vi.spyOn(component.showModalChange, 'emit');
      component.showModal.set(true);
      component.categoryId.set('123');

      component.cancel();

      expect(component.categoryId()).toBeUndefined();
      expect(component.showModal()).toBe(false);
      expect(showModalChangeSpy).toHaveBeenCalledWith(false);
    });
  });

  describe('delete', () => {
    it('should delete category and emit success result', async () => {
      const categoryDeletedSpy = vi.spyOn(component.categoryDeleted, 'emit');
      const showModalChangeSpy = vi.spyOn(component.showModalChange, 'emit');
      component.categoryId.set('cat-1');

      await component.delete();

      expect(mockHelpService.deleteHelpCategoryAsync).toHaveBeenCalledWith({
        id: 'cat-1',
      });
      expect(component.categoryId()).toBeUndefined();
      expect(component.showModal()).toBe(false);
      expect(showModalChangeSpy).toHaveBeenCalledWith(false);
      expect(component.deleting()).toBe(false);
      expect(categoryDeletedSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_HELP_SECTION,
        result: ActionResult.SUCCEED,
      });
    });

    it('should not call service when categoryId is undefined', async () => {
      const categoryDeletedSpy = vi.spyOn(component.categoryDeleted, 'emit');
      component.categoryId.set(undefined);

      await component.delete();

      expect(mockHelpService.deleteHelpCategoryAsync).not.toHaveBeenCalled();
      expect(component.deleting()).toBe(false);
      expect(categoryDeletedSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_HELP_SECTION,
      });
    });

    it('should handle error and still emit result', async () => {
      mockHelpService.deleteHelpCategoryAsync.mockRejectedValue(
        new Error('fail')
      );
      const categoryDeletedSpy = vi.spyOn(component.categoryDeleted, 'emit');
      component.categoryId.set('cat-1');

      await component.delete();

      expect(component.deleting()).toBe(false);
      expect(categoryDeletedSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_HELP_SECTION,
      });
    });
  });
});
