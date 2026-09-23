import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { NameValue } from 'app/core/generated/circabc';

/**
 * Dual-list multiselector component.
 *
 * Renders two lists side by side: one with the still-available items and one
 * with the currently selected items, together with the controls used to move
 * entries between them. Items are represented as {@link NameValue} pairs, where
 * `value` is used as the unique identifier and `name` as the display label.
 *
 * The component is driven by two required inputs ({@link availableItems} and
 * {@link selectedItems}) and reports changes back to the parent through the
 * {@link selectedItemsChange} and {@link itemsChanged} outputs, making it
 * usable in a one-way-binding / event pattern.
 */
@Component({
  selector: 'cbc-item-multiselector',
  templateUrl: './item-multiselector.component.html',
  styleUrl: './item-multiselector.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, TranslocoModule],
})
export class ItemMultiselectorComponent implements OnInit {
  /** Angular `FormBuilder` used to construct the reactive selection form. */
  private readonly fb = inject(FormBuilder);

  /**
   * Required input listing the items that can be picked from (the left-hand
   * "available" list).
   */
  public readonly availableItems = input.required<NameValue[]>();
  /**
   * Required input listing the items that are currently selected (the
   * right-hand "selected" list).
   */
  public readonly selectedItems = input.required<NameValue[]>();
  /**
   * Emits the updated collection of selected items whenever items are moved
   * between the two lists. Intended to be used with two-way-style binding on
   * `selectedItems`.
   */
  public readonly selectedItemsChange = output<NameValue[]>();
  /**
   * Emits (without a payload) whenever the selection changes, allowing the
   * parent to react to any modification regardless of the new value.
   */
  public readonly itemsChanged = output();

  /**
   * Reactive form holding the transient list-box selections
   * (`availableSelectedItems` and `selectedSelectedItems`) used to decide which
   * entries are moved on {@link selectItems} / {@link unselectItems}.
   */
  public multiSelectForm!: FormGroup;

  /**
   * Angular lifecycle hook. Builds the reactive selection form once the
   * component is initialised.
   */
  ngOnInit(): void {
    this.buildForm();
  }

  /**
   * Initialises {@link multiSelectForm} with empty controls for the highlighted
   * items in each list.
   */
  private buildForm() {
    this.multiSelectForm = this.fb.group({
      availableSelectedItems: [[]],
      selectedSelectedItems: [[]],
    });
  }

  /**
   * Moves the items highlighted in the "available" list into the "selected"
   * list. Existing selections are preserved and duplicates (matched by
   * {@link NameValue.value}) are skipped. Emits {@link selectedItemsChange}
   * with the new selection and {@link itemsChanged}.
   */
  public selectItems() {
    const selectedItems = this.selectedItems() ?? [];
    const updatedSelectedItems = [...selectedItems];
    for (const availableSelectedItem of this.multiSelectForm.controls
      .availableSelectedItems.value) {
      for (const availableItem of this.availableItems()) {
        if (
          availableSelectedItem === availableItem.value &&
          !this.itemExists(updatedSelectedItems, availableItem)
        ) {
          updatedSelectedItems.push(availableItem);
        }
      }
    }
    this.multiSelectForm.controls.selectedSelectedItems.patchValue(
      updatedSelectedItems
    );
    this.selectedItemsChange.emit(updatedSelectedItems);
    this.itemsChanged.emit();
  }

  /**
   * Checks whether an item is already present in a list, comparing by
   * {@link NameValue.value}.
   *
   * @param items - The list to search within.
   * @param itemToCkeck - The item to look for.
   * @returns `true` if an item with the same `value` is already present,
   * otherwise `false`.
   */
  private itemExists(items: NameValue[], itemToCkeck: NameValue): boolean {
    return items.some((item: NameValue) => item.value === itemToCkeck.value);
  }

  /**
   * Removes the items highlighted in the "selected" list from the current
   * selection. Emits {@link selectedItemsChange} with the reduced selection and
   * {@link itemsChanged}.
   */
  public unselectItems() {
    const selectedItems = this.selectedItems() ?? [];
    let updatedSelectedItems = [...selectedItems];
    for (const selectedSelectedItem of this.multiSelectForm.controls
      .selectedSelectedItems.value) {
      updatedSelectedItems = updatedSelectedItems.filter(
        (selectedItem) => selectedItem.value !== selectedSelectedItem
      );
    }
    this.multiSelectForm.controls.availableSelectedItems.patchValue(
      this.availableItems()
    );
    this.selectedItemsChange.emit(updatedSelectedItems);
    this.itemsChanged.emit();
  }
}
