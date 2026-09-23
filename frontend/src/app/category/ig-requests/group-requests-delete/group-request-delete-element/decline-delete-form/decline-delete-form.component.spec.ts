import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { CategoryService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeclineDeleteFormComponent } from './decline-delete-form.component';

describe('DeclineDeleteFormComponent', () => {
  const mockDialogData = {
    request: { id: 42 },
    categoryId: 'cat-1',
  };

  const mockDialogRef = { close: vi.fn() };
  const mockCategoryService = {
    validateInterestGroupDeleteRequests: vi.fn().mockReturnValue(of(undefined)),
    validateInterestGroupDeleteRequestsAsync: vi
      .fn()
      .mockResolvedValue(undefined),
  };

  let component: DeclineDeleteFormComponent;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [DeclineDeleteFormComponent, ReactiveFormsModule],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: mockDialogData },
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

    const fixture = TestBed.createComponent(DeclineDeleteFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form with empty argument and required validator', () => {
    expect(component.decliningForm).toBeDefined();
    expect(component.decliningForm.controls['argument'].value).toBe('');
    expect(component.decliningForm.valid).toBe(false);
  });

  it('should initialize groupDeletionRequestApproval from dialog data', () => {
    expect(component.groupDeletionRequestApproval).toEqual({
      id: 42,
      argument: '',
      agreement: -1,
    });
  });

  it('should call categoryService and close dialog on reject', async () => {
    component.decliningForm.controls['argument'].setValue('Not appropriate');

    await component.reject();

    expect(
      mockCategoryService.validateInterestGroupDeleteRequestsAsync
    ).toHaveBeenCalledWith({
      id: 'cat-1',
      groupDeletionRequestApproval: expect.objectContaining({
        id: 42,
        agreement: -1,
        argument: expect.stringContaining('Not appropriate'),
      }),
    });
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
    expect(component.processing()).toBe(false);
  });

  it('should set processing to true during reject', async () => {
    mockCategoryService.validateInterestGroupDeleteRequestsAsync.mockImplementation(
      () => {
        expect(component.processing()).toBe(true);
        return Promise.resolve(undefined);
      }
    );

    component.decliningForm.controls['argument'].setValue('reason');
    await component.reject();
  });
});
