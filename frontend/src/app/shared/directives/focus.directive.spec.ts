import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { FocusDirective } from './focus.directive';

@Component({
  template: `<input [cbcFocus]="true" />`,
  imports: [FocusDirective],
})
class TestHostComponent {}

describe('FocusDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      imports: [TestHostComponent],
    });
    fixture = TestBed.createComponent(TestHostComponent);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should focus the element after 500ms', () => {
    const input: HTMLInputElement =
      fixture.nativeElement.querySelector('input');
    const focusSpy = vi.spyOn(input, 'focus');

    fixture.detectChanges();

    expect(focusSpy).not.toHaveBeenCalled();

    vi.advanceTimersByTime(500);

    expect(focusSpy).toHaveBeenCalled();
  });
});
