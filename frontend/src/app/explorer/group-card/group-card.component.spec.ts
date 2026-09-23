import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { type InterestGroup } from 'app/core/generated/circabc';
import { SERVER_URL } from 'app/core/variables';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { GroupCardComponent } from './group-card.component';

describe('GroupCardComponent', () => {
  let component: GroupCardComponent;
  let componentRef: ComponentRef<GroupCardComponent>;
  let fixture: ComponentFixture<GroupCardComponent>;

  const mockI18nPipe = {
    transform: vi.fn(
      (val: { [key: string]: string } | undefined) => val?.['en'] ?? ''
    ),
  };

  const mockGroup: InterestGroup = {
    name: 'Test Group',
    title: { en: 'English Title' },
    permissions: { library: 'LibAdmin' },
    id: '123',
    logoUrl: undefined,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupCardComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: I18nPipe, useValue: mockI18nPipe },
        { provide: SERVER_URL, useValue: 'http://localhost/' },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GroupCardComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('group', mockGroup);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('getGroupNameOrTitle', () => {
    it('should return transformed title when title exists', () => {
      mockI18nPipe.transform.mockReturnValue('English Title');
      expect(component.getGroupNameOrTitle()).toBe('English Title');
    });

    it('should return name when title is empty', () => {
      componentRef.setInput('group', { ...mockGroup, title: {} });
      fixture.detectChanges();
      expect(component.getGroupNameOrTitle()).toBe('Test Group');
    });

    it('should return name when title is undefined', () => {
      componentRef.setInput('group', { ...mockGroup, title: undefined });
      fixture.detectChanges();
      expect(component.getGroupNameOrTitle()).toBe('Test Group');
    });
  });

  describe('getLink', () => {
    it('should return link with group id', () => {
      Object.defineProperty(globalThis, 'location', {
        value: { href: 'http://localhost/explore/category' },
        writable: true,
      });
      expect(component.getLink()).toBe('http://localhost/group/123');
    });
  });

  describe('hasLogo', () => {
    it('should return false when logoUrl is undefined', () => {
      expect(component.hasLogo()).toBe(false);
    });

    it('should return true when logoUrl is set', () => {
      componentRef.setInput('group', {
        ...mockGroup,
        logoUrl: 'workspace://SpacesStore/abc-123',
      });
      expect(component.hasLogo()).toBe(true);
    });
  });

  describe('getLogoUrl', () => {
    it('should return empty string when no logoUrl', () => {
      expect(component.getLogoUrl()).toBe('');
    });

    it('should strip workspace prefix from logoUrl', () => {
      componentRef.setInput('group', {
        ...mockGroup,
        logoUrl: 'workspace://SpacesStore/abc-123',
      });
      expect(component.getLogoUrl()).toBe('abc-123');
    });

    it('should return empty string when logoUrl does not contain workspace', () => {
      componentRef.setInput('group', {
        ...mockGroup,
        logoUrl: 'some-other-url',
      });
      expect(component.getLogoUrl()).toBe('');
    });
  });
});
