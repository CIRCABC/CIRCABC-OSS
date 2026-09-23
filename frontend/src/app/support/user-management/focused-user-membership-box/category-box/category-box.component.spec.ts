import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { CategoryService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CategoryBoxComponent } from './category-box.component';

describe('CategoryBoxComponent', () => {
  let component: CategoryBoxComponent;
  let componentRef: ComponentRef<CategoryBoxComponent>;
  let fixture: ComponentFixture<CategoryBoxComponent>;
  const mockCategoryService = {
    deleteCategoryAdministartor: vi.fn(),
    deleteCategoryAdministartorAsync: vi.fn().mockResolvedValue(null),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryBoxComponent],
      providers: [
        { provide: CategoryService, useValue: mockCategoryService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryBoxComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('category', { id: 'cat1', name: 'Test' });
    componentRef.setInput('userId', 'user1');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call deleteCategoryAdministartor and emit userUninvited on success', async () => {
    mockCategoryService.deleteCategoryAdministartorAsync.mockResolvedValue(
      null
    );
    const emitSpy = vi.spyOn(component.userUninvited, 'emit');

    await component.uninviteUSer();

    expect(
      mockCategoryService.deleteCategoryAdministartorAsync
    ).toHaveBeenCalledWith({ id: 'cat1', userId: 'user1' });
    expect(emitSpy).toHaveBeenCalled();
    expect(component.uninviting).toBe(false);
  });

  it('should set uninviting to false and not emit on error', async () => {
    mockCategoryService.deleteCategoryAdministartorAsync.mockRejectedValue(
      new Error('fail')
    );
    const emitSpy = vi.spyOn(component.userUninvited, 'emit');

    await component.uninviteUSer();

    expect(emitSpy).not.toHaveBeenCalled();
    expect(component.uninviting).toBe(false);
  });

  it('should not call service if categoryId is undefined', async () => {
    mockCategoryService.deleteCategoryAdministartorAsync.mockClear();
    componentRef.setInput('category', { name: 'NoId' });
    fixture.detectChanges();

    await component.uninviteUSer();

    expect(
      mockCategoryService.deleteCategoryAdministartorAsync
    ).not.toHaveBeenCalled();
  });
});
