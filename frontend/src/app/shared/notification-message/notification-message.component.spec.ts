import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NotificationMessageComponent } from './notification-message.component';

@Component({
  template: `<cbc-notification-message
    [message]="message()"
    [severity]="severity()"
    [guestAllowed]="guestAllowed()"
  />`,
  imports: [NotificationMessageComponent],
})
class TestHostComponent {
  message = signal('Test message');
  severity = signal(0);
  guestAllowed = signal<boolean | undefined>(true);
}

describe('NotificationMessageComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    const component = fixture.debugElement.query(
      By.directive(NotificationMessageComponent)
    );
    expect(component).toBeTruthy();
  });

  it('should display message when guestAllowed is true', () => {
    const section = fixture.debugElement.query(By.css('.box--message'));
    expect(section).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Test message');
  });

  it('should hide message when guestAllowed is false', () => {
    host.guestAllowed.set(false);
    fixture.detectChanges();
    const section = fixture.debugElement.query(By.css('.box--message'));
    expect(section).toBeNull();
  });

  it('should hide message when guestAllowed is undefined', () => {
    host.guestAllowed.set(undefined);
    fixture.detectChanges();
    const section = fixture.debugElement.query(By.css('.box--message'));
    expect(section).toBeNull();
  });

  it.each([
    [0, '.box--message--information'],
    [1, '.box--message--warning'],
    [2, '.box--message--error'],
    [3, '.box--message--success'],
  ])('should apply correct class for severity %i', (severity, cssClass) => {
    host.severity.set(severity);
    fixture.detectChanges();
    const section = fixture.debugElement.query(By.css(cssClass));
    expect(section).toBeTruthy();
  });

  describe('isGuestAllowed', () => {
    it('should return true when guestAllowed is true', () => {
      const component = fixture.debugElement.query(
        By.directive(NotificationMessageComponent)
      ).componentInstance as NotificationMessageComponent;
      expect(component.isGuestAllowed()).toBe(true);
    });

    it('should return false when guestAllowed is false', () => {
      host.guestAllowed.set(false);
      fixture.detectChanges();
      const component = fixture.debugElement.query(
        By.directive(NotificationMessageComponent)
      ).componentInstance as NotificationMessageComponent;
      expect(component.isGuestAllowed()).toBe(false);
    });

    it('should return false when guestAllowed is undefined', () => {
      host.guestAllowed.set(undefined);
      fixture.detectChanges();
      const component = fixture.debugElement.query(
        By.directive(NotificationMessageComponent)
      ).componentInstance as NotificationMessageComponent;
      expect(component.isGuestAllowed()).toBe(false);
    });
  });
});
