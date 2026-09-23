import { Component, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { SnackbarComponent } from './snackbar.component';

@Component({
  template: `<cbc-snackbar
    [message]="message"
    [duration]="duration"
    (snackFinished)="onFinished()"
  />`,
  imports: [SnackbarComponent],
})
class TestHostComponent {
  message = 'Test message';
  duration = 3000;
  onFinished = vi.fn();
  snackbar = viewChild.required(SnackbarComponent);
}

describe('SnackbarComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      imports: [TestHostComponent],
    });
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should set show to true on init', () => {
    expect(host.snackbar().show()).toBe(true);
  });

  it('should set show to false after duration', () => {
    vi.advanceTimersByTime(3000);
    expect(host.snackbar().show()).toBe(false);
  });

  it('should emit snackFinished after duration', () => {
    vi.advanceTimersByTime(3000);
    expect(host.onFinished).toHaveBeenCalled();
  });

  it('should respect custom duration', () => {
    vi.advanceTimersByTime(3000); // flush initial timer
    host.duration = 1000;
    host.message = 'New message';
    fixture.detectChanges();

    vi.advanceTimersByTime(999);
    expect(host.snackbar().show()).toBe(true);

    vi.advanceTimersByTime(1);
    expect(host.snackbar().show()).toBe(false);
  });

  it('should re-trigger on message change', () => {
    vi.advanceTimersByTime(3000);
    expect(host.snackbar().show()).toBe(false);

    host.message = 'Another message';
    fixture.detectChanges();

    expect(host.snackbar().show()).toBe(true);
  });
});
