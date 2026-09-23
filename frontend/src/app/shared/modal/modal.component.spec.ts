import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { ModalComponent } from './modal.component';

@Component({
  template: `<cbc-modal
    [(visible)]="visible"
    (ok)="onOk()"
    (cancelModal)="onCancel()"
  />`,
  imports: [ModalComponent],
})
class TestHostComponent {
  visible = signal(true);
  onOk = vi.fn();
  onCancel = vi.fn();
}

describe('ModalComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;
  let modal: ModalComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();
    modal = fixture.debugElement.children[0]
      .componentInstance as ModalComponent;
  });

  it('should create', () => {
    expect(modal).toBeDefined();
  });

  it('should have default input values', () => {
    expect(modal.title()).toBe('');
    expect(modal.okLabel()).toBe('label.ok');
    expect(modal.cancelLabel()).toBe('label.cancel');
    expect(modal.executingLabel()).toBe('');
    expect(modal.showOkButton()).toBe(true);
    expect(modal.displayCloseButton()).toBe(true);
    expect(modal.contentClass()).toBe('modal-content');
    expect(modal.executing()).toBe(false);
  });

  it('should emit ok event when onOk is called', () => {
    modal.onOk();
    expect(host.onOk).toHaveBeenCalled();
  });

  it('should emit cancelModal event when onCancel is called', () => {
    modal.onCancel();
    expect(host.onCancel).toHaveBeenCalled();
  });
});
