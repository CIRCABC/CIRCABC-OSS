import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  ContentService,
  InformationService,
  Node as ModelNode,
  News,
  NodesService,
} from 'app/core/generated/circabc';
import { UploadService } from 'app/core/upload.service';
import { nonEmptyTitle, urlValidator } from 'app/core/validation.service';
import { NewsCardComponent } from 'app/group/information/news-card/news-card.component';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { NotificationMessageComponent } from 'app/shared/notification-message/notification-message.component';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone component that renders the "add / edit news" form for an
 * interest group's information service.
 *
 * The template exposes a reactive form (`newsForm`) that lets a user compose a
 * news item by choosing a display pattern (e.g. `text`, `image`, `document`,
 * `date` or `iframe`), a layout, a size, a title, rich-text content, an
 * optional publication date, an optional URL and optional file uploads. A live
 * {@link NewsCardComponent} preview is shown alongside the form so the author
 * can see how the news card will look.
 *
 * The same component handles both creation and edition: when a `newsId` route
 * parameter is present the existing news item is loaded and the form is
 * pre-populated (see {@link AddNewsComponent.inEditMode}).
 *
 * Key collaborators:
 * - {@link InformationService} to read, create and update news items.
 * - {@link NodesService} to resolve the parent information node.
 * - {@link ContentService} to delete previously attached content on update.
 * - {@link UploadService} to upload attached files.
 * - {@link Router} / {@link ActivatedRoute} for navigation and route params.
 */
@Component({
  selector: 'cbc-add-news',
  templateUrl: './add-news.component.html',
  styleUrl: './add-news.component.scss',
  preserveWhitespaces: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    NotificationMessageComponent,
    MultilingualInputComponent,
    ControlMessageComponent,
    RichTextEditorComponent,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    SpinnerComponent,
    NewsCardComponent,
    TranslocoModule,
  ],
})
export class AddNewsComponent implements OnInit {
  /** Builder used to create the reactive {@link newsForm}. */
  private readonly fb = inject(FormBuilder);
  /** Current activated route, used to read params and navigate relatively. */
  private readonly route = inject(ActivatedRoute);
  /** API client for reading, creating and updating news items. */
  private readonly informationService = inject(InformationService);
  /** Router used to navigate back to the parent view after save/cancel. */
  private readonly router = inject(Router);
  /** Service used to upload attached files for image/document news. */
  private readonly uploadService = inject(UploadService);
  /** Service used to delete previously attached content when updating. */
  private readonly contentService = inject(ContentService);
  /** API client used to resolve the parent information node. */
  private readonly nodeService = inject(NodesService);

  /** Reactive form backing the news editor; built in {@link loadPageData}. */
  public newsForm!: FormGroup;
  /** Identifier of the interest group the news belongs to (route param). */
  public groupId!: string;
  /** Files staged for upload (image/document patterns use the first entry). */
  public filesToUpload: File[] = [];
  /** Whether a save/update request is currently in flight (disables the UI). */
  public readonly executing = signal(false);
  /** The existing news item being edited; undefined in creation mode. */
  public readonly editableNews = signal<News | undefined>(undefined);
  /** Identifier of the news item being edited (route param), if any. */
  public newsId!: string;
  /** Default publication date pre-filled in the date picker (today). */
  public defaultDate: Date = new Date();
  /** Identifier of the parent information node (route param), if any. */
  public infoId: string | undefined;
  /** Resolved parent information node, loaded when {@link infoId} is set. */
  public readonly infoNode = signal<ModelNode | undefined>(undefined);

  /**
   * Angular lifecycle hook. Subscribes to route parameter changes and
   * (re)loads the page data whenever the params change.
   */
  ngOnInit() {
    this.route.params.subscribe(
      async (params) => await this.loadPageData(params)
    );
  }

  /**
   * Reads route parameters, builds the {@link newsForm}, resolves the parent
   * information node when `infoId` is present, loads the editable news item
   * (edit mode) and wires up pattern-driven validation.
   *
   * @param params - Route parameters (`id`, `newsId`, `infoId`).
   * @returns A promise that resolves once the page data has been loaded.
   */
  private async loadPageData(params: Params) {
    this.groupId = params.id;
    this.newsId = params.newsId;
    this.infoId = params.infoId;
    this.newsForm = this.fb.group(
      {
        title: ['', [nonEmptyTitle]],
        size: [1],
        layout: ['normal'],
        pattern: ['text'],
        content: ['', Validators.required],
        date: [this.defaultDate],
        file: [],
        files: [],
        url: [],
      },
      {
        updateOn: 'change',
      }
    );
    if (this.infoId) {
      this.infoNode.set(
        await this.nodeService.getNodeAsync({ id: this.infoId })
      );
    }
    await this.loadEditableNews(this.newsId);

    this.subscribeToPatternChanges();
  }

  /**
   * Loads the news item to edit and patches its values into {@link newsForm}.
   * Does nothing when `id` is undefined (creation mode). The title is not
   * patched for `iframe` news since that pattern does not use a title.
   *
   * @param id - Identifier of the news item to load, or undefined to skip.
   * @returns A promise that resolves once the form has been populated.
   */
  async loadEditableNews(id: string) {
    if (id !== undefined) {
      const editableNews = await this.informationService.getNewsAsync({
        id: this.newsId,
      });
      this.editableNews.set(editableNews);

      if (editableNews.pattern !== 'iframe') {
        this.newsForm.controls.title.patchValue(editableNews.title);
      }

      this.newsForm.controls.size.patchValue(editableNews.size);
      this.newsForm.controls.layout.patchValue(editableNews.layout);
      this.newsForm.controls.pattern.patchValue(editableNews.pattern);
      this.newsForm.controls.content.patchValue(editableNews.content);
      if (editableNews.date) {
        this.newsForm.controls.date.patchValue(new Date(editableNews.date));
      }
      this.newsForm.controls.url.patchValue(editableNews.url);
      this.newsForm.controls.files.setValue(editableNews.files);
    }
  }

  /**
   * Subscribes to changes of the `pattern` control and adjusts the validators
   * of the `title`, `content` and `url` controls accordingly. For the `iframe`
   * pattern the URL becomes required (and must be a valid URL) while the title
   * and content validators are cleared; every other pattern restores the
   * title/content validators and clears the URL validators.
   */
  subscribeToPatternChanges() {
    const patternChanges = this.newsForm.controls.pattern.valueChanges;

    // subscribe to changes in order to update the validator when a change happens
    patternChanges.subscribe((pattern) => {
      if (pattern === 'iframe') {
        this.newsForm.controls.title.setValidators(null);
        this.newsForm.controls.content.setValidators(null);
        this.newsForm.controls.url.setValidators([
          Validators.required,
          urlValidator,
        ]);
        this.newsForm.controls.title.updateValueAndValidity();
        this.newsForm.controls.content.updateValueAndValidity();
        this.newsForm.controls.url.updateValueAndValidity();
      } else {
        this.newsForm.controls.title.setValidators([nonEmptyTitle]);
        this.newsForm.controls.content.setValidators([Validators.required]);
        this.newsForm.controls.url.setValidators(null);
        this.newsForm.controls.title.updateValueAndValidity();
        this.newsForm.controls.content.updateValueAndValidity();
        this.newsForm.controls.url.updateValueAndValidity();
      }
    });
  }

  /**
   * Indicates whether the component is editing an existing news item.
   *
   * @returns `true` when an editable news item has been loaded, otherwise
   * `false` (creation mode).
   */
  inEditMode(): boolean {
    return this.editableNews() !== undefined;
  }

  /**
   * Builds the composite card type string used by the preview
   * {@link NewsCardComponent}.
   *
   * @returns A string in the form `"<pattern>-<layout>-<size>"`.
   */
  getTypeOfCard() {
    return `${this.newsForm.value.pattern}-${this.newsForm.value.layout}-${this.newsForm.value.size}`;
  }

  /**
   * Creates a new news item from the current form value. Normalizes the date
   * to an ISO-like string, posts the news via {@link InformationService} and,
   * for `image`/`document` patterns with a staged file, uploads the attachment.
   * On success navigates back to the parent view. Toggles {@link executing}
   * around the request.
   *
   * @returns A promise that resolves once the create flow has completed.
   */
  async saveNews() {
    this.executing.set(true);
    const news: News = { ...this.newsForm.value };
    news.size = +this.newsForm.value.size;

    const result: ActionEmitterResult = {};
    result.type = ActionType.CREATE_INFORMATION_NEWS;

    try {
      if (news.date) {
        const date = new Date(news.date);
        news.date = `${date.getFullYear()}-${
          date.getMonth() + 1
        }-${date.getDate()}T00:00:00.000Z`;
      }

      const createdNews =
        await this.informationService.postInformationNewsAsync({
          id: this.groupId,
          news,
        });

      if (
        (createdNews.pattern === 'image' ||
          createdNews.pattern === 'document') &&
        createdNews.id
      ) {
        if (this.filesToUpload.length === 1) {
          await this.uploadService.uploadNewFile(
            this.filesToUpload[0],
            createdNews.id
          );
        }
      }

      result.result = ActionResult.SUCCEED;
    } catch (error) {
      console.error(error);
      result.result = ActionResult.FAILED;
    }

    if (result.result === ActionResult.SUCCEED) {
      this.router.navigate(['../..'], { relativeTo: this.route });
    }

    this.executing.set(false);
  }

  /**
   * Handles the file input `change` event by extracting the selected files and
   * staging them for upload.
   *
   * @param event - The DOM change event emitted by the file input element.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.handleFiles(filesList);
  }

  /**
   * Replaces {@link filesToUpload} with the entries of the provided file list.
   *
   * @param filesList - The list of files selected through the file input.
   */
  private handleFiles(filesList: FileList) {
    this.filesToUpload = [];
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.filesToUpload.push(fileItem);
      }
    }
  }

  /**
   * Returns the name of the first staged file.
   *
   * @returns The file name, or an empty string when no file is staged.
   */
  public getFileName() {
    if (this.filesToUpload && this.filesToUpload.length > 0) {
      return this.filesToUpload[0].name;
    }
    return '';
  }

  /**
   * Returns the first staged file.
   *
   * @returns The staged {@link File}, or `undefined` when none is staged.
   */
  public getFile() {
    if (this.filesToUpload && this.filesToUpload.length > 0) {
      return this.filesToUpload[0];
    }
    return undefined;
  }

  /**
   * @returns `true` when the currently selected pattern is `date`.
   */
  isDateSelected(): boolean {
    return this.newsForm.value.pattern === 'date';
  }

  /**
   * @returns `true` when the currently selected pattern is `image` or
   * `document` (i.e. patterns that accept a file attachment).
   */
  isFileOrImageSelected(): boolean {
    return (
      this.newsForm.value.pattern === 'image' ||
      this.newsForm.value.pattern === 'document'
    );
  }

  /**
   * @returns `true` when the currently selected pattern is `iframe`.
   */
  isIFrameSelected(): boolean {
    return this.newsForm.value.pattern === 'iframe';
  }

  /**
   * Updates the existing news item from the current form value. Normalizes the
   * date, updates the news via {@link InformationService} and, for
   * `image`/`document` patterns with a newly staged file, deletes the previous
   * attachment (if any) via {@link ContentService} before uploading the new
   * one. On success navigates back to the parent view. Toggles
   * {@link executing} around the request.
   *
   * @returns A promise that resolves once the update flow has completed.
   */
  async updateNews() {
    this.executing.set(true);
    const news: News = { ...this.newsForm.value };
    news.size = +this.newsForm.value.size;

    const result: ActionEmitterResult = {};
    result.type = ActionType.UPDATE_INFORMATION_NEWS;

    try {
      if (news.date) {
        const date = new Date(news.date);
        news.date = `${date.getFullYear()}-${
          date.getMonth() + 1
        }-${date.getDate()}T00:00:00.000Z`;
      }

      const createdNews = await this.informationService.putNewsAsync({
        id: this.newsId,
        news,
      });

      if (
        (createdNews.pattern === 'image' ||
          createdNews.pattern === 'document') &&
        createdNews.id
      ) {
        if (this.filesToUpload.length === 1) {
          const editableNews = this.editableNews();
          if (editableNews?.files && editableNews.files.length > 0) {
            const fileId = editableNews.files[0].id;
            if (fileId) {
              await this.contentService.deleteContentAsync({ id: fileId });
            }
          }

          await this.uploadService.uploadNewFile(
            this.filesToUpload[0],
            createdNews.id
          );
        }
      }

      result.result = ActionResult.SUCCEED;
    } catch (error) {
      console.error(error);
      result.result = ActionResult.FAILED;
    }

    if (result.result === ActionResult.SUCCEED) {
      this.router.navigate(['../..'], { relativeTo: this.route });
    }
    this.executing.set(false);
  }

  /**
   * Cancels the current create/edit operation and navigates back to the
   * parent view without persisting any changes.
   *
   * @returns A promise that resolves once navigation has been triggered.
   */
  async cancel() {
    this.router.navigate(['../..'], { relativeTo: this.route });
  }

  /**
   * Determines whether the form can be submitted. In creation mode for
   * `document`/`image` patterns a staged file is additionally required; in edit
   * mode the `iframe` pattern is always considered valid; otherwise the
   * underlying reactive form validity is used.
   *
   * @returns `true` when the form is in a submittable state.
   */
  public isFormValid(): boolean {
    if (
      !this.inEditMode() &&
      (this.newsForm.value.pattern === 'document' ||
        this.newsForm.value.pattern === 'image')
    ) {
      return this.newsForm.valid && this.filesToUpload.length > 0;
    }
    if (this.inEditMode() && this.newsForm.value.pattern === 'iframe') {
      return true;
    }
    return this.newsForm.valid;
  }

  /**
   * Convenience accessor for the `title` form control.
   *
   * @returns The `title` {@link AbstractControl}.
   */
  get titleControl(): AbstractControl {
    return this.newsForm.controls.title;
  }

  /**
   * Convenience accessor for the `content` form control.
   *
   * @returns The `content` {@link AbstractControl}.
   */
  get contentControl(): AbstractControl {
    return this.newsForm.controls.content;
  }
}
