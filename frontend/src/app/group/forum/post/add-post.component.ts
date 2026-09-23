import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  type Comment,
  Node as ModelNode,
  PostService,
  TopicService,
  UserService,
} from 'app/core/generated/circabc';
import { type Quote } from 'app/core/ui-model/index';
import { getUserFullName } from 'app/core/util';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component (`cbc-add-post`) that renders the form used to
 * create a new post (reply) in a forum topic or to edit an existing post.
 *
 * The template contains a reactive form with a single rich-text editor field
 * for the post body plus a spinner shown while a request is in flight. The
 * component supports three related behaviors driven by its inputs:
 * - Creating a brand new reply to the current {@link topic}.
 * - Pre-filling the editor with a quoted message when a {@link futureQuote}
 *   is supplied.
 * - Editing an existing post when {@link editPost} is supplied.
 *
 * It collaborates with {@link PostService} and {@link TopicService} to persist
 * changes, with {@link UserService} (via {@link getUserFullName}) to resolve
 * the quoted author's display name, and with {@link TranslocoService} for
 * localized labels. Outcomes are reported to the parent through the
 * {@link postedComment} output.
 */
@Component({
  selector: 'cbc-add-post',
  templateUrl: './add-post.component.html',
  styleUrl: './add-post.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    RichTextEditorComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AddPostComponent implements OnInit, OnChanges {
  /** Builder used to construct the reactive {@link addPostForm}. */
  private readonly fb = inject(FormBuilder);
  /** API client for post-level operations (used when updating a post). */
  private readonly postService = inject(PostService);
  /** API client for topic-level operations (used when posting a reply). */
  private readonly topicService = inject(TopicService);
  /** Translation service used to localize labels such as the "quoting" prefix. */
  private readonly translateService = inject(TranslocoService);
  /** API client used to resolve a quoted author's full name. */
  private readonly userService = inject(UserService);

  /** Required input: the forum topic that a new reply will be posted to. */
  readonly topic = input.required<ModelNode>();
  /**
   * Required input: the quote to pre-fill the editor with. When it changes to
   * a defined value, the editor is populated with the quoted author and text.
   */
  readonly futureQuote = input.required<Quote>();

  /**
   * Optional input: an existing post to edit. When set, the editor is
   * pre-filled with the post's current message and {@link updatePost} is used
   * to persist the change.
   */
  readonly editPost = input<ModelNode>();
  /**
   * Output emitted after a create or update operation completes, carrying the
   * {@link ActionEmitterResult} (action type and success/failure result).
   */
  readonly postedComment = output<ActionEmitterResult>();

  /** Reactive form backing the post editor; initialized in {@link ngOnInit}. */
  public addPostForm!: FormGroup;
  /** Whether the post form is currently displayed. */
  public showForm = signal(false);
  /** Whether a create/update request is currently in progress. */
  public posting = signal(false);

  /**
   * Angular lifecycle hook. Initializes {@link addPostForm} with a single
   * required `text` control that updates on change.
   */
  ngOnInit() {
    this.addPostForm = this.fb.group(
      {
        text: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Angular lifecycle hook reacting to input changes.
   *
   * - When {@link futureQuote} changes to a defined value, resolves the quoted
   *   author's full name, builds an HTML blockquote, patches it into the form
   *   and reveals the form.
   * - When {@link editPost} changes to a defined value, patches the post's
   *   current message into the form and reveals it.
   * - When {@link topic} changes, resets the form via {@link resetForm}.
   *
   * @param changes The set of changed input properties provided by Angular.
   * @returns A promise that resolves once change handling (including author
   * name resolution) has completed.
   */
  ngOnChanges(changes: SimpleChanges) {
    void this.handleChanges(changes);
  }

  private async handleChanges(changes: SimpleChanges) {
    if (changes.futureQuote) {
      if (changes.futureQuote.currentValue !== undefined) {
        const quoting = this.translateService.translate('label.quoting');
        const authorFullName = await getUserFullName(
          changes.futureQuote.currentValue.author as string,
          this.userService
        );
        const text = `<br/><br/><p>${quoting}: ${authorFullName}
        </p><blockquote>${changes.futureQuote.currentValue.text}</blockquote>`;
        if (this.addPostForm) {
          this.addPostForm.patchValue({ text });
          this.showForm.set(true);
        }
      }
    } else if (changes.editPost) {
      if (changes.editPost.currentValue !== undefined) {
        const text = changes.editPost.currentValue.properties.message;
        if (this.addPostForm) {
          this.addPostForm.patchValue({ text });
          this.showForm.set(true);
        }
      }
    } else if (changes.topic) {
      this.resetForm();
    }
  }

  /**
   * Hides the form and clears the reactive form's values.
   */
  public resetForm() {
    this.showForm.set(false);
    this.addPostForm.reset();
  }

  /**
   * Creates a new post (reply) in the current {@link topic} using the form's
   * contents.
   *
   * Does nothing when the form is invalid. On success the form is reset and
   * hidden. Regardless of outcome, an {@link ActionEmitterResult} with type
   * {@link ActionType.CREATE_POST} is emitted via {@link postedComment}.
   *
   * @returns A promise that resolves once the reply request has completed and
   * the result has been emitted.
   */
  public async addPost() {
    if (this.addPostForm.valid) {
      this.posting.set(true);
      const body: Comment = {
        ...this.addPostForm.value,
      };

      const result: ActionEmitterResult = {};
      result.type = ActionType.CREATE_POST;

      try {
        const topic = this.topic();
        if (topic.id) {
          await this.topicService.postReplyAsync({
            id: topic.id,
            notify: true,
            comment: body,
          });
          result.result = ActionResult.SUCCEED;
          this.addPostForm.reset();
          this.showForm.set(false);
        }
      } catch (error) {
        console.error(error);
        result.result = ActionResult.FAILED;
      }
      this.posting.set(false);
      this.postedComment.emit(result);
    }
  }

  /**
   * Updates the existing {@link editPost} with the form's contents.
   *
   * Does nothing when the form is invalid or when no editable post/properties
   * are available. On success the form is reset and hidden. An
   * {@link ActionEmitterResult} with type {@link ActionType.EDIT_POST} is
   * emitted via {@link postedComment} once the request completes.
   *
   * @returns A promise that resolves once the update request has completed and
   * the result has been emitted.
   */
  public async updatePost() {
    if (this.addPostForm.valid) {
      this.posting.set(true);

      const result: ActionEmitterResult = {};
      result.type = ActionType.EDIT_POST;

      const editPost = this.editPost();
      if (editPost?.properties) {
        editPost.properties.message = this.addPostForm.value.text;

        try {
          if (editPost.id) {
            await this.postService.putPostAsync({
              id: editPost.id,
              notify: false,
              post: editPost,
            });
            result.result = ActionResult.SUCCEED;
            this.addPostForm.reset();
            this.showForm.set(false);
          }
        } catch (error) {
          console.error(error);
          result.result = ActionResult.FAILED;
        }
        this.posting.set(false);
        this.postedComment.emit(result);
      }
    }
  }
}
