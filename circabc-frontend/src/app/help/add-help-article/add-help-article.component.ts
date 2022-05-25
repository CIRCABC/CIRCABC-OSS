import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpArticle, HelpService } from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import {
  LanguageCodeName,
  SupportedLangs,
} from 'app/shared/langs/supported-langs';
import { assertDefined } from 'app/core/asserts';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-help-article',
  templateUrl: './add-help-article.component.html',
  styleUrls: ['./add-help-article.component.scss'],
})
export class AddHelpArticleComponent implements OnInit, OnChanges {
  @Input()
  showModal = false;
  @Input()
  categoryId: string | undefined;
  @Input()
  articleId: string | undefined;
  @Output()
  readonly showModalChange = new EventEmitter();
  @Output()
  readonly articleCreated = new EventEmitter<ActionEmitterResult>();
  @Output()
  readonly articleUpdated = new EventEmitter<ActionEmitterResult>();

  public creating = false;
  public newArticlForm!: FormGroup;
  public model!: { [key: string]: string };
  public availableLangs!: LanguageCodeName[];
  public isValid = false;
  public editMode = false;
  public articleToEdit!: HelpArticle;

  constructor(private fb: FormBuilder, private helpService: HelpService) {}

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.articleId) {
      await this.prepareForm();
      this.switchLang(this.newArticlForm.value.currentLang);
    }
  }

  async ngOnInit() {
    this.newArticlForm = this.fb.group(
      {
        title: ['', ValidationService.nonEmptyTitle],
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

    this.availableLangs = SupportedLangs.availableLangs;

    if (this.articleId) {
      await this.prepareForm();
    }
  }

  public async prepareForm() {
    assertDefined(this.articleId);
    this.editMode = true;
    this.articleToEdit = await firstValueFrom(
      this.helpService.getHelpArticle(this.articleId)
    );
    this.newArticlForm.controls.title.patchValue(this.articleToEdit.title);
    if (this.articleToEdit.content) {
      this.model = this.articleToEdit.content;
    }
  }

  public async createArticle() {
    if (this.categoryId === undefined) {
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
        this.helpService.createCategoryArticle(this.categoryId, body)
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

      if (this.articleId) {
        await firstValueFrom(
          this.helpService.updateHelpArticle(this.articleId, body)
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
