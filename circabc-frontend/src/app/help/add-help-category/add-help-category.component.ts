import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpCategory, HelpService } from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-help-category',
  templateUrl: './add-help-category.component.html',
  preserveWhitespaces: true,
})
export class AddHelpCategoryComponent implements OnInit {
  @Input()
  showModal = false;
  @Input()
  categoryId: string | undefined;
  @Output()
  readonly showModalChange = new EventEmitter();
  @Output()
  readonly categoryCreated = new EventEmitter<ActionEmitterResult>();
  @Output()
  readonly categoryUpdated = new EventEmitter<ActionEmitterResult>();

  public creating = false;
  public newCategoryForm!: FormGroup;
  public editMode = false;
  public categoryToEdit!: HelpCategory;
  public isValid = false;

  constructor(private fb: FormBuilder, private helpService: HelpService) {}

  async ngOnInit() {
    this.newCategoryForm = this.fb.group(
      {
        title: ['', ValidationService.nonEmptyTitle],
      },
      {
        updateOn: 'change',
      }
    );

    this.newCategoryForm.controls.title.valueChanges.subscribe((_value) => {
      this.computeValidity();
    });

    if (this.categoryId) {
      this.editMode = true;
      this.categoryToEdit = await firstValueFrom(
        this.helpService.getHelpCategory(this.categoryId)
      );

      this.newCategoryForm.controls.title.patchValue(this.categoryToEdit.title);
    }
  }

  public async createCategory() {
    this.creating = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.ADD_HELP_CATEGORY;
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
    result.type = ActionType.UPDATE_HELP_CATEGORY;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpCategory = {
        title: this.newCategoryForm.value.title,
      };

      if (this.categoryId) {
        await firstValueFrom(
          this.helpService.updateHelpCategory(this.categoryId, body)
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
