import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { MembersService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UninviteUserComponent } from './uninvite-user.component';

describe('UninviteUserComponent', () => {
  let component: UninviteUserComponent;
  let componentRef: ComponentRef<UninviteUserComponent>;
  let fixture: ComponentFixture<UninviteUserComponent>;

  const mockMembersService = {
    deleteMember: vi.fn().mockReturnValue(of(undefined)),
    deleteMemberAsync: vi.fn().mockResolvedValue(undefined),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UninviteUserComponent],
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
    }).compileComponents();

    fixture = TestBed.createComponent(UninviteUserComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('groupId', 'group1');
    componentRef.setInput('showDialog', true);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('cancelWizard', () => {
    it('should emit CANCELED and hide dialog when backTo is close', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancelWizard('close');
      expect(component.showDialog()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
      });
    });

    it('should do nothing when backTo is not close', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancelWizard('other');
      expect(component.showDialog()).toBe(true);
      expect(emitSpy).not.toHaveBeenCalled();
    });
  });

  describe('uninviteMember', () => {
    it('should call deleteMember and emit SUCCEED result', async () => {
      componentRef.setInput('user', { userId: 'user1' });
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      await component.uninviteMember();

      expect(mockMembersService.deleteMemberAsync).toHaveBeenCalledWith({
        id: 'group1',
        userId: 'user1',
      });
      expect(component.showDialog()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.REMOVE_MEMBERSHIP,
      });
      expect(component.removing()).toBe(false);
    });

    it('should not call deleteMember when user is undefined', async () => {
      mockMembersService.deleteMemberAsync.mockClear();
      await component.uninviteMember();
      expect(mockMembersService.deleteMemberAsync).not.toHaveBeenCalled();
    });
  });
});
