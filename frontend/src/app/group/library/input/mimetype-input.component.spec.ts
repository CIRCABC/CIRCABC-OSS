import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { mimetypes } from 'app/group/library/mimetypes/supported-mimetypes';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { MimetypeInputComponent } from './mimetype-input.component';

describe('MimetypeInputComponent', () => {
  let component: MimetypeInputComponent;
  let fixture: ComponentFixture<MimetypeInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MimetypeInputComponent, ReactiveFormsModule],
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

    fixture = TestBed.createComponent(MimetypeInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty mimetype', () => {
    expect(component.form.value).toEqual({ mimetype: '' });
  });

  it('should return available mimetypes', () => {
    expect(component.getAvailableMimetypes()).toBe(mimetypes);
  });

  it('should patch form value on writeValue', () => {
    component.writeValue('application/json');
    expect(component.form.controls['mimetype'].value).toBe('application/json');
  });

  it('should not patch form when writeValue receives falsy', () => {
    component.writeValue(null);
    expect(component.form.controls['mimetype'].value).toBe('');
  });

  it('should register onChange callback and emit on value change', () => {
    const fn = vi.fn();
    component.registerOnChange(fn);
    component.form.controls['mimetype'].setValue('image/jp2');
    expect(fn).toHaveBeenCalledWith({ mimetype: 'image/jp2' });
  });

  it('should register onTouched callback', () => {
    const fn = vi.fn();
    component.registerOnTouched(fn);
    expect(component.onTouched).toBe(fn);
  });

  it('should disable mimetype control when disabled input is true', () => {
    fixture = TestBed.createComponent(MimetypeInputComponent);
    fixture.componentRef.setInput('disabled', true);
    fixture.detectChanges();
    expect(fixture.componentInstance.form.controls['mimetype'].disabled).toBe(
      true
    );
  });
});
