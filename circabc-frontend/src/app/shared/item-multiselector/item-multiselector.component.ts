import { Component, Input, OnInit, output, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NameValue } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-item-multiselector',
  templateUrl: './item-multiselector.component.html',
  styleUrl: './item-multiselector.component.scss',
  preserveWhitespaces: true,
  imports: [ReactiveFormsModule],
})
export class ItemMultiselectorComponent implements OnInit {
  public readonly availableItems = input.required<NameValue[]>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public selectedItems!: NameValue[];
  public readonly selectedItemsChange = output<NameValue[]>();
  public readonly itemsChanged = output();

  public multiSelectForm!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.buildForm();
  }

  private buildForm() {
    this.multiSelectForm = this.fb.group({
      availableSelectedItems: [[]],
      selectedSelectedItems: [[]],
    });
  }

  public selectItems() {
    if (this.selectedItems === undefined) {
      this.selectedItems = [];
    }
    for (const availableSelectedItem of this.multiSelectForm.controls
      .availableSelectedItems.value) {
      for (const availableItem of this.availableItems()) {
        if (
          availableSelectedItem === availableItem.value &&
          !this.itemExists(this.selectedItems, availableItem)
        ) {
          this.selectedItems.push(availableItem);
        }
      }
    }
    this.multiSelectForm.controls.selectedSelectedItems.patchValue(
      this.selectedItems
    );
    this.selectedItemsChange.emit(this.selectedItems);
    this.itemsChanged.emit();
  }

  private itemExists(items: NameValue[], itemToCkeck: NameValue): boolean {
    return (
      items.filter((item: NameValue) => item.value === itemToCkeck.value)
        .length !== 0
    );
  }

  public unselectItems() {
    for (const selectedSelectedItem of this.multiSelectForm.controls
      .selectedSelectedItems.value) {
      let index = 0;
      for (const selectedItem of this.selectedItems) {
        if (selectedSelectedItem === selectedItem.value) {
          this.selectedItems.splice(index, 1);
        }
        index += 1;
      }
    }
    this.multiSelectForm.controls.availableSelectedItems.patchValue(
      this.availableItems()
    );
    this.selectedItemsChange.emit(this.selectedItems);
    this.itemsChanged.emit();
  }
}
