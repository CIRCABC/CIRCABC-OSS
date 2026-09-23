import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { CancelCheckoutComponent } from './cancel-checkout.component';

describe('CancelCheckoutComponent', () => {
  function createComponent() {
    TestBed.configureTestingModule({
      imports: [CancelCheckoutComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(CancelCheckoutComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(CancelCheckoutComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should emit modalHide when closePopupWindow is called', () => {
    const fixture = createComponent();
    const spy = vi.fn();
    fixture.componentInstance.modalHide.subscribe(spy);

    fixture.componentInstance.closePopupWindow();

    expect(spy).toHaveBeenCalled();
  });

  it('should emit checkoutCanceled and modalHide when cancelCheckout is called', async () => {
    const fixture = createComponent();
    const canceledSpy = vi.fn();
    const hideSpy = vi.fn();
    fixture.componentInstance.checkoutCanceled.subscribe(canceledSpy);
    fixture.componentInstance.modalHide.subscribe(hideSpy);

    await fixture.componentInstance.cancelCheckout();

    expect(canceledSpy).toHaveBeenCalled();
    expect(hideSpy).toHaveBeenCalled();
  });
});
