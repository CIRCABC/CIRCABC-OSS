import { TestBed } from '@angular/core/testing';
import { NgControl } from '@angular/forms';
import { vi } from 'vitest';

import { VersionMaskDirective } from './version-mask.directive';

describe('VersionMaskDirective', () => {
  let directive: VersionMaskDirective;
  let writeValueSpy: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    writeValueSpy = vi.fn();
    const mockNgControl = {
      valueAccessor: { writeValue: writeValueSpy },
    } as unknown as NgControl;

    TestBed.configureTestingModule({
      providers: [
        VersionMaskDirective,
        { provide: NgControl, useValue: mockNgControl },
      ],
    });

    directive = TestBed.inject(VersionMaskDirective);
  });

  describe('onInputChange', () => {
    it('should write empty string when input is empty', () => {
      directive.onInputChange('', false);
      expect(writeValueSpy).toHaveBeenCalledWith('');
    });

    it('should format single digit with trailing dot', () => {
      directive.onInputChange('1', false);
      expect(writeValueSpy).toHaveBeenCalledWith('1.');
    });

    it.each([
      ['format two digits as major.minor', '12', false, '1.2'],
      ['format three digits as majorminor.patch', '123', false, '12.3'],
      ['strip non-digit characters', '1a2b3', false, '12.3'],
      ['truncate to 3 digits max', '1234', false, '12.3'],
      ['remove last digit on backspace when length <= 3', '12', true, '1.'],
      ['result in empty on backspace with single digit', '1', true, ''],
    ])('should %s', (_label, input, backspace, expected) => {
      directive.onInputChange(input, backspace);
      expect(writeValueSpy).toHaveBeenCalledWith(expected);
    });

    it('should not write value if valueAccessor is null', () => {
      directive.ngControl.valueAccessor = null;
      directive.onInputChange('12', false);
      expect(writeValueSpy).not.toHaveBeenCalled();
    });
  });

  describe('onModelChange', () => {
    it('should call onInputChange with event and backspace false', () => {
      directive.onModelChange('5');
      expect(writeValueSpy).toHaveBeenCalledWith('5.');
    });
  });

  describe('keydownBackspace', () => {
    it('should call onInputChange with target value and backspace true', () => {
      directive.keydownBackspace({ target: { value: '12' } });
      expect(writeValueSpy).toHaveBeenCalledWith('1.');
    });
  });
});
