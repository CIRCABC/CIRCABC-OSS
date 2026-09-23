import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco } from '@jsverse/transloco';
import { NameValue } from 'app/core/generated/circabc';
import { vi } from 'vitest';
import { ItemMultiselectorComponent } from './item-multiselector.component';

describe('ItemMultiselectorComponent', () => {
  let component: ItemMultiselectorComponent;
  let fixture: ComponentFixture<ItemMultiselectorComponent>;

  const availableItems: NameValue[] = [
    { name: 'Item A', value: 'a' },
    { name: 'Item B', value: 'b' },
    { name: 'Item C', value: 'c' },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItemMultiselectorComponent],
      providers: [
        provideTransloco({
          config: { availableLangs: ['en'], defaultLang: 'en' },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ItemMultiselectorComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('availableItems', availableItems);
    fixture.componentRef.setInput('selectedItems', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should build the form on init', () => {
    expect(component.multiSelectForm).toBeDefined();
    expect(
      component.multiSelectForm.controls['availableSelectedItems']
    ).toBeDefined();
    expect(
      component.multiSelectForm.controls['selectedSelectedItems']
    ).toBeDefined();
  });

  describe('selectItems', () => {
    it('should add selected available items to selectedItems', () => {
      const emitSpy = vi.fn();
      component.selectedItemsChange.subscribe(emitSpy);

      component.multiSelectForm.controls['availableSelectedItems'].setValue([
        'a',
        'b',
      ]);
      component.selectItems();

      expect(emitSpy).toHaveBeenCalledWith([
        { name: 'Item A', value: 'a' },
        { name: 'Item B', value: 'b' },
      ]);
    });

    it('should not add duplicate items', () => {
      fixture.componentRef.setInput('selectedItems', [
        { name: 'Item A', value: 'a' },
      ]);
      fixture.detectChanges();

      const emitSpy = vi.fn();
      component.selectedItemsChange.subscribe(emitSpy);

      component.multiSelectForm.controls['availableSelectedItems'].setValue([
        'a',
        'b',
      ]);
      component.selectItems();

      expect(emitSpy).toHaveBeenCalledWith([
        { name: 'Item A', value: 'a' },
        { name: 'Item B', value: 'b' },
      ]);
    });

    it('should emit itemsChanged', () => {
      const changedSpy = vi.fn();
      component.itemsChanged.subscribe(changedSpy);

      component.multiSelectForm.controls['availableSelectedItems'].setValue([
        'a',
      ]);
      component.selectItems();

      expect(changedSpy).toHaveBeenCalled();
    });
  });

  describe('unselectItems', () => {
    it('should remove selected items from the list', () => {
      fixture.componentRef.setInput('selectedItems', [
        { name: 'Item A', value: 'a' },
        { name: 'Item B', value: 'b' },
      ]);
      fixture.detectChanges();

      const emitSpy = vi.fn();
      component.selectedItemsChange.subscribe(emitSpy);

      component.multiSelectForm.controls['selectedSelectedItems'].setValue([
        'a',
      ]);
      component.unselectItems();

      expect(emitSpy).toHaveBeenCalledWith([{ name: 'Item B', value: 'b' }]);
    });

    it('should emit itemsChanged on unselect', () => {
      fixture.componentRef.setInput('selectedItems', [
        { name: 'Item A', value: 'a' },
      ]);
      fixture.detectChanges();

      const changedSpy = vi.fn();
      component.itemsChanged.subscribe(changedSpy);

      component.multiSelectForm.controls['selectedSelectedItems'].setValue([
        'a',
      ]);
      component.unselectItems();

      expect(changedSpy).toHaveBeenCalled();
    });
  });
});
