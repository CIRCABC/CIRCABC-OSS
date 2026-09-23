import { Component, input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FadeInOutDirective } from './fade-in-out.directive';

@Component({
  imports: [FadeInOutDirective],
  template: `<div cbcFadeInOut [show]="show()"></div>`,
})
class TestHostComponent {
  show = input(true);
}

describe('FadeInOutDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let div: HTMLElement;

  function createFixture(show: boolean): void {
    TestBed.configureTestingModule({
      imports: [TestHostComponent],
    });
    fixture = TestBed.overrideComponent(TestHostComponent, {
      set: { template: `<div cbcFadeInOut [show]="${show}"></div>` },
    }).createComponent(TestHostComponent);
    fixture.detectChanges();
    const foundDiv = (fixture.nativeElement as HTMLElement).querySelector(
      'div'
    );
    expect(foundDiv).not.toBeNull();
    div = foundDiv as HTMLElement;
  }

  it('should set transition and overflow styles on init', () => {
    createFixture(true);
    expect(div.style.transition).toBe(
      'opacity 200ms ease-in, height 200ms ease-out'
    );
    expect(div.style.overflow).toBe('hidden');
  });

  it('should show element when show is true', () => {
    createFixture(true);
    expect(div.style.opacity).toBe('1');
    expect(div.style.height).toBe('');
  });

  it('should hide element when show is false', () => {
    createFixture(false);
    expect(div.style.opacity).toBe('0');
    expect(div.style.height).toBe('0px');
  });
});
