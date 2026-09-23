import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { CategoryService, InterestGroup } from 'app/core/generated/circabc';
import { User } from 'app/core/generated/circabc/model/user';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import {
  DeleteRequestGroupComponent,
  DialogData,
} from './delete-request-group.component';

const mockGroup: InterestGroup = {
  id: 'group-123',
  name: 'Test Group',
  title: { en: 'Test Title' },
  permissions: {},
};

const mockUser: User = { userId: 'user-1', firstname: 'John' };

const mockDialogData: DialogData = { group: mockGroup };

describe('DeleteRequestGroupComponent', () => {
  let component: DeleteRequestGroupComponent;
  let fixture: ComponentFixture<DeleteRequestGroupComponent>;
  const mockLoginService = { getUser: vi.fn().mockReturnValue(mockUser) };
  const mockCategoryService = {
    isDeleteRequestPendingAsync: vi.fn().mockResolvedValue(false),
    postGroupDeletionRequestAsync: vi.fn().mockResolvedValue(undefined),
  };
  const mockDialogRef = { close: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    mockCategoryService.isDeleteRequestPendingAsync.mockResolvedValue(false);
    await TestBed.configureTestingModule({
      imports: [DeleteRequestGroupComponent, ReactiveFormsModule],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: mockDialogData },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteRequestGroupComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('initialisation', () => {
    it('should initialize form with group name and title', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.form.controls['name'].value).toBe('Test Group');
      expect(component.form.controls['title'].value).toEqual({
        en: 'Test Title',
      });
    });

    it('should check if delete request is pending', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(
        mockCategoryService.isDeleteRequestPendingAsync
      ).toHaveBeenCalledWith({
        groupId: 'group-123',
      });
      expect(component.isPending()).toBe(false);
      expect(component.processing()).toBe(false);
    });

    it('should not check pending if group has no id', async () => {
      TestBed.resetTestingModule();
      await TestBed.configureTestingModule({
        imports: [DeleteRequestGroupComponent, ReactiveFormsModule],
        providers: [
          { provide: LoginService, useValue: mockLoginService },
          { provide: CategoryService, useValue: mockCategoryService },
          { provide: MatDialogRef, useValue: mockDialogRef },
          {
            provide: MAT_DIALOG_DATA,
            useValue: {
              group: { name: 'No ID', permissions: {} },
            } as DialogData,
          },
          provideTransloco({
            config: { defaultLang: 'en', availableLangs: ['en'] },
          }),
          {
            provide: TRANSLOCO_LOADER,
            useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
          },
        ],
      }).compileComponents();

      const fixture2 = TestBed.createComponent(DeleteRequestGroupComponent);
      fixture2.detectChanges();
      await fixture2.whenStable();

      expect(
        mockCategoryService.isDeleteRequestPendingAsync
      ).not.toHaveBeenCalled();
    });
  });

  describe('requestDeleteGroup', () => {
    it('should post deletion request and close dialog', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      component.form.controls['justification'].patchValue({ en: 'reason' });

      await component.requestDeleteGroup();

      expect(
        mockCategoryService.postGroupDeletionRequestAsync
      ).toHaveBeenCalledWith({
        id: 'group-123',
        groupDeletionRequestInput: { justification: '<p>reason</p>' },
      });
      expect(mockDialogRef.close).toHaveBeenCalled();
      expect(component.processing()).toBe(false);
    });

    it('should set justification to empty string if not provided', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      component.form.controls['justification'].patchValue(null);

      await component.requestDeleteGroup();

      expect(
        mockCategoryService.postGroupDeletionRequestAsync
      ).toHaveBeenCalledWith({
        id: 'group-123',
        groupDeletionRequestInput: { justification: '' },
      });
    });
  });

  describe('justificationControl', () => {
    it('should return the justification form control', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.justificationControl).toBe(
        component.form.controls['justification']
      );
    });
  });
});
