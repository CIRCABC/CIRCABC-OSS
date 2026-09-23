import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { vi } from 'vitest';
import { InlineSelectComponent } from './inline-select.component';

describe('InlineSelectComponent', () => {
  let component: InlineSelectComponent;
  let componentRef: ComponentRef<InlineSelectComponent>;
  let fixture: ComponentFixture<InlineSelectComponent>;

  const mockTranslocoService = {
    translate: vi.fn((key: string) => key),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InlineSelectComponent],
      providers: [
        { provide: TranslocoService, useValue: mockTranslocoService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InlineSelectComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should initialize options from values input', () => {
    componentRef.setInput('values', ['a', 'b']);
    fixture.detectChanges();

    expect(component.options).toEqual([
      { value: 'a', label: 'a' },
      { value: 'b', label: 'b' },
    ]);
  });

  it('should translate options when translationPrefix is set', () => {
    mockTranslocoService.translate.mockImplementation(
      (key: string) => `translated_${key}`
    );
    componentRef.setInput('values', ['x']);
    componentRef.setInput('translationPrefix', 'prefix');
    fixture.detectChanges();

    expect(mockTranslocoService.translate).toHaveBeenCalledWith('prefix.x');
    expect(component.options).toEqual([
      { value: 'x', label: 'translated_prefix.x' },
    ]);
  });

  it('should patch form value from value input', () => {
    componentRef.setInput('values', ['opt1', 'opt2']);
    componentRef.setInput('value', 'opt2');
    fixture.detectChanges();

    expect(component.form.controls['selectValue'].value).toBe('opt2');
  });

  it('should emit selectionChanged when form value changes', () => {
    componentRef.setInput('values', ['a', 'b']);
    fixture.detectChanges();

    const emitSpy = vi.spyOn(component.selectionChanged, 'emit');
    component.form.controls['selectValue'].setValue('b');

    expect(emitSpy).toHaveBeenCalledWith('b');
  });

  it('should not patch form value when value input is empty', () => {
    componentRef.setInput('values', ['a']);
    componentRef.setInput('value', '');
    fixture.detectChanges();

    expect(component.form.controls['selectValue'].value).toBe('');
  });
});
