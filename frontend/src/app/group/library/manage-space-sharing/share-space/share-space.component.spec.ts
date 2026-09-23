import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import {
  ShareIGsAndPermissions,
  SpaceService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ShareSpaceComponent } from './share-space.component';

const mockShareData: ShareIGsAndPermissions = {
  igs: [
    { name: 'Group A', value: 'ig-1' },
    { name: 'Group B', value: 'ig-2' },
  ],
  permissions: ['LibAdmin', 'LibManageOwn'],
};

const mockSpaceService = {
  getShareIGsAndPermissionsAsync: vi.fn().mockResolvedValue(mockShareData),
  postShareSpace: vi.fn().mockReturnValue(of(undefined)),
  putShareSpacePermissionUpdate: vi.fn().mockReturnValue(of(undefined)),
  postShareSpaceAsync: vi.fn().mockResolvedValue(undefined),
  putShareSpacePermissionUpdateAsync: vi.fn().mockResolvedValue(undefined),
};

describe('ShareSpaceComponent', () => {
  let component: ShareSpaceComponent;
  let componentRef: ComponentRef<ShareSpaceComponent>;
  let fixture: ComponentFixture<ShareSpaceComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [ShareSpaceComponent],
      providers: [
        { provide: SpaceService, useValue: mockSpaceService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ShareSpaceComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('spaceId', 'space-123');
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  it('should initialize form with default values', () => {
    fixture.detectChanges();
    expect(component.spaceSharingForm).toBeDefined();
    expect(component.spaceSharingForm.value.notifyLeaders).toBe(true);
  });

  it('should load share IGs and permissions and reset form', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(
      mockSpaceService.getShareIGsAndPermissionsAsync
    ).toHaveBeenCalledWith({
      id: 'space-123',
    });
    expect(component.spaceSharingForm.value.selectedIg).toBe('ig-1');
    expect(component.spaceSharingForm.value.selectedPermission).toBe(
      'LibAdmin'
    );
  });

  it('should return true from stillIGsToShare when IGs exist', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.stillIGsToShare()).toBe(true);
  });

  it('should return false from stillIGsToShare when no IGs', async () => {
    mockSpaceService.getShareIGsAndPermissionsAsync.mockResolvedValueOnce({
      igs: [],
      permissions: [],
    });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.stillIGsToShare()).toBe(false);
  });

  it('should return true from allSelected when both fields are set', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.allSelected()).toBe(true);
  });

  it('should return false from allSelected when selectedIg is empty', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.spaceSharingForm.controls['selectedIg'].patchValue('');

    expect(component.allSelected()).toBe(false);
  });

  it('should share space and emit result', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    await component.share();

    expect(mockSpaceService.postShareSpaceAsync).toHaveBeenCalledWith({
      id: 'space-123',
      notifyLeaders: true,
      share: { igId: 'ig-1', permission: 'LibAdmin' },
    });
    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.SUCCEED,
      type: ActionType.SHARE_SPACE,
    });
    expect(component.sharing()).toBe(false);
  });

  it('should change permission and emit result', async () => {
    componentRef.setInput('igId', 'ig-99');
    componentRef.setInput('currentPermission', 'LibManageOwn');
    fixture.detectChanges();
    await fixture.whenStable();

    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    // selectedPermission defaults to 'LibAdmin' which differs from currentPermission
    await component.change();

    expect(
      mockSpaceService.putShareSpacePermissionUpdateAsync
    ).toHaveBeenCalledWith({
      id: 'space-123',
      igId: 'ig-99',
      permission: 'LibAdmin',
      notifyLeaders: true,
    });
    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.SUCCEED,
      type: ActionType.SHARE_SPACE_CHANGE_PERMISSION,
    });
    expect(component.changing()).toBe(false);
  });

  it('should cancel when changing to same permission', async () => {
    componentRef.setInput('igId', 'ig-99');
    componentRef.setInput('currentPermission', 'LibAdmin');
    fixture.detectChanges();
    await fixture.whenStable();

    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    await component.change();

    expect(
      mockSpaceService.putShareSpacePermissionUpdateAsync
    ).not.toHaveBeenCalled();
    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.CANCELED,
      type: ActionType.SHARE_SPACE,
    });
  });

  it('should cancel and emit canceled result', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    component.cancel('close');

    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.CANCELED,
      type: ActionType.SHARE_SPACE,
    });
  });

  it('should not emit when cancel is called with non-close argument', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    component.cancel('other');

    expect(emitSpy).not.toHaveBeenCalled();
  });

  it('should return true from toChangePermission when igId is set', () => {
    componentRef.setInput('igId', 'ig-99');
    fixture.detectChanges();

    expect(component.toChangePermission()).toBe(true);
  });

  it('should return false from toChangePermission when igId is not set', () => {
    fixture.detectChanges();

    expect(component.toChangePermission()).toBe(false);
  });
});
