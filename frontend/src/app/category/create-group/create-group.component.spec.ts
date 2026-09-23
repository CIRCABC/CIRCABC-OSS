import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result/index';
import {
  CategoryService,
  InterestGroup,
  UserService,
} from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { CreateGroupComponent } from './create-group.component';

describe('CreateGroupComponent', () => {
  let component: CreateGroupComponent;
  let fixture: ComponentFixture<CreateGroupComponent>;

  const paramsSubject = new Subject<{ id: string }>();

  const mockCategoryService = {
    postInterestGroup: vi.fn(),
    postInterestGroupAsync: vi.fn(),
  };

  const mockUserService = {
    getUsersAsync: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateGroupComponent],
      providers: [
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: UserService, useValue: mockUserService },
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
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

    fixture = TestBed.createComponent(CreateGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    paramsSubject.next({ id: 'cat-123' });
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should set categoryId from route params', () => {
    expect(component.categoryId).toBe('cat-123');
  });

  describe('form navigation', () => {
    it('should show details form initially', () => {
      expect(component.showDetailsForm()).toBe(true);
      expect(component.showLeadersForm()).toBe(false);
      expect(component.showNotificationForm()).toBe(false);
    });

    it('setLeadersForm should show leaders form', () => {
      component.setLeadersForm();
      expect(component.showDetailsForm()).toBe(false);
      expect(component.showLeadersForm()).toBe(true);
      expect(component.showNotificationForm()).toBe(false);
    });

    it('setNotificationForm should show notification form', () => {
      component.setNotificationForm();
      expect(component.showDetailsForm()).toBe(false);
      expect(component.showLeadersForm()).toBe(false);
      expect(component.showNotificationForm()).toBe(true);
    });

    it('setDetailsForm should show details form', () => {
      component.setLeadersForm();
      component.setDetailsForm();
      expect(component.showDetailsForm()).toBe(true);
      expect(component.showLeadersForm()).toBe(false);
    });
  });

  describe('okAction', () => {
    it('should navigate from details to leaders', async () => {
      component.setDetailsForm();
      await component.okAction();
      expect(component.showLeadersForm()).toBe(true);
    });

    it('should navigate from leaders to notification', async () => {
      component.setLeadersForm();
      await component.okAction();
      expect(component.showNotificationForm()).toBe(true);
    });

    it('should call createGroup from notification form', async () => {
      const group: InterestGroup = {
        id: 'grp-1',
        name: 'Test',
        permissions: {},
      };
      mockCategoryService.postInterestGroupAsync.mockResolvedValue(group);

      component.setNotificationForm();
      component.futureMembers.set([{ userId: 'user1' }]);
      component.groupDetailsForm.patchValue({
        name: 'test',
        title: { en: 'T' },
        description: { en: 'D' },
        contact: { en: 'C' },
      });
      component.groupNotificationForm.patchValue({
        notify: false,
        notificationText: { en: 'N' },
      });

      const emitSpy = vi.spyOn(component.modalClosed, 'emit');
      await component.okAction();

      expect(mockCategoryService.postInterestGroupAsync).toHaveBeenCalledWith({
        id: 'cat-123',
        interestGroupPostModel: expect.objectContaining({ leaders: ['user1'] }),
      });
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.SUCCEED,
          type: ActionType.CREATE_INTEREST_GROUP,
        })
      );
    });
  });

  describe('createGroup', () => {
    beforeEach(() => {
      component.groupDetailsForm.patchValue({
        name: 'grp',
        title: { en: 'T' },
        description: { en: 'D' },
        contact: { en: 'C' },
      });
      component.groupNotificationForm.patchValue({
        notify: true,
        notificationText: { en: 'hello' },
      });
    });

    it('should emit SUCCEED on success', async () => {
      const group: InterestGroup = {
        id: 'new-id',
        name: 'grp',
        permissions: {},
      };
      mockCategoryService.postInterestGroupAsync.mockResolvedValue(group);
      component.futureMembers.set([{ userId: 'u1' }, { userId: 'u2' }]);

      const emitSpy = vi.spyOn(component.modalClosed, 'emit');
      await component.createGroup();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.SUCCEED,
          node: { id: 'new-id' },
        })
      );
      expect(component.processing()).toBe(false);
    });

    it('should emit FAILED when group already exists', async () => {
      mockCategoryService.postInterestGroupAsync.mockRejectedValue({
        error: { message: 'Group already exists.' },
      });

      const emitSpy = vi.spyOn(component.modalClosed, 'emit');
      await component.createGroup();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.FAILED,
          type: ActionType.CREATE_INTEREST_GROUP_EXISTS,
        })
      );
    });
  });

  describe('cancel', () => {
    it('should emit CANCELED result', () => {
      const emitSpy = vi.spyOn(component.modalClosed, 'emit');
      component.cancel();
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.CANCELED,
          type: ActionType.CREATE_INTEREST_GROUP,
        })
      );
    });
  });

  describe('searchUsers', () => {
    it('should search users when search is not empty', async () => {
      const users = [{ userId: 'u1', firstname: 'John' }];
      mockUserService.getUsersAsync.mockResolvedValue(users);
      component.groupLeadersForm.controls['search'].setValue('john');

      await component.searchUsers();

      expect(mockUserService.getUsersAsync).toHaveBeenCalledWith({
        query: 'john',
        filter: false,
      });
      expect(component.availableUsers()).toEqual(users);
      expect(component.searchingUsers()).toBe(false);
    });

    it('should not search when search expression is empty', async () => {
      mockUserService.getUsersAsync.mockClear();
      component.groupLeadersForm.controls['search'].setValue('');

      await component.searchUsers();

      expect(mockUserService.getUsersAsync).not.toHaveBeenCalled();
    });
  });

  describe('selectUsers', () => {
    it('should add selected users to futureMembers', () => {
      component.availableUsers.set([
        { userId: 'u1', firstname: 'A' },
        { userId: 'u2', firstname: 'B' },
      ]);
      component.groupLeadersForm.controls['possibleUsers'].setValue([
        'u1',
        'u2',
      ]);

      component.selectUsers();

      expect(component.futureMembers()).toEqual([
        { userId: 'u1', firstname: 'A' },
        { userId: 'u2', firstname: 'B' },
      ]);
    });

    it('should not add duplicate users', () => {
      component.futureMembers.set([{ userId: 'u1', firstname: 'A' }]);
      component.availableUsers.set([{ userId: 'u1', firstname: 'A' }]);
      component.groupLeadersForm.controls['possibleUsers'].setValue(['u1']);

      component.selectUsers();

      expect(component.futureMembers()).toHaveLength(1);
    });
  });

  describe('removeFromFutureMember', () => {
    it('should remove the specified user', () => {
      const user = { userId: 'u1' };
      component.futureMembers.set([user, { userId: 'u2' }]);

      component.removeFromFutureMember(user);

      expect(component.futureMembers()).toEqual([{ userId: 'u2' }]);
    });
  });

  describe('areFormsValid', () => {
    it('should return true when on details form', () => {
      component.setDetailsForm();
      expect(component.areFormsValid()).toBe(true);
    });

    it('should return true when on leaders form', () => {
      component.setLeadersForm();
      expect(component.areFormsValid()).toBe(true);
    });

    it('should return false on notification form with no members', () => {
      component.setNotificationForm();
      component.futureMembers.set([]);
      expect(component.areFormsValid()).toBe(false);
    });
  });

  describe('getOkLabel', () => {
    it('should return next on details form', () => {
      component.setDetailsForm();
      expect(component.getOkLabel()).toBe('label.next');
    });

    it('should return create on notification form', () => {
      component.setNotificationForm();
      expect(component.getOkLabel()).toBe('label.create');
    });
  });
});
