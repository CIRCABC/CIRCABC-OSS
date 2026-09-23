import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  availableLanguages,
  supportedLanguages,
} from 'app/shared/langs/supported-langs';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { LangSelectorComponent } from './lang-selector.component';

describe('LangSelectorComponent', () => {
  let component: LangSelectorComponent;
  let fixture: ComponentFixture<LangSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LangSelectorComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LangSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form on ngOnInit', () => {
    expect(component.form).toBeDefined();
    expect(component.form.controls['lang']).toBeDefined();
  });

  it('should build available languages from supportedLanguages by default', () => {
    const codes = component.availableLang().map((l) => l.code);
    const expectedCodes = supportedLanguages.map((l) => l.code);
    expect(codes).toEqual(expectedCodes);
    // Each entry should also have a flag property
    component.availableLang().forEach((l) => {
      expect(l).toHaveProperty('flag');
    });
  });

  it('should build available languages from availableLanguages when worldwide is true', () => {
    fixture = TestBed.createComponent(LangSelectorComponent);
    fixture.componentRef.setInput('worldwide', true);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const codes = component.availableLang().map((l) => l.code);
    const expectedCodes = availableLanguages.map((l) => l.code);
    expect(codes).toEqual(expectedCodes);
  });

  it('should exclude disabled languages', () => {
    fixture = TestBed.createComponent(LangSelectorComponent);
    fixture.componentRef.setInput('disabledLangs', ['en', 'fr']);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const codes = component.availableLang().map((l) => l.code);
    expect(codes).not.toContain('en');
    expect(codes).not.toContain('fr');
  });

  describe('ControlValueAccessor', () => {
    it('should set form value on writeValue', () => {
      component.writeValue('fr');
      expect(component.form.controls['lang'].value).toBe('fr');
    });

    it('should reset form on writeValue with empty string', () => {
      component.writeValue('');
      expect(component.form.controls['lang'].value).toBeNull();
    });

    it('should register onChange and invoke it on language change', () => {
      const fn = vi.fn();
      component.registerOnChange(fn);
      component.onLanguageChange('test');
      expect(fn).toHaveBeenCalledWith('test');
    });

    it('should register onTouched', () => {
      const fn = vi.fn();
      component.registerOnTouched(fn);
      // onTouched is private; just verify it doesn't throw
      expect(() => component.registerOnTouched(fn)).not.toThrow();
    });
  });

  describe('onLanguageChange', () => {
    it('should set form value and emit changedLang', () => {
      const emitSpy = vi.spyOn(component.changedLang, 'emit');
      component.onLanguageChange('de');

      expect(component.form.controls['lang'].value).toBe('de');
      expect(emitSpy).toHaveBeenCalledWith('de');
      expect(component.expanded).toBe(false);
    });

    it('should call registered onChange with the value', () => {
      const fn = vi.fn();
      component.registerOnChange(fn);
      component.onLanguageChange('it');
      expect(fn).toHaveBeenCalledWith('it');
    });
  });

  describe('expand/collapse', () => {
    it('should set expanded to true on expand', () => {
      component.expand();
      expect(component.expanded).toBe(true);
    });

    it('should set expanded to false on collapse', () => {
      component.expanded = true;
      component.collapse();
      expect(component.expanded).toBe(false);
    });
  });

  it('should disable form when disable input is true', () => {
    fixture = TestBed.createComponent(LangSelectorComponent);
    fixture.componentRef.setInput('disable', true);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.form.disabled).toBe(true);
  });
});
