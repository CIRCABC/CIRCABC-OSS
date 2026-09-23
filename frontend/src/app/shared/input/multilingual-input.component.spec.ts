import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { MultilingualInputComponent } from './multilingual-input.component';

@Component({
  template: `<cbc-multilingual-input label="Test" />`,
  imports: [MultilingualInputComponent],
})
class TestHostComponent {}

describe('MultilingualInputComponent', () => {
  let component: MultilingualInputComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TestHostComponent,
        MultilingualInputComponent,
        ReactiveFormsModule,
      ],
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

    fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();
    component = fixture.debugElement.children[0].componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with controls for all supported languages', () => {
    expect(component.form).toBeDefined();
    expect(component.form.contains('en')).toBe(true);
    expect(component.form.contains('fr')).toBe(true);
  });

  it('should initialize formSelector with language and text controls', () => {
    expect(component.formSelector).toBeDefined();
    expect(component.formSelector.controls['language']).toBeDefined();
    expect(component.formSelector.controls['text']).toBeDefined();
  });

  it('should default lang to en', () => {
    expect(component.lang()).toBe('en');
  });

  describe('writeValue', () => {
    it('should populate model from provided values', () => {
      component.writeValue({ en: 'Hello', fr: 'Bonjour' });

      expect(component.model['en'].value).toBe('Hello');
      expect(component.model['en'].display).toBe(true);
      expect(component.model['fr'].value).toBe('Bonjour');
      expect(component.model['fr'].display).toBe(true);
    });

    it('should handle null values', () => {
      component.writeValue(null);

      expect(component.model['en'].value).toBe('');
      expect(component.model['en'].display).toBe(false);
    });

    it('should set showTranslationPanel to true', () => {
      component.showTranslationPanel.set(false);
      component.writeValue({ en: 'test' });

      expect(component.showTranslationPanel()).toBe(true);
    });
  });

  describe('registerOnChange', () => {
    it('should register the onChange callback', () => {
      const fn = vi.fn();
      component.registerOnChange(fn);

      component.form.controls['en'].patchValue('test');
      expect(fn).toHaveBeenCalled();
    });
  });

  describe('registerOnTouched', () => {
    it('should register the onTouched callback', () => {
      const fn = vi.fn();
      component.registerOnTouched(fn);
      expect(component.onTouched).toBe(fn);
    });
  });

  describe('selectLang', () => {
    it('should update lang and patch formSelector text', () => {
      component.model['fr'].value = 'Bonjour';
      component.selectLang('fr');

      expect(component.lang()).toBe('fr');
      expect(component.formSelector.controls['text'].value).toBe('Bonjour');
    });
  });

  describe('updateModel', () => {
    it('should update model and form control for current lang', () => {
      component.updateModel('Hello');

      expect(component.model['en'].value).toBe('Hello');
      expect(component.form.controls['en'].value).toBe('Hello');
    });
  });

  describe('removeLangValue', () => {
    it('should clear model and form control for given lang', () => {
      component.model['en'] = { display: true, value: 'Hello' };
      component.form.controls['en'].patchValue('Hello');

      component.removeLangValue('en');

      expect(component.model['en'].display).toBe(false);
      expect(component.model['en'].value).toBe('');
      expect(component.form.controls['en'].value).toBe('');
    });
  });

  describe('disableForm / enableForm', () => {
    it('should disable the form and selector text', () => {
      component.disableForm();

      expect(component.form.disabled).toBe(true);
      expect(component.formSelector.controls['text'].disabled).toBe(true);
    });

    it('should enable the form and selector text', () => {
      component.disableForm();
      component.enableForm();

      expect(component.form.enabled).toBe(true);
      expect(component.formSelector.controls['text'].enabled).toBe(true);
    });
  });
});
