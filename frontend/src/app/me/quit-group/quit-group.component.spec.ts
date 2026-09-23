import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { type InterestGroup, MembersService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { QuitGroupComponent } from './quit-group.component';

const mockGroup: InterestGroup = {
  id: 'group-123',
  name: 'Test Group',
  title: { en: 'Test Group Title' },
  permissions: {},
};

describe('QuitGroupComponent', () => {
  let component: QuitGroupComponent;
  let componentRef: ComponentRef<QuitGroupComponent>;
  let fixture: ComponentFixture<QuitGroupComponent>;

  const mockMembersService = {
    deleteMember: vi.fn().mockReturnValue(of(undefined)),
    deleteMemberAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockUiMessageService = {
    addSuccessMessage: vi.fn(),
    addErrorMessage: vi.fn(),
  };

  const mockI18nPipe = {
    transform: vi.fn((value: { [key: string]: string }) => value?.['en'] ?? ''),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [QuitGroupComponent],
      providers: [
        { provide: MembersService, useValue: mockMembersService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(QuitGroupComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('group', mockGroup);
    componentRef.setInput('username', 'testuser');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('removeMembership', () => {
    it('should call deleteMember and emit events on success', async () => {
      const showChangeSpy = vi.spyOn(component.showChange, 'emit');
      const membershipRemovedSpy = vi.spyOn(
        component.membershipRemoved,
        'emit'
      );

      await component.removeMembership();

      expect(mockMembersService.deleteMemberAsync).toHaveBeenCalledWith({
        id: 'group-123',
        userId: 'testuser',
      });
      expect(component.show()).toBe(false);
      expect(showChangeSpy).toHaveBeenCalledWith(false);
      expect(membershipRemovedSpy).toHaveBeenCalled();
      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
      expect(component.processing()).toBe(false);
    });

    it('should show error message on failure', async () => {
      mockMembersService.deleteMemberAsync.mockRejectedValue(new Error('fail'));

      await component.removeMembership();

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
      expect(component.processing()).toBe(false);
    });

    it('should not call deleteMember if group has no id', async () => {
      componentRef.setInput('group', {
        name: 'No ID',
        permissions: {},
      } as InterestGroup);
      fixture.detectChanges();

      await component.removeMembership();

      expect(mockMembersService.deleteMemberAsync).not.toHaveBeenCalled();
      expect(component.processing()).toBe(false);
    });
  });

  describe('cancel', () => {
    it('should set show to false and emit showChange', () => {
      component.show.set(true);
      const showChangeSpy = vi.spyOn(component.showChange, 'emit');

      component.cancel();

      expect(component.show()).toBe(false);
      expect(showChangeSpy).toHaveBeenCalledWith(false);
    });
  });

  describe('getGroupLabel', () => {
    it('should return translated title when available', () => {
      expect(component.getGroupLabel()).toBe('Test Group Title');
    });

    it('should return name when title translation is empty', () => {
      mockI18nPipe.transform.mockReturnValue('');
      componentRef.setInput('group', { ...mockGroup, title: { en: '' } });
      fixture.detectChanges();

      expect(component.getGroupLabel()).toBe('Test Group');
    });

    it('should return name when no title exists', () => {
      componentRef.setInput('group', {
        name: 'Only Name',
        permissions: {},
      } as InterestGroup);
      fixture.detectChanges();

      expect(component.getGroupLabel()).toBe('Only Name');
    });
  });
});
