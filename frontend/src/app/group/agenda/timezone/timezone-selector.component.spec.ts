import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco } from '@jsverse/transloco';
import { vi } from 'vitest';
import {
  availableTimezones,
  defaultTimezone,
} from '../timezones/supported-timezones';
import { TimezoneSelectorComponent } from './timezone-selector.component';

describe('TimezoneSelectorComponent', () => {
  let component: TimezoneSelectorComponent;
  let fixture: ComponentFixture<TimezoneSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimezoneSelectorComponent],
      providers: [
        provideTransloco({
          config: { availableLangs: ['en'], defaultLang: 'en' },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TimezoneSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should populate availableTimezones on init', () => {
    expect(component.availableTimezones).toBe(availableTimezones);
  });

  describe('ControlValueAccessor', () => {
    it('should set selectedTimezone when writeValue is called with a value', () => {
      component.writeValue('GMT+1');
      expect(component.selectedTimezone()).toBe('GMT+1');
    });

    it('should set defaultTimezone when writeValue is called with null', () => {
      component.writeValue(null);
      expect(component.selectedTimezone()).toBe(defaultTimezone.value);
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
  });

  describe('onTimezoneChange', () => {
    it('should call onChange and emit changedTimezone', () => {
      const onChangeSpy = vi.fn();
      component.registerOnChange(onChangeSpy);
      const emitSpy = vi.spyOn(component.changedTimezone, 'emit');

      component.onTimezoneChange('GMT+2');

      expect(onChangeSpy).toHaveBeenCalledWith('GMT+2');
      expect(emitSpy).toHaveBeenCalledWith('GMT+2');
    });
  });
});
