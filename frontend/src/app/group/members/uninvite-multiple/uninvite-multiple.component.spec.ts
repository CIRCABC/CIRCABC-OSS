import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { MembersService } from 'app/core/generated/circabc';
import { SelectableUserProfile } from 'app/core/ui-model/index';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UninviteMultipleComponent } from './uninvite-multiple.component';

const mockMembersService = {
  deleteMember: vi.fn().mockReturnValue(of(undefined)),
  deleteMemberAsync: vi.fn().mockResolvedValue(undefined),
};

describe('UninviteMultipleComponent', () => {
  let component: UninviteMultipleComponent;
  let componentRef: ComponentRef<UninviteMultipleComponent>;
  let fixture: ComponentFixture<UninviteMultipleComponent>;

  const users: SelectableUserProfile[] = [
    {
      user: { userId: 'user1', firstname: 'A', lastname: 'B' },
      profile: { title: {} },
    },
    {
      user: { userId: 'user2', firstname: 'C', lastname: 'D' },
      profile: { title: {} },
    },
  ];

  beforeEach(async () => {
    vi.clearAllMocks();
    mockMembersService.deleteMember.mockReturnValue(of(undefined));
    mockMembersService.deleteMemberAsync.mockResolvedValue(undefined);

    await TestBed.configureTestingModule({
      imports: [UninviteMultipleComponent],
      providers: [
        { provide: MembersService, useValue: mockMembersService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(UninviteMultipleComponent, {
        set: {
          imports: [TranslocoModule, I18nPipe],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(UninviteMultipleComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('users', users);
    componentRef.setInput('groupId', 'group1');
    componentRef.setInput('showModal', true);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('deleteMembers', () => {
    it('should call deleteMember for each user and emit SUCCEED', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.deleteMembers();

      expect(mockMembersService.deleteMemberAsync).toHaveBeenCalledWith({
        id: 'group1',
        userId: 'user1',
      });
      expect(mockMembersService.deleteMemberAsync).toHaveBeenCalledWith({
        id: 'group1',
        userId: 'user2',
      });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.REMOVE_MEMBERSHIPS,
        result: ActionResult.SUCCEED,
      });
      expect(component.deleting()).toBe(false);
      expect(component.showModal()).toBe(false);
    });

    it('should emit FAILED when deleteMember throws', async () => {
      mockMembersService.deleteMemberAsync.mockRejectedValue(new Error('fail'));
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.deleteMembers();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.REMOVE_MEMBERSHIPS,
        result: ActionResult.FAILED,
      });
      expect(component.deleting()).toBe(false);
      expect(component.showModal()).toBe(false);
    });

    it('should skip users without userId', async () => {
      componentRef.setInput('users', [
        { user: {}, profile: { title: {} } },
        { profile: { title: {} } },
      ] as SelectableUserProfile[]);
      fixture.detectChanges();

      await component.deleteMembers();

      expect(mockMembersService.deleteMemberAsync).not.toHaveBeenCalled();
    });
  });

  describe('cancel', () => {
    it('should emit CANCELED and hide modal', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancel();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.REMOVE_MEMBERSHIPS,
        result: ActionResult.CANCELED,
      });
      expect(component.showModal()).toBe(false);
    });
  });
});
