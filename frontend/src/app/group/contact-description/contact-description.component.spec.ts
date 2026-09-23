import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { type InterestGroup } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ContactDescriptionComponent } from './contact-description.component';

describe('ContactDescriptionComponent', () => {
  let component: ContactDescriptionComponent;
  let componentRef: ComponentRef<ContactDescriptionComponent>;
  let fixture: ComponentFixture<ContactDescriptionComponent>;
  let translocoService: TranslocoService;

  const mockGroup: InterestGroup = {
    name: 'Test Group',
    permissions: {},
    description: { en: 'English description', fr: 'Description française' },
    contact: { en: 'contact@test.com', fr: 'contact-fr@test.com' },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContactDescriptionComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en', 'fr'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ContactDescriptionComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    translocoService = TestBed.inject(TranslocoService);
  });

  it('should create', () => {
    componentRef.setInput('group', mockGroup);
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  describe('hasContact', () => {
    it('should return true when contact has value for active lang', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.hasContact()).toBe(true);
    });

    it('should return false when contact is undefined', () => {
      componentRef.setInput('group', { ...mockGroup, contact: undefined });
      fixture.detectChanges();
      expect(component.hasContact()).toBe(false);
    });

    it('should return false when contact has empty string for lang', () => {
      componentRef.setInput('group', { ...mockGroup, contact: { en: '' } });
      fixture.detectChanges();
      expect(component.hasContact()).toBe(false);
    });
  });

  describe('hasMLValue', () => {
    it('should return true when obj has value for active lang', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.hasMLValue({ en: 'value' })).toBe(true);
    });

    it('should return false when obj has no value for active lang', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.hasMLValue({ de: 'value' })).toBe(false);
    });

    it('should return false when obj value is empty string', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.hasMLValue({ en: '' })).toBe(false);
    });
  });

  describe('getLang', () => {
    it('should return active lang when description contains it', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.getLang()).toBe('en');
    });

    it('should return default lang when description does not contain active lang', () => {
      vi.spyOn(translocoService, 'getActiveLang').mockReturnValue('de');
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.getLang()).toBe('en');
    });

    it('should return default lang when description is undefined', () => {
      componentRef.setInput('group', { ...mockGroup, description: undefined });
      fixture.detectChanges();
      expect(component.getLang()).toBe('en');
    });
  });
});
