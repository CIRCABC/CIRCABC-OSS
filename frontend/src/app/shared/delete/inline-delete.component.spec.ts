import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { InlineDeleteComponent } from './inline-delete.component';

describe('InlineDeleteComponent', () => {
  let component: InlineDeleteComponent;
  let fixture: ComponentFixture<InlineDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InlineDeleteComponent],
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

    fixture = TestBed.createComponent(InlineDeleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('deleteAskConfirmation', () => {
    it('should set mustConfirm to true and emit mustConfirmChange', () => {
      const spy = vi.fn();
      component.mustConfirmChange.subscribe(spy);

      component.deleteAskConfirmation();

      expect(component.mustConfirm()).toBe(true);
      expect(spy).toHaveBeenCalledWith(true);
    });
  });

  describe('confirm', () => {
    it('should set deleting to true and emit deletionConfirmed when deleteInline is true', () => {
      const deletionSpy = vi.fn();
      const deletingSpy = vi.fn();
      component.deletionConfirmed.subscribe(deletionSpy);
      component.deletingChange.subscribe(deletingSpy);

      component.confirm();

      expect(component.deleting()).toBe(true);
      expect(deletionSpy).toHaveBeenCalled();
      expect(deletingSpy).toHaveBeenCalledWith(true);
    });

    it('should set mustConfirm to false and not set deleting when deleteInline is false', () => {
      fixture.componentRef.setInput('deleteInline', false);
      component.mustConfirm.set(true);

      const deletionSpy = vi.fn();
      const deletingSpy = vi.fn();
      component.deletionConfirmed.subscribe(deletionSpy);
      component.deletingChange.subscribe(deletingSpy);

      component.confirm();

      expect(component.mustConfirm()).toBe(false);
      expect(component.deleting()).toBe(false);
      expect(deletionSpy).toHaveBeenCalled();
      expect(deletingSpy).toHaveBeenCalledWith(false);
    });
  });

  describe('cancel', () => {
    it('should set mustConfirm to false and emit mustConfirmChange', () => {
      component.mustConfirm.set(true);
      const spy = vi.fn();
      component.mustConfirmChange.subscribe(spy);

      component.cancel();

      expect(component.mustConfirm()).toBe(false);
      expect(spy).toHaveBeenCalledWith(false);
    });
  });
});
