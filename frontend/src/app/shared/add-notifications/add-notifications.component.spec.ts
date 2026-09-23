import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { NotificationService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddNotificationsComponent } from './add-notifications.component';

const mockNotificationService = {
  postNotification: vi.fn(),
  postNotificationAsync: vi.fn(),
};

describe('AddNotificationsComponent', () => {
  let component: AddNotificationsComponent;
  let componentRef: ComponentRef<AddNotificationsComponent>;
  let fixture: ComponentFixture<AddNotificationsComponent>;

  beforeEach(async () => {
    mockNotificationService.postNotification.mockReset();
    mockNotificationService.postNotificationAsync.mockReset();

    await TestBed.configureTestingModule({
      imports: [AddNotificationsComponent],
      providers: [
        { provide: NotificationService, useValue: mockNotificationService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(AddNotificationsComponent, {
        set: {
          imports: [TranslocoModule],
          schemas: [NO_ERRORS_SCHEMA],
          template: '',
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(AddNotificationsComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('node', { id: 'node-1', name: 'Test Node' });
    componentRef.setInput('ig', 'ig-1');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form on ngOnInit', () => {
    expect(component.newNotificationForm).toBeDefined();
    expect(
      component.newNotificationForm.controls['invitedUsersOrProfiles']
    ).toBeDefined();
  });

  describe('cancelWizard', () => {
    it('should emit a CANCELED result and reset state', () => {
      const finishedSpy = vi.spyOn(component.finished, 'emit');
      component.newNotifModel = {
        user1: { authority: { userId: 'u1' }, notifications: 'ALLOWED' },
      };
      component.configModel = [
        {
          authKey: 'user1',
          authValue: { authority: { userId: 'u1' }, notifications: 'ALLOWED' },
        },
      ];

      component.cancelWizard();

      expect(component.showModal()).toBe(false);
      expect(component.newNotifModel).toEqual({});
      expect(component.configModel).toEqual([]);
      expect(finishedSpy).toHaveBeenCalledWith({
        type: ActionType.ADD_NOTIFICATIONS,
        result: ActionResult.CANCELED,
      });
    });
  });

  describe('addNotifications', () => {
    it('should post notifications for users and emit SUCCEED', async () => {
      mockNotificationService.postNotificationAsync.mockResolvedValue({});
      const finishedSpy = vi.spyOn(component.finished, 'emit');

      component.newNotifModel = {
        user1: {
          authority: { userId: 'u1', firstname: 'John', lastname: 'Doe' },
          notifications: 'ALLOWED',
        },
      };

      await component.addNotifications();

      expect(
        mockNotificationService.postNotificationAsync
      ).toHaveBeenCalledWith({
        id: 'node-1',
        notificationDefinition: {
          profiles: [],
          users: [
            {
              notifications: 'ALLOWED',
              user: { userId: 'u1', firstname: 'John', lastname: 'Doe' },
            },
          ],
        },
      });
      expect(component.adding()).toBe(false);
      expect(component.newNotifModel).toEqual({});
      expect(finishedSpy).toHaveBeenCalledWith({
        type: ActionType.ADD_NOTIFICATIONS,
        result: ActionResult.SUCCEED,
      });
    });

    it('should post notifications for profiles', async () => {
      mockNotificationService.postNotificationAsync.mockResolvedValue({});

      component.newNotifModel = {
        group1: {
          authority: { groupName: 'group1', name: 'Admins' },
          notifications: 'DENIED',
        },
      };

      await component.addNotifications();

      expect(
        mockNotificationService.postNotificationAsync
      ).toHaveBeenCalledWith({
        id: 'node-1',
        notificationDefinition: {
          profiles: [
            {
              notifications: 'DENIED',
              profile: { groupName: 'group1', name: 'Admins' },
            },
          ],
          users: [],
        },
      });
    });

    it('should emit FAILED on error', async () => {
      mockNotificationService.postNotificationAsync.mockRejectedValue(
        new Error('fail')
      );
      const finishedSpy = vi.spyOn(component.finished, 'emit');

      component.newNotifModel = {
        user1: {
          authority: { userId: 'u1' },
          notifications: 'ALLOWED',
        },
      };

      await component.addNotifications();

      expect(component.adding()).toBe(false);
      expect(finishedSpy).toHaveBeenCalledWith({
        type: ActionType.ADD_NOTIFICATIONS,
        result: ActionResult.FAILED,
      });
    });
  });

  describe('assign', () => {
    it('should add users to newNotifModel from form value', () => {
      component.newNotificationForm.setValue({
        invitedUsersOrProfiles: [
          { userId: 'u1', firstname: 'A', lastname: 'B' },
        ],
      });

      component.assign();

      expect(component.newNotifModel['u1']).toEqual({
        authority: { userId: 'u1', firstname: 'A', lastname: 'B' },
        notifications: 'ALLOWED',
      });
      expect(component.configModel).toHaveLength(1);
    });

    it('should add profiles to newNotifModel from form value', () => {
      component.newNotificationForm.setValue({
        invitedUsersOrProfiles: [{ groupName: 'g1', name: 'Group1' }],
      });

      component.assign();

      expect(component.newNotifModel['g1']).toEqual({
        authority: { groupName: 'g1', name: 'Group1' },
        notifications: 'ALLOWED',
      });
    });
  });

  describe('getAuthorityDisplay', () => {
    it('should return full name for a user', () => {
      const result = component.getAuthorityDisplay({
        userId: 'u1',
        firstname: 'John',
        lastname: 'Doe',
      });
      expect(result).toBe('John Doe');
    });

    it('should return profile name for a profile', () => {
      const result = component.getAuthorityDisplay({
        groupName: 'g1',
        name: 'Admins',
      });
      expect(result).toBe('Admins');
    });
  });

  describe('removeNotification', () => {
    it('should remove the entry and update configModel', () => {
      component.newNotifModel = {
        user1: { authority: { userId: 'u1' }, notifications: 'ALLOWED' },
        user2: { authority: { userId: 'u2' }, notifications: 'DENIED' },
      };

      component.removeNotification('user1');

      expect(component.newNotifModel['user1']).toBeUndefined();
      expect(component.configModel).toHaveLength(1);
      expect(component.configModel[0].authKey).toBe('user2');
    });
  });

  describe('isShowAddButton', () => {
    it('should return false when no notifications configured', () => {
      component.newNotifModel = {};
      expect(component.isShowAddButton()).toBe(false);
    });

    it('should return true when notifications exist', () => {
      component.newNotifModel = {
        user1: { authority: { userId: 'u1' }, notifications: 'ALLOWED' },
      };
      expect(component.isShowAddButton()).toBe(true);
    });
  });

  describe('switchValue', () => {
    it('should toggle from ALLOWED to DENIED', () => {
      component.newNotifModel = {
        user1: { authority: { userId: 'u1' }, notifications: 'ALLOWED' },
      };

      component.switchValue('user1');

      expect(component.newNotifModel['user1'].notifications).toBe('DENIED');
    });

    it('should toggle from DENIED to ALLOWED', () => {
      component.newNotifModel = {
        user1: { authority: { userId: 'u1' }, notifications: 'DENIED' },
      };

      component.switchValue('user1');

      expect(component.newNotifModel['user1'].notifications).toBe('ALLOWED');
    });
  });
});
