import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { availableEncodings } from 'app/group/library/encodings/encodings';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { EncodingInputComponent } from './encoding-input.component';

describe('EncodingInputComponent', () => {
  let component: EncodingInputComponent;
  let fixture: ComponentFixture<EncodingInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EncodingInputComponent],
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

    fixture = TestBed.createComponent(EncodingInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty encoding', () => {
    expect(component.form.value).toEqual({ encoding: '' });
  });

  it('should return available encodings', () => {
    expect(component.getAvailableEncodings()).toBe(availableEncodings);
  });

  it('should patch form value on writeValue', () => {
    component.writeValue('UTF-8');
    expect(component.form.controls['encoding'].value).toBe('UTF-8');
  });

  it('should not patch form when writeValue receives falsy value', () => {
    component.writeValue('UTF-8');
    component.writeValue(null);
    expect(component.form.controls['encoding'].value).toBe('UTF-8');
  });

  it('should register onChange function', () => {
    const fn = vi.fn();
    component.registerOnChange(fn);
    expect(component.onChange).toBe(fn);
  });

  it('should register onTouched function', () => {
    const fn = vi.fn();
    component.registerOnTouched(fn);
    expect(component.onTouched).toBe(fn);
  });

  it('should call onChange when form value changes', () => {
    const fn = vi.fn();
    component.registerOnChange(fn);
    component.form.controls['encoding'].setValue('UTF-8');
    expect(fn).toHaveBeenCalledWith({ encoding: 'UTF-8' });
  });

  it('should disable encoding control when disabled input is true', () => {
    fixture = TestBed.createComponent(EncodingInputComponent);
    fixture.componentRef.setInput('disabled', true);
    fixture.detectChanges();
    expect(fixture.componentInstance.form.controls['encoding'].disabled).toBe(
      true
    );
  });
});
