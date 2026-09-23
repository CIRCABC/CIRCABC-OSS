import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NumberBadgeComponent } from './number-badge.component';

describe('NumberBadgeComponent', () => {
  let fixture: ComponentFixture<NumberBadgeComponent>;
  let componentRef: ComponentRef<NumberBadgeComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NumberBadgeComponent],
    });
    fixture = TestBed.createComponent(NumberBadgeComponent);
    componentRef = fixture.componentRef;
  });

  it('should create', () => {
    componentRef.setInput('number', 5);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });

  it.each([
    [5, -1, '#58c37e', 'no limit is set'],
    [3, 5, '#58c37e', 'number is within limit'],
    [5, 5, '#58c37e', 'number equals limit'],
    [6, 5, 'red', 'number exceeds limit'],
  ])(
    'should return correct background color (%s, limit=%s → %s) when %s',
    (number, limit, expected) => {
      componentRef.setInput('number', number);
      if (limit >= 0) componentRef.setInput('limit', limit);
      fixture.detectChanges();
      expect(fixture.componentInstance.backgroundColor()).toBe(expected);
    }
  );

  it('should render the number in the template', () => {
    componentRef.setInput('number', 42);
    fixture.detectChanges();
    const span = fixture.nativeElement.querySelector('.number-badge');
    expect(span.textContent.trim()).toBe('42');
  });
});
