import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { ModalDeleteComponent } from './modal-delete.component';

describe('ModalDeleteComponent', () => {
  let component: ModalDeleteComponent;
  let fixture: ComponentFixture<ModalDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalDeleteComponent],
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

    fixture = TestBed.createComponent(ModalDeleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have showModal default to false', () => {
    expect(component.showModal()).toBe(false);
  });

  describe('closePopupWindow', () => {
    it('should set showModal to false and emit deletionCanceled', () => {
      const canceledSpy = vi.fn();
      component.deletionCanceled.subscribe(canceledSpy);
      component.showModal.set(true);

      component.closePopupWindow();

      expect(component.showModal()).toBe(false);
      expect(canceledSpy).toHaveBeenCalled();
    });
  });

  describe('delete', () => {
    it('should set showModal to false and emit deletionConfirmed', async () => {
      const confirmedSpy = vi.fn();
      component.deletionConfirmed.subscribe(confirmedSpy);
      component.showModal.set(true);

      await component.delete();

      expect(component.showModal()).toBe(false);
      expect(confirmedSpy).toHaveBeenCalled();
    });
  });
});
