import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { PreviewComponent } from './preview.component';

describe('PreviewComponent', () => {
  function createComponent() {
    TestBed.configureTestingModule({
      imports: [PreviewComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(PreviewComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(PreviewComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    expect(createComponent().componentInstance).toBeDefined();
  });

  it('should have default input values', () => {
    const comp = createComponent().componentInstance;
    expect(comp.title()).toBe('');
    expect(comp.okLabel()).toBe('label.ok');
    expect(comp.cancelLabel()).toBe('label.cancel');
    expect(comp.executingLabel()).toBe('');
    expect(comp.showOkButton()).toBe(true);
    expect(comp.displayCloseButton()).toBe(true);
    expect(comp.contentClass()).toBe('modal-content');
  });

  it('should default visible and executing to false', () => {
    const comp = createComponent().componentInstance;
    expect(comp.visible()).toBe(false);
    expect(comp.executing()).toBe(false);
  });

  it('should emit cancelPreview on onCancel', () => {
    const comp = createComponent().componentInstance;
    const spy = vi.fn();
    comp.cancelPreview.subscribe(spy);
    comp.onCancel();
    expect(spy).toHaveBeenCalled();
  });

  it('should emit ok on onOk', () => {
    const comp = createComponent().componentInstance;
    const spy = vi.fn();
    comp.ok.subscribe(spy);
    comp.onOk();
    expect(spy).toHaveBeenCalled();
  });
});
