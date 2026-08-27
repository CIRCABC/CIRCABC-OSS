import { Component, Input, OnInit, output, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpService, HelpSubcategory } from 'app/core/generated/circabc';
import { nonEmptyTitle } from 'app/core/validation.service';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-help-subcategory',
  templateUrl: './add-help-subcategory.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    TranslocoModule,
  ],
})
export class AddHelpSubcategoryComponent implements OnInit {
  @Input()
  showModal = false;
  readonly categoryId = input<string>();
  readonly subcategoryId = input<string>();
  readonly showModalChange = output<boolean>();
  readonly subcategoryCreated = output<ActionEmitterResult>();
  readonly subcategoryUpdated = output<ActionEmitterResult>();

  public creating = false;
  public newSubcategoryForm!: FormGroup;
  public editMode = false;
  public subcategoryToEdit!: HelpSubcategory;
  public isValid = false;

  constructor(
    private fb: FormBuilder,
    private helpService: HelpService
  ) {}

  async ngOnInit() {
    this.newSubcategoryForm = this.fb.group(
      {
        title: ['', nonEmptyTitle],
      },
      {
        updateOn: 'change',
      }
    );

    this.newSubcategoryForm.controls.title.valueChanges.subscribe((_value) => {
      this.computeValidity();
    });

    const subcategoryId = this.subcategoryId();
    if (subcategoryId) {
      this.editMode = true;
      this.subcategoryToEdit = await firstValueFrom(
        this.helpService.getHelpSubcategory(subcategoryId)
      );

      this.newSubcategoryForm.controls.title.patchValue(
        this.subcategoryToEdit.title
      );
    }
  }

  public async createSubcategory() {
    const categoryId = this.categoryId();
    if (!categoryId) {
      return;
    }

    this.creating = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.ADD_HELP_SUBCATEGORY;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpSubcategory = {
        title: this.newSubcategoryForm.value.title,
      };

      await firstValueFrom(
        this.helpService.createCategorySubcategory(categoryId, body)
      );
      result.result = ActionResult.SUCCEED;
      this.showModal = false;
      this.newSubcategoryForm.reset();

      this.showModalChange.emit(this.showModal);
    } catch (error) {
      console.error(error);
    }
    this.creating = false;
    this.subcategoryCreated.emit(result);
  }

  public async updateSubcategory() {
    this.creating = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.UPDATE_HELP_SUBCATEGORY;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpSubcategory = {
        title: this.newSubcategoryForm.value.title,
      };

      const subcategoryId = this.subcategoryId();
      if (subcategoryId) {
        await firstValueFrom(
          this.helpService.putHelpSubcategory(subcategoryId, body)
        );
        result.result = ActionResult.SUCCEED;
        this.showModal = false;
        this.newSubcategoryForm.reset();

        this.showModalChange.emit(this.showModal);
      }
    } catch (error) {
      console.error(error);
    }
    this.creating = false;
    this.subcategoryUpdated.emit(result);
  }

  public cancel() {
    this.showModal = false;
    if (!this.editMode) {
      this.newSubcategoryForm.reset();
    }

    this.showModalChange.emit(this.showModal);
  }

  private computeValidity() {
    if (this.newSubcategoryForm) {
      this.isValid = this.newSubcategoryForm.valid;
    } else {
      this.isValid = false;
    }
  }
}
