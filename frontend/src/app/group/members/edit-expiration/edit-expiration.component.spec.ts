import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import { ActionType } from 'app/action-result/action-type';
import { MembersService, UserProfile } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { EditExpirationComponent } from './edit-expiration.component';

const mockMembersService = {
  deleteMemberExpiration: vi.fn().mockReturnValue(of(undefined)),
  deleteMemberExpirationAsync: vi.fn().mockResolvedValue(undefined),
  updateMemberExpiration: vi.fn().mockReturnValue(of(undefined)),
  updateMemberExpirationAsync: vi.fn().mockResolvedValue(undefined),
  createMemberExpiration: vi.fn().mockReturnValue(of(undefined)),
  createMemberExpirationAsync: vi.fn().mockResolvedValue(undefined),
};

function createMember(userId: string, expirationDate?: string): UserProfile {
  return {
    user: { userId },
    profile: { id: 'prof1', groupName: 'GROUP_group1' },
    expirationDate,
  };
}

describe('EditExpirationComponent', () => {
  let component: EditExpirationComponent;
  let componentRef: ComponentRef<EditExpirationComponent>;
  let fixture: ComponentFixture<EditExpirationComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [EditExpirationComponent],
      providers: [
        provideNativeDateAdapter(),
        { provide: MembersService, useValue: mockMembersService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditExpirationComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('showModal', true);
    componentRef.setInput('members', []);
    componentRef.setInput('groupId', 'group1');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form with default values', () => {
    expect(component.expirationForm.value.expiration).toBe(false);
    expect(component.expirationForm.value.showOkButton).toBe(false);
  });

  it('should enable date control when expiration is toggled on', () => {
    component.expirationForm.controls['expiration'].setValue(true);
    expect(
      component.expirationForm.controls['expirationDateTime'].enabled
    ).toBe(true);
    expect(
      component.expirationForm.controls['expirationDateTime'].value
    ).toEqual(component.minDate);
  });

  it('should disable date control when expiration is toggled off', () => {
    component.expirationForm.controls['expiration'].setValue(true);
    component.expirationForm.controls['expiration'].setValue(false);
    expect(
      component.expirationForm.controls['expirationDateTime'].disabled
    ).toBe(true);
  });

  it('should update membersDisplay on input change', () => {
    const members: UserProfile[] = [
      createMember('user1', '2026-12-31T00:00:00.000Z'),
    ];
    componentRef.setInput('members', members);
    fixture.detectChanges();

    expect(component.membersDisplay).toHaveLength(1);
    expect(component.expirationForm.controls['expiration'].value).toBe(true);
  });

  it('should set expirationDateTime for single member with expiration', () => {
    const members: UserProfile[] = [
      createMember('user1', '2026-12-31T00:00:00.000Z'),
    ];
    componentRef.setInput('members', members);
    fixture.detectChanges();

    const dateValue =
      component.expirationForm.controls['expirationDateTime'].value;
    expect(dateValue).toBeInstanceOf(Date);
  });

  it('should emit cancel result on cancel()', () => {
    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    component.cancel();
    expect(emitSpy).toHaveBeenCalledWith({
      type: ActionType.EDIT_EXPIRATION,
      result: ActionResult.CANCELED,
    });
  });

  it('should call deleteMemberExpiration when expiration is off', async () => {
    const members: UserProfile[] = [
      createMember('user1', '2026-12-31T00:00:00.000Z'),
    ];
    componentRef.setInput('members', members);
    fixture.detectChanges();

    component.expirationForm.controls['expiration'].setValue(false);
    await component.editExpiration();

    expect(mockMembersService.deleteMemberExpirationAsync).toHaveBeenCalledWith(
      {
        id: 'group1',
        userId: 'user1',
      }
    );
  });

  it('should call createMemberExpiration for member without existing expiration', async () => {
    const members: UserProfile[] = [createMember('user1')];
    componentRef.setInput('members', members);
    fixture.detectChanges();

    component.expirationForm.controls['expiration'].setValue(true);
    const futureDate = new Date('2027-06-15T00:00:00.000Z');
    component.expirationForm.controls['expirationDateTime'].setValue(
      futureDate
    );

    await component.editExpiration();

    expect(mockMembersService.createMemberExpirationAsync).toHaveBeenCalledWith(
      {
        id: 'group1',
        userId: 'user1',
        expirationDate: expect.any(String),
        profileId: 'prof1',
        alfrescoGroup: 'GROUP_group1',
      }
    );
  });

  it('should call updateMemberExpiration for member with existing expiration', async () => {
    const members: UserProfile[] = [
      createMember('user1', '2026-12-31T00:00:00.000Z'),
    ];
    componentRef.setInput('members', members);
    fixture.detectChanges();

    const futureDate = new Date('2027-06-15T00:00:00.000Z');
    component.expirationForm.controls['expirationDateTime'].setValue(
      futureDate
    );

    await component.editExpiration();

    expect(mockMembersService.updateMemberExpirationAsync).toHaveBeenCalledWith(
      {
        id: 'group1',
        userId: 'user1',
        expirationDate: futureDate.toISOString(),
      }
    );
  });

  it('should emit FAILED result when service throws', async () => {
    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    const members: UserProfile[] = [
      createMember('user1', '2026-12-31T00:00:00.000Z'),
    ];
    componentRef.setInput('members', members);
    fixture.detectChanges();

    component.expirationForm.controls['expiration'].setValue(false);
    mockMembersService.deleteMemberExpirationAsync.mockRejectedValue(
      new Error('fail')
    );

    await component.editExpiration();

    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({ result: ActionResult.FAILED })
    );
  });

  it('should set processing to false after editExpiration completes', async () => {
    component.expirationForm.controls['expiration'].setValue(false);
    await component.editExpiration();
    expect(component.processing()).toBe(false);
  });
});
