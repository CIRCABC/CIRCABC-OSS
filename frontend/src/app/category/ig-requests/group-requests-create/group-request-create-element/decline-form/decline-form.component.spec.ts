import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { CategoryService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeclineFormComponent } from './decline-form.component';

describe('DeclineFormComponent', () => {
  let component: DeclineFormComponent;

  const mockDialogRef = { close: vi.fn() };
  const mockCategoryService = {
    validateInterestGroupRequestsAsync: vi.fn().mockResolvedValue(undefined),
  };
  const mockData = {
    request: { id: 42 },
    categoryId: 'cat-1',
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [DeclineFormComponent, ReactiveFormsModule],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: mockData },
        { provide: MatDialogRef, useValue: mockDialogRef },
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

    const fixture = TestBed.createComponent(DeclineFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize the form on ngOnInit', () => {
    expect(component.decliningForm).toBeDefined();
    expect(component.decliningForm.get('argument')?.value).toBe('');
    expect(component.decliningForm.get('agreement')?.value).toBe(-1);
    expect(component.decliningForm.get('id')?.value).toBe(42);
  });

  it('should call validateInterestGroupRequests and close dialog on reject', async () => {
    component.decliningForm.patchValue({ argument: 'reason' });

    await component.reject();

    expect(
      mockCategoryService.validateInterestGroupRequestsAsync
    ).toHaveBeenCalledWith({
      id: 'cat-1',
      idRequest: 42,
      groupCreationRequestApproval: component.decliningForm.value,
    });
    expect(component.processing()).toBe(false);
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
  });

  it('should handle error in reject and still close dialog', async () => {
    mockCategoryService.validateInterestGroupRequestsAsync.mockRejectedValue(
      new Error('fail')
    );
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    await component.reject();

    expect(consoleSpy).toHaveBeenCalled();
    expect(component.processing()).toBe(false);
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
  });

  it('should not call service if categoryId is falsy', async () => {
    component.categoryId = '';

    await component.reject();

    expect(
      mockCategoryService.validateInterestGroupRequestsAsync
    ).not.toHaveBeenCalled();
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
  });
});
