import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormControl, Validators } from '@angular/forms';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { ControlMessageComponent } from './control-message.component';

describe('ControlMessageComponent', () => {
  let component: ControlMessageComponent;
  let componentRef: ComponentRef<ControlMessageComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ControlMessageComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(ControlMessageComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(ControlMessageComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  it('should return empty array when control has no errors', () => {
    const control = new FormControl('valid');
    componentRef.setInput('control', control);
    expect(component.getErrorsKeys()).toEqual([]);
  });

  it('should return empty array when control has errors but is pristine and untouched and showInvalid is false', () => {
    const control = new FormControl('', Validators.required);
    componentRef.setInput('control', control);
    expect(component.getErrorsKeys()).toEqual([]);
  });

  it('should return error keys when control is dirty and has errors', () => {
    const control = new FormControl('', Validators.required);
    control.markAsDirty();
    componentRef.setInput('control', control);

    const result = component.getErrorsKeys();
    expect(result).toEqual([{ key: 'validation.required', value: true }]);
  });

  it('should return error keys when control is touched and has errors', () => {
    const control = new FormControl('', Validators.required);
    control.markAsTouched();
    componentRef.setInput('control', control);

    const result = component.getErrorsKeys();
    expect(result).toEqual([{ key: 'validation.required', value: true }]);
  });

  it('should return error keys when showInvalid is true and control is invalid', () => {
    const control = new FormControl('', Validators.required);
    componentRef.setInput('control', control);
    componentRef.setInput('showInvalid', true);

    const result = component.getErrorsKeys();
    expect(result).toEqual([{ key: 'validation.required', value: true }]);
  });

  it('should return multiple error keys', () => {
    const control = new FormControl('ab', [
      Validators.minLength(5),
      Validators.maxLength(1),
    ]);
    control.markAsDirty();
    componentRef.setInput('control', control);

    const result = component.getErrorsKeys();
    expect(result).toHaveLength(2);
    expect(result[0].key).toBe('validation.minlength');
    expect(result[1].key).toBe('validation.maxlength');
  });
});
