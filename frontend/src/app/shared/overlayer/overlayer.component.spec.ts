import { TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { vi } from 'vitest';
import { OverlayerComponent } from './overlayer.component';

describe('OverlayerComponent', () => {
  let component: OverlayerComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FormBuilder],
    });

    component = TestBed.runInInjectionContext(() => new OverlayerComponent());
    component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with doNotShow as false', () => {
    expect(component.form.value.doNotShow).toBe(false);
  });

  it('should set visible to false and emit closed on close()', () => {
    component.visible.set(true);
    const closedSpy = vi.fn();
    component.closed.subscribe(closedSpy);

    component.close();

    expect(component.visible()).toBe(false);
    expect(closedSpy).toHaveBeenCalledWith(false);
  });

  it('should emit doNotShow value when close() is called', () => {
    component.form.patchValue({ doNotShow: true });
    const closedSpy = vi.fn();
    component.closed.subscribe(closedSpy);

    component.close();

    expect(closedSpy).toHaveBeenCalledWith(true);
  });
});
