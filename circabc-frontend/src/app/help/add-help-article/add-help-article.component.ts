import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
  input,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { assertDefined } from 'app/core/asserts';
import { HelpArticle, HelpService } from 'app/core/generated/circabc';
import { nonEmptyTitle } from 'app/core/validation.service';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import {
  LanguageCodeName,
  supportedLanguages,
} from 'app/shared/langs/supported-langs';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SharedModule } from 'primeng/api';
import { EditorModule } from 'primeng/editor';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-help-article',
  templateUrl: './add-help-article.component.html',
  styleUrl: './add-help-article.component.scss',
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    EditorModule,
    SharedModule,
    TranslocoModule,
  ],
})
export class AddHelpArticleComponent implements OnInit, OnChanges {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly categoryId = input<string>();
  readonly articleId = input<string>();
  readonly showModalChange = output<boolean>();
  readonly articleCreated = output<ActionEmitterResult>();
  readonly articleUpdated = output<ActionEmitterResult>();

  public creating = false;
  public newArticlForm!: FormGroup;
  public model!: { [key: string]: string };
  public availableLangs!: LanguageCodeName[];
  public isValid = false;
  public editMode = false;
  public articleToEdit!: HelpArticle;

  constructor(
    private fb: FormBuilder,
    private helpService: HelpService
  ) {}

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.articleId) {
      await this.prepareForm();
      this.switchLang(this.newArticlForm.value.currentLang);
    }
  }

  async ngOnInit() {
    this.newArticlForm = this.fb.group(
      {
        title: ['', nonEmptyTitle],
        currentLang: '',
        content: [''],
      },
      {
        updateOn: 'change',
      }
    );

    this.newArticlForm.controls.currentLang.valueChanges.subscribe((value) => {
      this.switchLang(value);
    });

    this.newArticlForm.controls.title.valueChanges.subscribe((_value) => {
      this.computeValidity();
    });

    this.model = {};

    this.availableLangs = supportedLanguages;

    if (this.articleId()) {
      await this.prepareForm();
    }
  }

  public async prepareForm() {
    const articleId = this.articleId();
    assertDefined(articleId);
    this.editMode = true;
    this.articleToEdit = await firstValueFrom(
      this.helpService.getHelpArticle(articleId)
    );
    this.newArticlForm.controls.title.patchValue(this.articleToEdit.title);
    if (this.articleToEdit.content) {
      this.model = this.articleToEdit.content;
    }
  }

  public async createArticle() {
    const categoryId = this.categoryId();
    if (categoryId === undefined) {
      return;
    }
    this.creating = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.ADD_HELP_ARTICLE;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpArticle = {
        title: this.newArticlForm.value.title,
        content: this.model,
      };
      await firstValueFrom(
        this.helpService.createCategoryArticle(categoryId, body)
      );
      result.result = ActionResult.SUCCEED;
      this.showModal = false;
      this.model = {};
      this.newArticlForm.reset();

      this.showModalChange.emit(this.showModal);
    } catch (error) {
      console.error(error);
    }
    this.creating = false;
    this.articleCreated.emit(result);
  }

  public async updateArticle() {
    this.creating = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.UPDATE_HELP_ARTICLE;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpArticle = {
        title: this.newArticlForm.value.title,
        content: this.model,
      };

      const articleId = this.articleId();
      if (articleId) {
        await firstValueFrom(
          this.helpService.updateHelpArticle(articleId, body)
        );
        result.result = ActionResult.SUCCEED;
        this.newArticlForm.reset();
        this.showModal = false;
        await this.prepareForm();
        this.showModalChange.emit(this.showModal);
      }
    } catch (error) {
      console.error(error);
    }
    this.creating = false;
    this.articleUpdated.emit(result);
  }

  public cancel() {
    this.showModal = false;
    this.model = {};
    if (!this.editMode) {
      this.newArticlForm.reset();
      this.computeValidity();
    }

    this.showModalChange.emit(this.showModal);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public syncText(event: any) {
    this.model[this.newArticlForm.value.currentLang] = event.htmlValue;
    this.computeValidity();
  }

  public switchLang(value: string) {
    if (this.model[value]) {
      this.newArticlForm.patchValue({ content: this.model[value] });
    } else {
      this.newArticlForm.patchValue({ content: '' });
    }

    this.computeValidity();
  }

  private computeValidity() {
    if (this.newArticlForm) {
      let isValidModel = false;
      for (const key of Object.keys(this.model)) {
        if (
          this.model[key] !== '' &&
          this.model[key] !== undefined &&
          this.model[key] !== null
        ) {
          isValidModel = true;
        }
      }

      this.isValid = this.newArticlForm.valid && isValidModel;
    } else {
      this.isValid = false;
    }
  }
}
