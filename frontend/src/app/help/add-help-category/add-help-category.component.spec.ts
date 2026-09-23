import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result/action-result';
import { ActionType } from 'app/action-result/action-type';
import { HelpService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddHelpCategoryComponent } from './add-help-category.component';

const mockHelpService = {
  getHelpCategoryAsync: vi.fn(),
  createHelpCategory: vi.fn(),
  createHelpCategoryAsync: vi.fn().mockResolvedValue({}),
  updateHelpCategory: vi.fn(),
  updateHelpCategoryAsync: vi.fn().mockResolvedValue({}),
};

describe('AddHelpCategoryComponent', () => {
  let component: AddHelpCategoryComponent;
  let fixture: ComponentFixture<AddHelpCategoryComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [AddHelpCategoryComponent],
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

    fixture = TestBed.createComponent(AddHelpCategoryComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('category loading', () => {
    it('should initialize form in create mode when no categoryId', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.editMode()).toBe(false);
      expect(component.newCategoryForm).toBeDefined();
      expect(component.newCategoryForm.controls['title']).toBeDefined();
    });

    it('should enter edit mode and load category when categoryId is set', async () => {
      const mockCategory = { id: '123', title: { en: 'Test' } };
      mockHelpService.getHelpCategoryAsync.mockResolvedValue(mockCategory);

      fixture.componentRef.setInput('categoryId', '123');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.editMode()).toBe(true);
      expect(component.categoryToEdit()).toEqual(mockCategory);
      expect(mockHelpService.getHelpCategoryAsync).toHaveBeenCalledWith({
        id: '123',
      });
    });
  });

  describe('createCategory', () => {
    beforeEach(async () => {
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should create category and emit success result', async () => {
      mockHelpService.createHelpCategoryAsync.mockResolvedValue({});
      component.newCategoryForm.controls['title'].setValue({ en: 'New Cat' });

      const emitSpy = vi.spyOn(component.categoryCreated, 'emit');

      await component.createCategory();

      expect(mockHelpService.createHelpCategoryAsync).toHaveBeenCalledWith({
        helpCategory: expect.objectContaining({
          title: expect.objectContaining({ en: 'New Cat' }),
        }),
      });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.ADD_HELP_SECTION,
        result: ActionResult.SUCCEED,
      });
      expect(component.creating()).toBe(false);
    });

    it('should emit failed result on error', async () => {
      mockHelpService.createHelpCategoryAsync.mockRejectedValue(
        new Error('fail')
      );
      component.newCategoryForm.controls['title'].setValue({ en: 'New Cat' });

      const emitSpy = vi.spyOn(component.categoryCreated, 'emit');

      await component.createCategory();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.ADD_HELP_SECTION,
        result: ActionResult.FAILED,
      });
      expect(component.creating()).toBe(false);
    });
  });

  describe('updateCategory', () => {
    beforeEach(async () => {
      const mockCategory = { id: '123', title: { en: 'Existing' } };
      mockHelpService.getHelpCategoryAsync.mockResolvedValue(mockCategory);

      fixture.componentRef.setInput('categoryId', '123');
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should update category and emit success result', async () => {
      mockHelpService.updateHelpCategoryAsync.mockResolvedValue({});
      component.newCategoryForm.controls['title'].setValue({ en: 'Updated' });

      const emitSpy = vi.spyOn(component.categoryUpdated, 'emit');

      await component.updateCategory();

      expect(mockHelpService.updateHelpCategoryAsync).toHaveBeenCalledWith({
        id: '123',
        helpCategory: expect.objectContaining({
          title: expect.objectContaining({ en: 'Updated' }),
        }),
      });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.UPDATE_HELP_SECTION,
        result: ActionResult.SUCCEED,
      });
      expect(component.creating()).toBe(false);
    });

    it('should emit failed result on error', async () => {
      mockHelpService.updateHelpCategoryAsync.mockRejectedValue(
        new Error('fail')
      );
      component.newCategoryForm.controls['title'].setValue({ en: 'Updated' });

      const emitSpy = vi.spyOn(component.categoryUpdated, 'emit');

      await component.updateCategory();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.UPDATE_HELP_SECTION,
        result: ActionResult.FAILED,
      });
    });
  });

  describe('cancel', () => {
    it('should set showModal to false and emit', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      component.showModal.set(true);
      const emitSpy = vi.spyOn(component.showModalChange, 'emit');

      component.cancel();

      expect(component.showModal()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith(false);
    });

    it('should reset form when not in edit mode', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      component.newCategoryForm.controls['title'].setValue('test');
      const resetSpy = vi.spyOn(component.newCategoryForm, 'reset');

      component.cancel();

      expect(resetSpy).toHaveBeenCalled();
    });

    it('should not reset form in edit mode', async () => {
      const mockCategory = { id: '123', title: { en: 'Existing' } };
      mockHelpService.getHelpCategoryAsync.mockResolvedValue(mockCategory);

      fixture.componentRef.setInput('categoryId', '123');
      fixture.detectChanges();
      await fixture.whenStable();

      const resetSpy = vi.spyOn(component.newCategoryForm, 'reset');

      component.cancel();

      expect(resetSpy).not.toHaveBeenCalled();
    });
  });

  describe('validity', () => {
    it('should update isValid when title changes', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.isValid()).toBe(false);

      component.newCategoryForm.controls['title'].setValue({ en: 'Valid' });

      expect(component.isValid()).toBe(true);
    });
  });
});
