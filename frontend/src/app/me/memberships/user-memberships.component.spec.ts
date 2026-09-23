import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupProfile,
  Profile,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UserMembershipsComponent } from './user-memberships.component';

const mockI18nPipe = {
  transform: vi.fn((mltext: { [key: string]: string } | undefined) => {
    if (!mltext) return '';
    return mltext['en'] ?? '';
  }),
};

describe('UserMembershipsComponent', () => {
  let component: UserMembershipsComponent;
  let componentRef: ComponentRef<UserMembershipsComponent>;
  let fixture: ComponentFixture<UserMembershipsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserMembershipsComponent],
      providers: [
        provideRouter([]),
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

    fixture = TestBed.createComponent(UserMembershipsComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('memberships', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('getGroupDisplay', () => {
    it('should return "unknown" when interestGroup is undefined', () => {
      expect(component.getGroupDisplay(undefined)).toBe('unknown');
    });

    it('should return the name when title is not set', () => {
      const ig: InterestGroup = { name: 'TestGroup', permissions: {} };
      expect(component.getGroupDisplay(ig)).toBe('TestGroup');
    });

    it('should return the translated title when available', () => {
      mockI18nPipe.transform.mockReturnValueOnce('Translated Title');
      const ig: InterestGroup = {
        name: 'TestGroup',
        title: { en: 'Translated Title' },
        permissions: {},
      };
      expect(component.getGroupDisplay(ig)).toBe('Translated Title');
    });

    it('should return name when title translation is empty', () => {
      mockI18nPipe.transform.mockReturnValueOnce('');
      const ig: InterestGroup = {
        name: 'TestGroup',
        title: { fr: 'Titre' },
        permissions: {},
      };
      expect(component.getGroupDisplay(ig)).toBe('TestGroup');
    });
  });

  describe('getProfileDisplay', () => {
    it('should return empty string when profile is undefined', () => {
      expect(component.getProfileDisplay(undefined)).toBe('');
    });

    it('should return the profile name when title is not set', () => {
      const profile: Profile = { name: 'Admin' };
      expect(component.getProfileDisplay(profile)).toBe('Admin');
    });

    it('should return empty string when profile has no name and no title', () => {
      const profile: Profile = {};
      expect(component.getProfileDisplay(profile)).toBe('');
    });

    it('should return the translated title when available', () => {
      mockI18nPipe.transform.mockReturnValueOnce('Administrator');
      const profile: Profile = {
        name: 'Admin',
        title: { en: 'Administrator' },
      };
      expect(component.getProfileDisplay(profile)).toBe('Administrator');
    });

    it('should return name when title translation is empty', () => {
      mockI18nPipe.transform.mockReturnValueOnce('');
      const profile: Profile = {
        name: 'Admin',
        title: { fr: 'Administrateur' },
      };
      expect(component.getProfileDisplay(profile)).toBe('Admin');
    });
  });

  describe('getCurrentLang', () => {
    it('should return the active language', () => {
      expect(component.getCurrentLang()).toBe('en');
    });
  });

  describe('getDefaultLang', () => {
    it('should return the default language', () => {
      expect(component.getDefaultLang()).toBe('en');
    });
  });

  describe('loading input', () => {
    it('should default to false', () => {
      expect(component.loading()).toBe(false);
    });

    it('should reflect the provided value', () => {
      componentRef.setInput('loading', true);
      fixture.detectChanges();
      expect(component.loading()).toBe(true);
    });
  });

  describe('memberships input', () => {
    it('should reflect provided memberships', () => {
      const memberships: InterestGroupProfile[] = [
        {
          interestGroup: { name: 'Group1', permissions: {} },
          profile: { name: 'Member' },
        },
      ];
      componentRef.setInput('memberships', memberships);
      fixture.detectChanges();
      expect(component.memberships()).toEqual(memberships);
    });
  });
});
