import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionService } from 'app/action-result/action.service';
import { ActionResult } from 'app/action-result/action-result';
import { ActionType } from 'app/action-result/action-type';
import {
  Node as ModelNode,
  PagedNodes,
  PermissionService,
  SpaceService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddSpaceComponent } from './add-space.component';

const mockPagedNodes: PagedNodes = { data: [], total: 0 };
const mockCreatedNode: ModelNode = { id: 'new-space-id', name: 'TestSpace' };

const mockSpaceService = {
  getChildrenAsync: vi.fn().mockResolvedValue(mockPagedNodes),
  postSubspace: vi.fn().mockReturnValue(of(mockCreatedNode)),
  postSubspaceAsync: vi.fn().mockResolvedValue(mockCreatedNode),
};

const mockPermissionService = {
  putPermission: vi
    .fn()
    .mockReturnValue(
      of({ inherited: false, permissions: { profiles: [], users: [] } })
    ),
  putPermissionAsync: vi.fn().mockResolvedValue({
    inherited: false,
    permissions: { profiles: [], users: [] },
  }),
};

const mockRouter = {
  navigate: vi.fn().mockResolvedValue(true),
};

const mockActionService = {
  propagateActionFinished: vi.fn(),
};

async function setup() {
  const mockRoute = { params: of({}) };

  TestBed.configureTestingModule({
    imports: [AddSpaceComponent],
    providers: [
      provideNativeDateAdapter(),
      { provide: SpaceService, useValue: mockSpaceService },
      { provide: PermissionService, useValue: mockPermissionService },
      { provide: Router, useValue: mockRouter },
      { provide: ActivatedRoute, useValue: mockRoute },
      { provide: ActionService, useValue: mockActionService },
      provideTransloco({
        config: { defaultLang: 'en', availableLangs: ['en'] },
      }),
      {
        provide: TRANSLOCO_LOADER,
        useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
      },
    ],
    schemas: [NO_ERRORS_SCHEMA],
  }).overrideComponent(AddSpaceComponent, {
    set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
  });

  const fixture = TestBed.createComponent(AddSpaceComponent);
  const component = fixture.componentInstance;

  fixture.componentRef.setInput('showWizard', false);
  fixture.componentRef.setInput('parentNode', {
    id: 'parent-id',
    name: 'Parent',
  });
  fixture.detectChanges();

  return { fixture, component };
}

describe('AddSpaceComponent', () => {
  let fixture: ComponentFixture<AddSpaceComponent>;
  let component: AddSpaceComponent;

  beforeEach(async () => {
    vi.clearAllMocks();
    ({ fixture, component } = await setup());
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnChanges', () => {
    it('should load contents when showWizard becomes true', async () => {
      fixture.componentRef.setInput('showWizard', true);
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockSpaceService.getChildrenAsync).toHaveBeenCalledWith({
        id: 'parent-id',
        language: 'en',
        guest: false,
        limit: -1,
        page: 1,
        order: 'modified_DESC',
        folderOnly: false,
        fileOnly: false,
        skipExpiredItems: true,
      });
      expect(component.contents).toEqual(mockPagedNodes);
      expect(component.createSpaceForm()).toBeDefined();
    });
  });

  describe('buildForm', () => {
    beforeEach(async () => {
      fixture.componentRef.setInput('showWizard', true);
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should initialize form with default values', () => {
      expect(component.createSpaceForm()!.value.name).toBe('');
      expect(component.createSpaceForm()!.value.managePermited).toBe(true);
      expect(component.createSpaceForm()!.value.expirationDateActived).toBe(
        false
      );
    });

    it('should mark name as required', () => {
      component.createSpaceForm()!.controls['name'].setValue('');
      expect(component.createSpaceForm()!.controls['name'].valid).toBe(false);
    });

    it('should set dateRequired when expirationDateActived is true and date is null', () => {
      component.createSpaceForm()!.patchValue({
        expirationDateActived: true,
        expirationDate: null,
      });
      expect(component.dateRequired()).toBe(true);
    });
  });

  describe('cancelWizard', () => {
    beforeEach(async () => {
      fixture.componentRef.setInput('showWizard', true);
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should emit CANCELED result and reset form', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('cancel');

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.CANCELED,
          type: ActionType.CREATE_SPACE,
        })
      );
    });
  });

  describe('createSpace', () => {
    beforeEach(async () => {
      fixture.componentRef.setInput('showWizard', true);
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should create space and emit SUCCEED when form is valid and managePermited is true', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.createSpaceForm()!.patchValue({
        name: 'NewFolder',
        managePermited: true,
      });

      await component.createSpace();

      expect(mockSpaceService.postSubspaceAsync).toHaveBeenCalledWith({
        id: 'parent-id',
        node: expect.objectContaining({ name: 'NewFolder' }),
      });
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.SUCCEED,
          type: ActionType.CREATE_SPACE,
          node: mockCreatedNode,
        })
      );
      expect(mockActionService.propagateActionFinished).toHaveBeenCalled();
      expect(component.creating()).toBe(false);
    });

    it('should navigate to permissions when managePermited is false', async () => {
      component.createSpaceForm()!.patchValue({
        name: 'NewFolder',
        managePermited: false,
      });

      await component.createSpace();

      expect(mockPermissionService.putPermissionAsync).toHaveBeenCalledWith({
        id: 'new-space-id',
        permissionDefinition: expect.objectContaining({ inherited: false }),
      });
      expect(mockRouter.navigate).toHaveBeenCalled();
    });

    it('should emit FAILED result on error', async () => {
      mockSpaceService.postSubspaceAsync.mockRejectedValueOnce(
        new Error('API error')
      );
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.createSpaceForm()!.patchValue({ name: 'NewFolder' });

      await component.createSpace();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.FAILED,
          type: ActionType.CREATE_SPACE,
        })
      );
    });

    it('should not call API when form is invalid', async () => {
      component.createSpaceForm()!.patchValue({ name: '' });

      await component.createSpace();

      expect(mockSpaceService.postSubspaceAsync).not.toHaveBeenCalled();
    });
  });

  describe('nameControl', () => {
    beforeEach(async () => {
      fixture.componentRef.setInput('showWizard', true);
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should return the name form control', () => {
      expect(component.nameControl).toBe(
        component.createSpaceForm()!.controls['name']
      );
    });
  });

  describe('isExpired', () => {
    beforeEach(async () => {
      fixture.componentRef.setInput('showWizard', true);
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should return true when expiration date is in the past', () => {
      component.createSpaceForm()!.patchValue({
        expirationDate: new Date('2000-01-01'),
      });
      expect(component.isExpired()).toBe(true);
    });

    it('should return false when expiration date is in the future', () => {
      component.createSpaceForm()!.patchValue({
        expirationDate: new Date('2099-01-01'),
      });
      expect(component.isExpired()).toBe(false);
    });
  });
});
