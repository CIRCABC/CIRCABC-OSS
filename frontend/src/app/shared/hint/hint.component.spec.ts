import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { HintComponent } from './hint.component';

describe('HintComponent', () => {
  let component: HintComponent;
  let componentRef: ComponentRef<HintComponent>;
  let fixture: ComponentFixture<HintComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HintComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(HintComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('text', 'Test hint');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should have shown set to false initially', () => {
    expect(component.shown).toBe(false);
  });

  it('should not emit clickOutside when clicking inside the component', () => {
    const emitSpy = vi.spyOn(component.clickOutside, 'emit');
    const insideElement = fixture.nativeElement;

    component.onClick(new MouseEvent('click'), insideElement);

    expect(emitSpy).not.toHaveBeenCalled();
    expect(component.shown).toBe(false);
  });

  it('should emit clickOutside and set shown to false when clicking outside', () => {
    const emitSpy = vi.spyOn(component.clickOutside, 'emit');
    component.shown = true;
    const outsideElement = document.createElement('div');
    document.body.appendChild(outsideElement);
    const event = new MouseEvent('click');

    component.onClick(event, outsideElement);

    expect(emitSpy).toHaveBeenCalledWith(event);
    expect(component.shown).toBe(false);
    document.body.removeChild(outsideElement);
  });

  it('should do nothing when targetElement is null', () => {
    const emitSpy = vi.spyOn(component.clickOutside, 'emit');
    component.shown = true;

    component.onClick(new MouseEvent('click'), null);

    expect(emitSpy).not.toHaveBeenCalled();
    expect(component.shown).toBe(true);
  });
});
