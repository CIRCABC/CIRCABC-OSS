import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { CategoryService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AcceptFormComponent } from './accept-form.component';

const mockRequest = {
  id: 42,
  proposedName: 'test-group',
  proposedTitle: { en: 'Test Group' },
  proposedDescription: { en: 'A test group' },
  leaders: [{ userId: 'user1' }, { userId: 'user2' }],
};

const mockDialogData = {
  request: { ...mockRequest },
  categoryId: 'cat-123',
};

const mockCategoryService = {
  editInterestGroupRequest: vi.fn().mockReturnValue(of(undefined)),
  validateInterestGroupRequests: vi.fn().mockReturnValue(of(undefined)),
  postInterestGroup: vi.fn().mockReturnValue(of({})),
  editInterestGroupRequestAsync: vi.fn().mockResolvedValue(undefined),
  validateInterestGroupRequestsAsync: vi.fn().mockResolvedValue(undefined),
  postInterestGroupAsync: vi.fn().mockResolvedValue({}),
};

const mockDialogRef = {
  close: vi.fn(),
};

describe('AcceptFormComponent', () => {
  let component: AcceptFormComponent;

  beforeEach(() => {
    mockCategoryService.editInterestGroupRequest.mockReturnValue(of(undefined));
    mockCategoryService.validateInterestGroupRequests.mockReturnValue(
      of(undefined)
    );
    mockCategoryService.postInterestGroup.mockReturnValue(of({}));
    mockCategoryService.editInterestGroupRequestAsync.mockResolvedValue(
      undefined
    );
    mockCategoryService.validateInterestGroupRequestsAsync.mockResolvedValue(
      undefined
    );
    mockCategoryService.postInterestGroupAsync.mockResolvedValue({});
    mockDialogRef.close.mockReset();

    TestBed.configureTestingModule({
      imports: [AcceptFormComponent, ReactiveFormsModule],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            ...mockDialogData,
            request: { ...mockRequest, leaders: [...mockRequest.leaders] },
          },
        },
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
    });

    const fixture = TestBed.createComponent(AcceptFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form with request data on init', () => {
    expect(component.acceptingForm.value.name).toBe('test-group');
    expect(component.acceptingForm.value.title).toBe('Test Group');
    expect(component.acceptingForm.value.description).toBe('A test group');
    expect(component.acceptingForm.value.id).toBe(42);
  });

  it('should have required validators on argument and name', () => {
    component.acceptingForm.patchValue({ argument: '', name: '' });
    expect(component.acceptingForm.controls['argument'].valid).toBe(false);
    expect(component.acceptingForm.controls['name'].valid).toBe(false);

    component.acceptingForm.patchValue({
      argument: 'reason',
      name: 'valid-name',
    });
    expect(component.acceptingForm.controls['argument'].valid).toBe(true);
  });

  it('should expose form controls via getters', () => {
    expect(component.nameControl).toBe(
      component.acceptingForm.controls['name']
    );
    expect(component.titleControl).toBe(
      component.acceptingForm.controls['title']
    );
    expect(component.descriptionControl).toBe(
      component.acceptingForm.controls['description']
    );
  });

  it('should call category service methods and close dialog on accept', async () => {
    component.acceptingForm.patchValue({
      argument: 'approved',
      name: 'new-group',
      title: 'New Title',
      description: 'New Desc',
    });

    await component.accept();

    expect(
      mockCategoryService.editInterestGroupRequestAsync
    ).toHaveBeenCalledWith({
      id: 'cat-123',
      idRequest: 42,
      groupCreationRequest: expect.objectContaining({
        id: 42,
        proposedName: 'new-group',
      }),
    });
    expect(
      mockCategoryService.validateInterestGroupRequestsAsync
    ).toHaveBeenCalledWith({
      id: 'cat-123',
      idRequest: 42,
      groupCreationRequestApproval: expect.objectContaining({
        argument: expect.stringContaining('approved'),
      }),
    });
    expect(mockCategoryService.postInterestGroupAsync).toHaveBeenCalledWith({
      id: 'cat-123',
      interestGroupPostModel: expect.objectContaining({
        name: 'new-group',
        title: { en: 'New Title' },
        description: { en: 'New Desc' },
        leaders: ['user1', 'user2'],
        notify: true,
      }),
    });
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
    expect(component.processing()).toBe(false);
  });

  it('should not call services if categoryId is falsy', async () => {
    component.categoryId = '';
    mockCategoryService.editInterestGroupRequest.mockClear();
    await component.accept();

    expect(mockCategoryService.editInterestGroupRequest).not.toHaveBeenCalled();
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
  });

  it('should handle errors gracefully during accept', async () => {
    mockCategoryService.editInterestGroupRequest.mockReturnValue(of(undefined));
    mockCategoryService.validateInterestGroupRequests.mockImplementation(() => {
      throw new Error('fail');
    });

    component.acceptingForm.patchValue({ argument: 'reason', name: 'grp' });
    await component.accept();

    expect(component.processing()).toBe(false);
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
  });

  it('should return empty leaders array when request has no leaders', async () => {
    component.request.leaders = undefined;
    component.acceptingForm.patchValue({ argument: 'ok', name: 'grp' });

    await component.accept();

    expect(mockCategoryService.postInterestGroupAsync).toHaveBeenCalledWith({
      id: 'cat-123',
      interestGroupPostModel: expect.objectContaining({ leaders: [] }),
    });
  });
});
