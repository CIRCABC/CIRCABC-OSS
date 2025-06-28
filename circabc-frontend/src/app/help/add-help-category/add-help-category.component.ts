import { Component, Input, OnInit, output, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpCategory, HelpService } from 'app/core/generated/circabc';
import { nonEmptyTitle } from 'app/core/validation.service';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-help-category',
  templateUrl: './add-help-category.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    TranslocoModule,
  ],
})
export class AddHelpCategoryComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly categoryId = input<string>();
  readonly showModalChange = output<boolean>();
  readonly categoryCreated = output<ActionEmitterResult>();
  readonly categoryUpdated = output<ActionEmitterResult>();

  public creating = false;
  public newCategoryForm!: FormGroup;
  public editMode = false;
  public categoryToEdit!: HelpCategory;
  public isValid = false;

  constructor(
    private fb: FormBuilder,
    private helpService: HelpService
  ) {}

  async ngOnInit() {
    this.newCategoryForm = this.fb.group(
      {
        title: ['', nonEmptyTitle],
      },
      {
        updateOn: 'change',
      }
    );

    this.newCategoryForm.controls.title.valueChanges.subscribe((_value) => {
      this.computeValidity();
    });

    const categoryId = this.categoryId();
    if (categoryId) {
      this.editMode = true;
      this.categoryToEdit = await firstValueFrom(
        this.helpService.getHelpCategory(categoryId)
      );

      this.newCategoryForm.controls.title.patchValue(this.categoryToEdit.title);
    }
  }

  public async createCategory() {
    this.creating = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.ADD_HELP_SECTION;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpCategory = {
        title: this.newCategoryForm.value.title,
      };

      await firstValueFrom(this.helpService.createHelpCategory(body));
      result.result = ActionResult.SUCCEED;
      this.showModal = false;
      this.newCategoryForm.reset();

      this.showModalChange.emit(this.showModal);
    } catch (error) {
      console.error(error);
    }
    this.creating = false;
    this.categoryCreated.emit(result);
  }

  public async updateCategory() {
    this.creating = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.UPDATE_HELP_SECTION;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpCategory = {
        title: this.newCategoryForm.value.title,
      };

      const categoryId = this.categoryId();
      if (categoryId) {
        await firstValueFrom(
          this.helpService.updateHelpCategory(categoryId, body)
        );
        result.result = ActionResult.SUCCEED;
        this.showModal = false;
        this.newCategoryForm.reset();

        this.showModalChange.emit(this.showModal);
      }
    } catch (error) {
      console.error(error);
    }
    this.creating = false;
    this.categoryUpdated.emit(result);
  }

  public cancel() {
    this.showModal = false;
    if (!this.editMode) {
      this.newCategoryForm.reset();
    }

    this.showModalChange.emit(this.showModal);
  }

  private computeValidity() {
    if (this.newCategoryForm) {
      this.isValid = this.newCategoryForm.valid;
    } else {
      this.isValid = false;
    }
  }
}
